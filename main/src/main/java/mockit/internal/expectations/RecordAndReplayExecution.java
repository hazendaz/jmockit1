/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import static mockit.internal.util.Utilities.NO_ARGS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import mockit.Expectations;
import mockit.internal.expectations.invocation.ExpectedInvocation;
import mockit.internal.expectations.mocking.CaptureOfNewInstances;
import mockit.internal.expectations.mocking.FieldTypeRedefinitions;
import mockit.internal.expectations.mocking.PartialMocking;
import mockit.internal.expectations.mocking.TypeRedefinitions;
import mockit.internal.expectations.state.ExecutingTest;
import mockit.internal.state.TestRun;
import mockit.internal.util.ClassNaming;
import mockit.internal.util.DefaultValues;
import mockit.internal.util.ObjectMethods;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class RecordAndReplayExecution {
    public static final ReentrantLock RECORD_OR_REPLAY_LOCK = new ReentrantLock();
    public static final ReentrantLock TEST_ONLY_PHASE_LOCK = new ReentrantLock();

    @Nullable
    private final PartialMocking partialMocking;
    @NonNull
    private final PhasedExecutionState executionState;
    @NonNull
    private final FailureState failureState;
    @Nullable
    private RecordPhase recordPhase;
    @Nullable
    private ReplayPhase replayPhase;
    @Nullable
    private BaseVerificationPhase verificationPhase;

    public RecordAndReplayExecution() {
        executionState = new PhasedExecutionState();
        partialMocking = null;
        discoverMockedTypesAndInstancesForMatchingOnInstance();
        failureState = new FailureState();
        replayPhase = new ReplayPhase(executionState, failureState);
    }

    public RecordAndReplayExecution(@NonNull Expectations targetObject,
            @Nullable Object... instancesToBePartiallyMocked) {
        TestRun.enterNoMockingZone();
        ExecutingTest executingTest = TestRun.getExecutingTest();
        executingTest.setShouldIgnoreMockingCallbacks(true);

        try {
            RecordAndReplayExecution previous = executingTest.getPreviousRecordAndReplay();

            executionState = previous == null ? new PhasedExecutionState() : previous.executionState;
            failureState = new FailureState();
            recordPhase = new RecordPhase(executionState);

            executingTest.setRecordAndReplay(this);
            partialMocking = applyPartialMocking(instancesToBePartiallyMocked);
            discoverMockedTypesAndInstancesForMatchingOnInstance();

            // noinspection LockAcquiredButNotSafelyReleased
            TEST_ONLY_PHASE_LOCK.lock();
        } catch (RuntimeException e) {
            executingTest.setRecordAndReplay(null);
            throw e;
        } finally {
            executingTest.setShouldIgnoreMockingCallbacks(false);
            TestRun.exitNoMockingZone();
        }
    }

    private void discoverMockedTypesAndInstancesForMatchingOnInstance() {
        TypeRedefinitions fieldTypeRedefinitions = TestRun.getFieldTypeRedefinitions();

        if (fieldTypeRedefinitions != null) {
            List<Class<?>> fields = fieldTypeRedefinitions.getTargetClasses();
            List<Class<?>> targetClasses = new ArrayList<>(fields);

            TypeRedefinitions paramTypeRedefinitions = TestRun.getExecutingTest().getParameterRedefinitions();

            if (paramTypeRedefinitions != null) {
                targetClasses.addAll(paramTypeRedefinitions.getTargetClasses());
            }

            executionState.instanceBasedMatching.discoverMockedTypesToMatchOnInstances(targetClasses);

            if (partialMocking != null && !partialMocking.targetInstances.isEmpty()) {
                executionState.partiallyMockedInstances = new PartiallyMockedInstances(partialMocking.targetInstances);
            }
        }
    }

    @Nullable
    private static PartialMocking applyPartialMocking(@Nullable Object... instances) {
        if (instances == null || instances.length == 0) {
            return null;
        }

        PartialMocking mocking = new PartialMocking();
        mocking.redefineTypes(instances);
        return mocking;
    }

    @Nullable
    public RecordPhase getRecordPhase() {
        return recordPhase;
    }

    /**
     * Only to be called from generated bytecode or from the Mocking Bridge.
     */
    @Nullable
    public static Object recordOrReplay(@Nullable Object mock, int mockAccess, @NonNull String classDesc,
            @NonNull String mockDesc, @Nullable String genericSignature, int executionModeOrdinal,
            @Nullable Object[] args) throws Throwable {
        @NonNull
        Object[] mockArgs = args == null ? NO_ARGS : args;
        ExecutionMode executionMode = ExecutionMode.values()[executionModeOrdinal];

        if (notToBeMocked(mock, classDesc)) {
            // This occurs if called from a custom argument matching method, in a call to an overridden Object method
            // (equals, hashCode,
            // toString), from a different thread during recording/verification, or during replay but between tests.
            return defaultReturnValue(mock, classDesc, mockDesc, genericSignature, executionMode, mockArgs);
        }

        ExecutingTest executingTest = TestRun.getExecutingTest();

        if (executingTest.isShouldIgnoreMockingCallbacks()) {
            // This occurs when called from a reentrant delegate method, or during static initialization of a mocked
            // class.
            return defaultReturnValue(executingTest, mock, classDesc, mockDesc, genericSignature, executionMode,
                    mockArgs);
        }

        if (executingTest.shouldProceedIntoRealImplementation(mock, classDesc)
                || executionMode.isToExecuteRealImplementation(mock)) {
            return Void.class;
        }

        boolean isConstructor = mock != null && mockDesc.startsWith("<init>");
        RECORD_OR_REPLAY_LOCK.lock();

        try {
            RecordAndReplayExecution instance = executingTest.getOrCreateRecordAndReplay();

            if (isConstructor && instance.handleCallToConstructor(mock, classDesc)) {
                return instance.getResultForConstructor(mock, executionMode);
            }

            return instance.getResult(mock, mockAccess, classDesc, mockDesc, genericSignature, executionMode, mockArgs);
        } finally {
            RECORD_OR_REPLAY_LOCK.unlock();
        }
    }

    private static boolean notToBeMocked(@Nullable Object mock, @NonNull String classDesc) {
        return RECORD_OR_REPLAY_LOCK.isHeldByCurrentThread()
                || TEST_ONLY_PHASE_LOCK.isLocked() && !TEST_ONLY_PHASE_LOCK.isHeldByCurrentThread()
                || !TestRun.mockFixture().isStillMocked(mock, classDesc);
    }

    @NonNull
    private static Object defaultReturnValue(@Nullable Object mock, @NonNull String classDesc,
            @NonNull String nameAndDesc, @Nullable String genericSignature, @NonNull ExecutionMode executionMode,
            @NonNull Object[] args) {
        if (executionMode.isToExecuteRealImplementation(mock)) {
            return Void.class;
        }

        if (mock != null) {
            Object rv = ObjectMethods.evaluateOverride(mock, nameAndDesc, args);

            if (rv != null) {
                return executionMode.isToExecuteRealObjectOverride(mock) ? Void.class : rv;
            }
        }

        String returnTypeDesc = DefaultValues.getReturnTypeDesc(nameAndDesc);

        if (returnTypeDesc.charAt(0) == 'L') {
            ExpectedInvocation invocation = new ExpectedInvocation(mock, classDesc, nameAndDesc, genericSignature,
                    args);
            Object cascadedInstance = invocation.getDefaultValueForReturnType();

            if (cascadedInstance != null) {
                return cascadedInstance;
            }
        }

        return Void.class;
    }

    @Nullable
    private static Object defaultReturnValue(@NonNull ExecutingTest executingTest, @Nullable Object mock,
            @NonNull String classDesc, @NonNull String nameAndDesc, @Nullable String genericSignature,
            @NonNull ExecutionMode executionMode, @NonNull Object[] args) throws Throwable {
        RecordAndReplayExecution execution = executingTest.getCurrentRecordAndReplay();

        if (execution != null) {
            Expectation recordedExpectation = execution.executionState.findExpectation(mock, classDesc, nameAndDesc,
                    args);

            if (recordedExpectation != null) {
                return recordedExpectation.produceResult(mock, args);
            }
        }

        return defaultReturnValue(mock, classDesc, nameAndDesc, genericSignature, executionMode, args);
    }

    private boolean handleCallToConstructor(@NonNull Object mock, @NonNull String classDesc) {
        if (replayPhase != null) {
            TypeRedefinitions paramTypeRedefinitions = TestRun.getExecutingTest().getParameterRedefinitions();

            if (paramTypeRedefinitions != null) {
                CaptureOfNewInstances paramTypeCaptures = paramTypeRedefinitions.getCaptureOfNewInstances();

                if (paramTypeCaptures != null && paramTypeCaptures.captureNewInstance(null, mock)) {
                    return true;
                }
            }

            FieldTypeRedefinitions fieldTypeRedefinitions = TestRun.getFieldTypeRedefinitions();

            if (fieldTypeRedefinitions != null
                    && fieldTypeRedefinitions.captureNewInstanceForApplicableMockField(mock)) {
                return true;
            }
        }

        return isCallToSuperClassConstructor(mock, classDesc);
    }

    private static boolean isCallToSuperClassConstructor(@NonNull Object mock, @NonNull String calledClassDesc) {
        Class<?> mockedClass = mock.getClass();

        if (ClassNaming.isAnonymousClass(mockedClass)) {
            // An anonymous class instantiation always invokes the constructor on the super-class,
            // so that is the class we need to consider, not the anonymous one.
            mockedClass = mockedClass.getSuperclass();

            if (mockedClass == Object.class) {
                return false;
            }
        }

        String calledClassName = calledClassDesc.replace('/', '.');

        return !calledClassName.equals(mockedClass.getName());
    }

    @Nullable
    private Object getResultForConstructor(@NonNull Object mock, @NonNull ExecutionMode executionMode) {
        return executionMode == ExecutionMode.Regular || executionMode == ExecutionMode.Partial && replayPhase == null
                || TestRun.getExecutingTest().isInjectableMock(mock) ? null : Void.class;
    }

    @Nullable
    private Object getResult(@Nullable Object mock, int mockAccess, @NonNull String classDesc, @NonNull String mockDesc,
            @Nullable String genericSignature, @NonNull ExecutionMode executionMode, @NonNull Object[] args)
            throws Throwable {
        Phase currentPhase = getCurrentPhase();
        failureState.clearErrorThrown();

        boolean withRealImpl = executionMode.isWithRealImplementation(mock);
        Object result = currentPhase.handleInvocation(mock, mockAccess, classDesc, mockDesc, genericSignature,
                withRealImpl, args);

        failureState.reportErrorThrownIfAny();
        return result;
    }

    @NonNull
    private Phase getCurrentPhase() {
        ReplayPhase replay = replayPhase;

        if (replay == null) {
            RecordPhase recordPhaseLocal = recordPhase;
            assert recordPhaseLocal != null;
            return recordPhaseLocal;
        }

        BaseVerificationPhase verification = verificationPhase;

        if (verification != null) {
            return verification;
        }

        return replay;
    }

    @NonNull
    public BaseVerificationPhase startVerifications(boolean inOrder,
            @Nullable Object[] mockedTypesAndInstancesToVerify) {
        assert replayPhase != null;

        if (inOrder) {
            verificationPhase = new OrderedVerificationPhase(replayPhase);
        } else if (mockedTypesAndInstancesToVerify == null) {
            verificationPhase = new UnorderedVerificationPhase(replayPhase);
        } else {
            verificationPhase = new FullVerificationPhase(replayPhase, mockedTypesAndInstancesToVerify);
        }

        return verificationPhase;
    }

    @Nullable
    public static Error endCurrentReplayIfAny() {
        RecordAndReplayExecution instance = TestRun.getRecordAndReplayForRunningTest();
        return instance == null ? null : instance.endExecution();
    }

    @Nullable
    private Error endExecution() {
        if (TEST_ONLY_PHASE_LOCK.isLocked()) {
            TEST_ONLY_PHASE_LOCK.unlock();
        }

        ReplayPhase replay = switchFromRecordToReplayIfNotYet();
        Error error = replay.endExecution();

        if (error == null) {
            error = failureState.getErrorThrownInAnotherThreadIfAny();
        }

        if (error == null && verificationPhase != null) {
            error = verificationPhase.endVerification();
            verificationPhase = null;
        }

        return error;
    }

    @NonNull
    private ReplayPhase switchFromRecordToReplayIfNotYet() {
        if (replayPhase == null) {
            recordPhase = null;
            replayPhase = new ReplayPhase(executionState, failureState);
        }

        return replayPhase;
    }

    @Nullable
    TestOnlyPhase getCurrentTestOnlyPhase() {
        return recordPhase != null ? recordPhase : verificationPhase;
    }

    void endInvocations() {
        TEST_ONLY_PHASE_LOCK.unlock();

        if (verificationPhase == null) {
            switchFromRecordToReplayIfNotYet();
        } else {
            Error error = verificationPhase.endVerification();
            verificationPhase = null;

            if (error != null) {
                throw error;
            }
        }
    }
}
