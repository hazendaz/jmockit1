/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import mockit.internal.expectations.invocation.ExpectedInvocation;
import mockit.internal.expectations.invocation.InvocationConstraints;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.checkerframework.checker.nullness.qual.NonNull;

final class ReplayPhase extends Phase {
    @NonNull
    final FailureState failureState;
    @NonNull
    final List<Expectation> invocations;
    @NonNull
    final List<Object> invocationInstances;
    @NonNull
    final List<Object[]> invocationArguments;

    ReplayPhase(@NonNull PhasedExecutionState executionState, @NonNull FailureState failureState) {
        super(executionState);
        this.failureState = failureState;
        invocations = new ArrayList<>();
        invocationInstances = new ArrayList<>();
        invocationArguments = new ArrayList<>();
    }

    @Override
    @Nullable
    Object handleInvocation(@Nullable Object mock, int mockAccess, @NonNull String mockClassDesc,
            @NonNull String mockNameAndDesc, @Nullable String genericSignature, boolean withRealImpl,
            @NonNull Object[] args) throws Throwable {
        Expectation expectation = executionState.findExpectation(mock, mockClassDesc, mockNameAndDesc, args);
        Object replacementInstance = mock == null ? null
                : executionState.equivalentInstances.getReplacementInstanceForMethodInvocation(mock, mockNameAndDesc);

        if (expectation == null) {
            expectation = createExpectation(replacementInstance == null ? mock : replacementInstance, mockAccess,
                    mockClassDesc, mockNameAndDesc, genericSignature, args);
        } else if (expectation.recordPhase != null) {
            registerNewInstanceAsEquivalentToOneFromRecordedConstructorInvocation(mock, expectation.invocation);
        }

        invocations.add(expectation);
        invocationInstances.add(mock);
        invocationArguments.add(args);
        expectation.constraints.incrementInvocationCount();

        return produceResult(expectation, mock, withRealImpl, args);
    }

    @NonNull
    private Expectation createExpectation(@Nullable Object mock, int mockAccess, @NonNull String mockClassDesc,
            @NonNull String mockNameAndDesc, @Nullable String genericSignature, @NonNull Object[] args) {
        ExpectedInvocation invocation = new ExpectedInvocation(mock, mockAccess, mockClassDesc, mockNameAndDesc, false,
                genericSignature, args);
        Expectation expectation = new Expectation(invocation);
        executionState.addExpectation(expectation);
        return expectation;
    }

    private void registerNewInstanceAsEquivalentToOneFromRecordedConstructorInvocation(@Nullable Object mock,
            @NonNull ExpectedInvocation invocation) {
        if (mock != null && invocation.isConstructor()) {
            Map<Object, Object> instanceMap = getInstanceMap();
            instanceMap.put(mock, invocation.instance);
        }
    }

    @Nullable
    private Object produceResult(@NonNull Expectation expectation, @Nullable Object mock, boolean withRealImpl,
            @NonNull Object[] args) throws Throwable {
        boolean executeRealImpl = withRealImpl && expectation.recordPhase == null;

        if (executeRealImpl) {
            expectation.executedRealImplementation = true;
            return Void.class;
        }

        if (expectation.constraints.isInvocationCountMoreThanMaximumExpected()) {
            UnexpectedInvocation unexpectedInvocation = expectation.invocation.errorForUnexpectedInvocation(args);
            failureState.setErrorThrown(unexpectedInvocation);
            return null;
        }

        return expectation.produceResult(mock, args);
    }

    @Nullable
    Error endExecution() {
        return getErrorForFirstExpectationThatIsMissing();
    }

    @Nullable
    private Error getErrorForFirstExpectationThatIsMissing() {
        List<Expectation> notStrictExpectations = executionState.expectations;

        // New expectations might get added to the list, so a regular loop would cause a CME.
        for (Expectation notStrict : notStrictExpectations) {
            InvocationConstraints constraints = notStrict.constraints;

            if (constraints.isInvocationCountLessThanMinimumExpected()) {
                List<ExpectedInvocation> nonMatchingInvocations = getNonMatchingInvocations(notStrict);
                return constraints.errorForMissingExpectations(notStrict.invocation, nonMatchingInvocations);
            }
        }

        return null;
    }

    @NonNull
    private List<ExpectedInvocation> getNonMatchingInvocations(@NonNull Expectation unsatisfiedExpectation) {
        ExpectedInvocation unsatisfiedInvocation = unsatisfiedExpectation.invocation;
        List<ExpectedInvocation> nonMatchingInvocations = new ArrayList<>();

        for (Expectation replayedExpectation : invocations) {
            ExpectedInvocation replayedInvocation = replayedExpectation.invocation;

            if (replayedExpectation != unsatisfiedExpectation && replayedInvocation.isMatch(unsatisfiedInvocation)) {
                nonMatchingInvocations.add(replayedInvocation);
            }
        }

        return nonMatchingInvocations;
    }
}
