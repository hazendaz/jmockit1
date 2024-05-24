/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import java.util.ArrayList;
import java.util.List;

import mockit.internal.expectations.invocation.ExpectedInvocation;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

class UnorderedVerificationPhase extends BaseVerificationPhase {
    @NonNull
    private final List<VerifiedExpectation> verifiedExpectations;

    UnorderedVerificationPhase(@NonNull ReplayPhase replayPhase) {
        super(replayPhase);
        verifiedExpectations = new ArrayList<>();
    }

    @NonNull
    @Override
    final List<ExpectedInvocation> findExpectation(@Nullable Object mock, @NonNull String mockClassDesc,
            @NonNull String mockNameAndDesc, @NonNull Object[] args) {
        if (!matchInstance && executionState.isToBeMatchedOnInstance(mock, mockNameAndDesc)) {
            matchInstance = true;
        }

        replayIndex = -1;
        List<Expectation> expectationsInReplayOrder = replayPhase.invocations;
        Expectation verification = currentVerification;
        List<ExpectedInvocation> matchingInvocationsWithDifferentArgs = new ArrayList<>();

        for (int i = 0, n = expectationsInReplayOrder.size(); i < n; i++) {
            Expectation replayExpectation = expectationsInReplayOrder.get(i);
            Object replayInstance = replayPhase.invocationInstances.get(i);
            Object[] replayArgs = replayPhase.invocationArguments.get(i);

            if (matches(mock, mockClassDesc, mockNameAndDesc, args, replayExpectation, replayInstance, replayArgs)) {
                replayIndex = i;

                if (verification != null) {
                    verification.constraints.invocationCount++;
                }

                currentExpectation = replayExpectation;
            } else if (matchingInvocationWithDifferentArgs != null) {
                matchingInvocationsWithDifferentArgs.add(matchingInvocationWithDifferentArgs);
            }
        }

        if (verification != null && replayIndex >= 0) {
            pendingError = verifyConstraints(verification);
        }

        return matchingInvocationsWithDifferentArgs;
    }

    @Nullable
    private Error verifyConstraints(@NonNull Expectation verification) {
        ExpectedInvocation lastInvocation = replayPhase.invocations.get(replayIndex).invocation;
        Object[] lastArgs = replayPhase.invocationArguments.get(replayIndex);
        return verification.verifyConstraints(lastInvocation, lastArgs, 1, -1);
    }

    @Override
    final void addVerifiedExpectation(@NonNull Expectation expectation, @NonNull Object[] args) {
        VerifiedExpectation verifiedExpectation = new VerifiedExpectation(expectation, args, argMatchers, -1);
        addVerifiedExpectation(verifiedExpectation);
        verifiedExpectations.add(verifiedExpectation);
    }

    @Override
    final void handleInvocationCountConstraint(int minInvocations, int maxInvocations) {
        pendingError = null;

        Expectation verifying = currentVerification;

        if (verifying == null) {
            return;
        }

        Error errorThrown;

        if (replayIndex >= 0) {
            ExpectedInvocation replayInvocation = replayPhase.invocations.get(replayIndex).invocation;
            Object[] replayArgs = replayPhase.invocationArguments.get(replayIndex);
            errorThrown = verifying.verifyConstraints(replayInvocation, replayArgs, minInvocations, maxInvocations);
        } else {
            errorThrown = verifying.verifyConstraints(minInvocations);
        }

        if (errorThrown != null) {
            throw errorThrown;
        }
    }
}
