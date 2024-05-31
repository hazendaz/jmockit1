/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import mockit.internal.expectations.invocation.ExpectedInvocation;
import mockit.internal.expectations.invocation.InvocationArguments;
import mockit.internal.util.ClassLoad;

import org.checkerframework.checker.index.qual.NonNegative;

final class FullVerificationPhase extends UnorderedVerificationPhase {
    @NonNull
    private final Object[] mockedTypesAndInstancesToVerify;

    FullVerificationPhase(@NonNull ReplayPhase replayPhase, @NonNull Object[] mockedTypesAndInstancesToVerify) {
        super(replayPhase);
        this.mockedTypesAndInstancesToVerify = mockedTypesAndInstancesToVerify;
    }

    @Nullable
    @Override
    Error endVerification() {
        if (pendingError != null) {
            return pendingError;
        }

        List<Expectation> expectationsInReplayOrder = replayPhase.invocations;
        List<Expectation> notVerified = new ArrayList<>();

        for (int i = 0, n = expectationsInReplayOrder.size(); i < n; i++) {
            Expectation replayExpectation = expectationsInReplayOrder.get(i);

            if (replayExpectation != null && isEligibleForFullVerification(replayExpectation)) {
                Object[] replayArgs = replayPhase.invocationArguments.get(i);

                if (!wasVerified(replayExpectation, replayArgs, i)) {
                    notVerified.add(replayExpectation);
                }
            }
        }

        if (!notVerified.isEmpty()) {
            if (mockedTypesAndInstancesToVerify.length == 0) {
                Expectation firstUnexpected = notVerified.get(0);
                return firstUnexpected.invocation.errorForUnexpectedInvocation();
            }

            return validateThatUnverifiedInvocationsAreAllowed(notVerified);
        }

        return null;
    }

    private static boolean isEligibleForFullVerification(@NonNull Expectation replayExpectation) {
        return !replayExpectation.executedRealImplementation && replayExpectation.constraints.minInvocations <= 0;
    }

    private boolean wasVerified(@NonNull Expectation replayExpectation, @NonNull Object[] replayArgs,
            @NonNegative int expectationIndex) {
        InvocationArguments invokedArgs = replayExpectation.invocation.arguments;
        List<VerifiedExpectation> verifiedExpectations = executionState.verifiedExpectations;

        for (VerifiedExpectation verified : verifiedExpectations) {
            if (verified.expectation == replayExpectation) {
                Object[] storedArgs = invokedArgs.prepareForVerification(verified.arguments, verified.argMatchers);
                boolean argumentsMatch = invokedArgs.isMatch(replayArgs, getInstanceMap());
                invokedArgs.setValuesWithNoMatchers(storedArgs);

                if (argumentsMatch && verified.matchesReplayIndex(expectationIndex)) {
                    return true;
                }
            }
        }

        invokedArgs.setValuesWithNoMatchers(replayArgs);
        return false;
    }

    @Nullable
    private Error validateThatUnverifiedInvocationsAreAllowed(@NonNull List<Expectation> unverified) {
        for (Expectation expectation : unverified) {
            ExpectedInvocation invocation = expectation.invocation;

            if (isInvocationToBeVerified(invocation)) {
                return invocation.errorForUnexpectedInvocation();
            }
        }

        return null;
    }

    private boolean isInvocationToBeVerified(@NonNull ExpectedInvocation unverifiedInvocation) {
        String invokedClassName = unverifiedInvocation.getClassName();
        Object invokedInstance = unverifiedInvocation.instance;

        for (Object mockedTypeOrInstance : mockedTypesAndInstancesToVerify) {
            if (mockedTypeOrInstance instanceof Class) {
                Class<?> mockedType = (Class<?>) mockedTypeOrInstance;

                if (invokedClassName.equals(mockedType.getName())) {
                    return true;
                }
            } else if (invokedInstance == null) {
                ClassLoader loader = mockedTypeOrInstance.getClass().getClassLoader();
                Class<?> invokedClass = ClassLoad.loadFromLoader(loader, invokedClassName);

                if (invokedClass.isInstance(mockedTypeOrInstance)) {
                    return true;
                }
            } else if (unverifiedInvocation.matchInstance) {
                if (mockedTypeOrInstance == invokedInstance) {
                    return true;
                }
            } else if (invokedInstance.getClass().isInstance(mockedTypeOrInstance)) {
                return true;
            }
        }

        return false;
    }
}
