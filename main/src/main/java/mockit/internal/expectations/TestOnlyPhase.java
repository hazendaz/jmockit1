/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import java.util.ArrayList;
import java.util.List;

import mockit.internal.expectations.argumentMatching.ArgumentMatcher;
import mockit.internal.expectations.argumentMatching.CaptureMatcher;
import mockit.internal.expectations.argumentMatching.ClassMatcher;

import org.checkerframework.checker.index.qual.NonNegative;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public abstract class TestOnlyPhase extends Phase {
    boolean matchInstance;
    @Nullable
    List<ArgumentMatcher<?>> argMatchers;
    @Nullable
    Expectation currentExpectation;

    TestOnlyPhase(@NonNull PhasedExecutionState executionState) {
        super(executionState);
    }

    public final void addArgMatcher(@NonNull ArgumentMatcher<?> matcher) {
        getArgumentMatchers().add(matcher);
    }

    @NonNull
    private List<ArgumentMatcher<?>> getArgumentMatchers() {
        if (argMatchers == null) {
            argMatchers = new ArrayList<>();
        }

        return argMatchers;
    }

    final void moveArgMatcher(@NonNegative int originalMatcherIndex, @NonNegative int toIndex) {
        List<ArgumentMatcher<?>> matchers = getArgumentMatchers();
        int i = getMatcherPositionIgnoringNulls(originalMatcherIndex, matchers);

        for (i--; i < toIndex; i++) {
            matchers.add(i, null);
        }
    }

    @NonNegative
    private static int getMatcherPositionIgnoringNulls(@NonNegative int originalMatcherIndex,
            @NonNull List<ArgumentMatcher<?>> matchers) {
        int i = 0;

        for (int matchersFound = 0; matchersFound <= originalMatcherIndex; i++) {
            if (matchers.get(i) != null) {
                matchersFound++;
            }
        }

        return i;
    }

    final void setExpectedSingleArgumentType(@NonNegative int parameterIndex, @NonNull Class<?> argumentType) {
        ArgumentMatcher<?> newMatcher = ClassMatcher.create(argumentType);
        getArgumentMatchers().set(parameterIndex, newMatcher);
    }

    final void setExpectedMultiArgumentType(@NonNegative int parameterIndex, @NonNull Class<?> argumentType) {
        CaptureMatcher<?> matcher = (CaptureMatcher<?>) getArgumentMatchers().get(parameterIndex);
        matcher.setExpectedType(argumentType);
    }

    void setMaxInvocationCount(int maxInvocations) {
        if (currentExpectation != null) {
            int currentMinimum = currentExpectation.constraints.minInvocations;
            int minInvocations = maxInvocations < 0 ? currentMinimum : Math.min(currentMinimum, maxInvocations);
            handleInvocationCountConstraint(minInvocations, maxInvocations);
        }
    }

    abstract void handleInvocationCountConstraint(int minInvocations, int maxInvocations);

    static boolean isEnumElement(@NonNull Object mock) {
        Object[] enumElements = mock.getClass().getEnumConstants();

        if (enumElements != null) {
            for (Object enumElement : enumElements) {
                if (enumElement == mock) {
                    return true;
                }
            }
        }

        return false;
    }
}
