/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mockit.internal.expectations.argumentMatching.ArgumentMatcher;
import mockit.internal.expectations.invocation.ExpectedInvocation;
import mockit.internal.expectations.invocation.InvocationArguments;

import org.checkerframework.checker.index.qual.NonNegative;

public abstract class BaseVerificationPhase extends TestOnlyPhase {
    @Nonnull
    final ReplayPhase replayPhase;
    @Nonnull
    private final List<VerifiedExpectation> currentVerifiedExpectations;
    @Nullable
    Expectation currentVerification;
    int replayIndex;
    @Nullable
    Error pendingError;
    @Nullable
    ExpectedInvocation matchingInvocationWithDifferentArgs;

    BaseVerificationPhase(@Nonnull ReplayPhase replayPhase) {
        super(replayPhase.executionState);
        this.replayPhase = replayPhase;
        currentVerifiedExpectations = new ArrayList<>();
    }

    @Nullable
    @Override
    final Object handleInvocation(@Nullable Object mock, int mockAccess, @Nonnull String mockClassDesc,
            @Nonnull String mockNameAndDesc, @Nullable String genericSignature, boolean withRealImpl,
            @Nonnull Object[] args) {
        if (pendingError != null) {
            replayPhase.failureState.setErrorThrown(pendingError);
            pendingError = null;
            return null;
        }

        matchInstance = mock != null && (executionState.equivalentInstances.isReplacementInstance(mock, mockNameAndDesc)
                || isEnumElement(mock));

        ExpectedInvocation currentInvocation = new ExpectedInvocation(mock, mockAccess, mockClassDesc, mockNameAndDesc,
                matchInstance, genericSignature, args);
        currentInvocation.arguments.setMatchers(argMatchers);
        currentVerification = new Expectation(currentInvocation);

        currentExpectation = null;
        currentVerifiedExpectations.clear();
        List<ExpectedInvocation> matchingInvocationsWithDifferentArgs = findExpectation(mock, mockClassDesc,
                mockNameAndDesc, args);
        argMatchers = null;

        if (replayPhase.failureState.getErrorThrown() != null) {
            return null;
        }

        if (currentExpectation == null) {
            pendingError = currentVerification.invocation
                    .errorForMissingInvocation(matchingInvocationsWithDifferentArgs);
            currentExpectation = currentVerification;
        }

        return currentExpectation.invocation.getDefaultValueForReturnType();
    }

    @Nonnull
    abstract List<ExpectedInvocation> findExpectation(@Nullable Object mock, @Nonnull String mockClassDesc,
            @Nonnull String mockNameAndDesc, @Nonnull Object[] args);

    final boolean matches(@Nullable Object mock, @Nonnull String mockClassDesc, @Nonnull String mockNameAndDesc,
            @Nonnull Object[] args, @Nonnull Expectation replayExpectation, @Nullable Object replayInstance,
            @Nonnull Object[] replayArgs) {
        ExpectedInvocation invocation = replayExpectation.invocation;
        boolean constructor = invocation.isConstructor();
        Map<Object, Object> replacementMap = getReplacementMap();
        matchingInvocationWithDifferentArgs = null;

        if (invocation.isMatch(mock, mockClassDesc, mockNameAndDesc, replacementMap)) {
            boolean matching;

            if (mock == null || invocation.instance == null || constructor && !matchInstance) {
                matching = true;
            } else {
                matching = executionState.equivalentInstances.areMatchingInstances(matchInstance, invocation.instance,
                        mock);
            }

            if (matching) {
                matchingInvocationWithDifferentArgs = invocation;

                InvocationArguments invocationArguments = invocation.arguments;
                List<ArgumentMatcher<?>> originalMatchers = invocationArguments.getMatchers();
                Object[] originalArgs = invocationArguments.prepareForVerification(args, argMatchers);
                boolean argumentsMatch = invocationArguments.isMatch(replayArgs, getInstanceMap());
                invocationArguments.setValuesAndMatchers(originalArgs, originalMatchers);

                if (argumentsMatch) {
                    addVerifiedExpectation(replayExpectation, replayArgs);
                    return true;
                }
            }
        }

        return false;
    }

    abstract void addVerifiedExpectation(@Nonnull Expectation expectation, @Nonnull Object[] args);

    final void addVerifiedExpectation(@Nonnull VerifiedExpectation verifiedExpectation) {
        executionState.verifiedExpectations.add(verifiedExpectation);
        currentVerifiedExpectations.add(verifiedExpectation);
    }

    @Override
    final void setMaxInvocationCount(int maxInvocations) {
        if (maxInvocations == 0 || pendingError == null) {
            super.setMaxInvocationCount(maxInvocations);
        }
    }

    @Nullable
    Error endVerification() {
        return pendingError;
    }

    @Nullable
    final Object getArgumentValueForCurrentVerification(@NonNegative int parameterIndex) {
        List<VerifiedExpectation> verifiedExpectations = executionState.verifiedExpectations;

        if (verifiedExpectations.isEmpty()) {
            return currentVerification == null ? null
                    : currentVerification.invocation.getArgumentValues()[parameterIndex];
        }

        VerifiedExpectation lastMatched = verifiedExpectations.get(verifiedExpectations.size() - 1);
        return lastMatched.arguments[parameterIndex];
    }

    @Nonnull
    public final <T> List<T> getNewInstancesMatchingVerifiedConstructorInvocation() {
        List<T> newInstances = new ArrayList<>();

        for (VerifiedExpectation verifiedExpectation : currentVerifiedExpectations) {
            // noinspection unchecked
            T newInstance = (T) verifiedExpectation.captureNewInstance();
            newInstances.add(newInstance);
        }

        return newInstances;
    }
}
