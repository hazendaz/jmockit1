/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.state;

import javax.annotation.Nullable;

import mockit.internal.expectations.RecordAndReplayExecution;
import mockit.internal.expectations.mocking.FieldTypeRedefinitions;
import mockit.internal.expectations.state.ExecutingTest;
import mockit.internal.faking.FakeClasses;
import mockit.internal.faking.FakeStates;
import mockit.internal.injection.TestedClassInstantiations;
import mockit.internal.util.StackTrace;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * A singleton which stores several data structures which in turn hold global state for individual test methods, test
 * classes, and for the test run as a whole.
 */
public final class TestRun {
    private static final TestRun INSTANCE = new TestRun();

    private TestRun() {
    }

    // Fields with global state
    // ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final ThreadLocal<Integer> noMockingCount = new ThreadLocal<>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }

        @Override
        public void set(Integer valueToAdd) {
            super.set(get() + valueToAdd);
        }
    };

    // Used only by the Coverage tool:
    private int testId;

    @Nullable
    private Class<?> currentTestClass;
    @Nullable
    private Object currentTestInstance;
    @Nullable
    private FieldTypeRedefinitions fieldTypeRedefinitions;
    @Nullable
    private TestedClassInstantiations testedClassInstantiations;

    @NonNull
    private final MockFixture mockFixture = new MockFixture();

    @NonNull
    private final ExecutingTest executingTest = new ExecutingTest();
    @NonNull
    private final FakeClasses fakeClasses = new FakeClasses();

    // Static "getters" for global state
    // ///////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean isInsideNoMockingZone() {
        return noMockingCount.get() > 0;
    }

    @Nullable
    public static Class<?> getCurrentTestClass() {
        return INSTANCE.currentTestClass;
    }

    @Nullable
    public static Object getCurrentTestInstance() {
        return INSTANCE.currentTestInstance;
    }

    public static int getTestId() {
        return INSTANCE.testId;
    }

    @Nullable
    public static FieldTypeRedefinitions getFieldTypeRedefinitions() {
        return INSTANCE.fieldTypeRedefinitions;
    }

    @Nullable
    public static TestedClassInstantiations getTestedClassInstantiations() {
        return INSTANCE.testedClassInstantiations;
    }

    @NonNull
    public static MockFixture mockFixture() {
        return INSTANCE.mockFixture;
    }

    @NonNull
    public static ExecutingTest getExecutingTest() {
        return INSTANCE.executingTest;
    }

    @Nullable
    public static RecordAndReplayExecution getRecordAndReplayForRunningTest() {
        return INSTANCE.executingTest.getCurrentRecordAndReplay();
    }

    @NonNull
    public static RecordAndReplayExecution getOrCreateRecordAndReplayForRunningTest() {
        return INSTANCE.executingTest.getOrCreateRecordAndReplay();
    }

    @NonNull
    public static RecordAndReplayExecution getRecordAndReplayForVerifications() {
        return INSTANCE.executingTest.getRecordAndReplayForVerifications();
    }

    @NonNull
    public static FakeClasses getFakeClasses() {
        return INSTANCE.fakeClasses;
    }

    @NonNull
    public static FakeStates getFakeStates() {
        return INSTANCE.fakeClasses.fakeStates;
    }

    // Static "mutators" for global state
    // //////////////////////////////////////////////////////////////////////////////////////////////////

    public static void setCurrentTestClass(@Nullable Class<?> testClass) {
        INSTANCE.currentTestClass = testClass;
    }

    public static void prepareForNextTest() {
        INSTANCE.testId++;
        INSTANCE.executingTest.setRecordAndReplay(null);
    }

    public static void enterNoMockingZone() {
        noMockingCount.set(1);
    }

    public static void exitNoMockingZone() {
        noMockingCount.set(-1);
    }

    public static void clearNoMockingZone() {
        noMockingCount.remove();
    }

    public static void clearCurrentTestInstance() {
        INSTANCE.currentTestInstance = null;
    }

    public static void setRunningIndividualTest(@NonNull Object testInstance) {
        INSTANCE.currentTestInstance = testInstance;
    }

    public static void setFieldTypeRedefinitions(@Nullable FieldTypeRedefinitions redefinitions) {
        INSTANCE.fieldTypeRedefinitions = redefinitions;
    }

    public static void setTestedClassInstantiations(@Nullable TestedClassInstantiations testedClassInstantiations) {
        INSTANCE.testedClassInstantiations = testedClassInstantiations;
    }

    public static void finishCurrentTestExecution() {
        INSTANCE.executingTest.finishExecution();
    }

    // Methods to be called only from generated bytecode or from the ClassLoadingBridge
    // ////////////////////////////////////////////////////

    @SuppressWarnings({ "StaticMethodOnlyUsedInOneClass", "SimplifiableIfStatement" })
    public static boolean updateFakeState(@NonNull String fakeClassDesc, @Nullable Object mockedInstance,
            int fakeStateIndex) {
        Object fake = getFake(fakeClassDesc, mockedInstance);

        if (fake == null) {
            return false;
        }

        if (fakeStateIndex < 0) {
            return true;
        }

        return getFakeStates().updateFakeState(fake, fakeStateIndex);
    }

    @Nullable
    public static Object getFake(@NonNull String fakeClassDesc, @Nullable Object mockedInstance) {
        return INSTANCE.fakeClasses.getFake(fakeClassDesc, mockedInstance);
    }

    // Other methods ///////////////////////////////////////////////////////////////////////////////////////////////////

    public static void ensureThatClassIsInitialized(@NonNull Class<?> aClass) {
        boolean previousFlag = INSTANCE.executingTest.setShouldIgnoreMockingCallbacks(true);

        try {
            Class.forName(aClass.getName(), true, aClass.getClassLoader());
        } catch (ClassNotFoundException ignore) {
        } catch (LinkageError e) {
            StackTrace.filterStackTrace(e);
            e.printStackTrace();
        } finally {
            INSTANCE.executingTest.setShouldIgnoreMockingCallbacks(previousFlag);
        }
    }
}
