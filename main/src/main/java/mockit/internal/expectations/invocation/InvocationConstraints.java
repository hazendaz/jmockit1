/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import java.util.Collections;
import java.util.List;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class InvocationConstraints {
    public int minInvocations;
    private int maxInvocations;
    @NonNegative
    public int invocationCount;

    public InvocationConstraints(boolean nonStrictInvocation) {
        setLimits(nonStrictInvocation ? 0 : 1, -1);
    }

    public void setLimits(int minInvocations, int maxInvocations) {
        this.minInvocations = minInvocations;
        this.maxInvocations = maxInvocations;
    }

    void adjustMaxInvocations(@NonNegative int expectedInvocationCount) {
        if (maxInvocations > 0 && maxInvocations < expectedInvocationCount) {
            maxInvocations = expectedInvocationCount;
        }
    }

    void setUnlimitedMaxInvocations() {
        maxInvocations = -1;
    }

    public void incrementInvocationCount() {
        invocationCount++;
    }

    public boolean isInvocationCountLessThanMinimumExpected() {
        return invocationCount < minInvocations;
    }

    public boolean isInvocationCountMoreThanMaximumExpected() {
        return maxInvocations >= 0 && invocationCount > maxInvocations;
    }

    @Nullable
    public Error verifyLowerLimit(@NonNull ExpectedInvocation invocation, int lowerLimit) {
        if (invocationCount < lowerLimit) {
            int missingInvocations = lowerLimit - invocationCount;
            return invocation.errorForMissingInvocations(missingInvocations,
                    Collections.<ExpectedInvocation>emptyList());
        }

        return null;
    }

    @Nullable
    public Error verifyUpperLimit(@NonNull ExpectedInvocation invocation, @NonNull Object[] replayArgs,
            int upperLimit) {
        if (upperLimit >= 0) {
            int unexpectedInvocations = invocationCount - upperLimit;

            if (unexpectedInvocations > 0) {
                return invocation.errorForUnexpectedInvocations(replayArgs, unexpectedInvocations);
            }
        }

        return null;
    }

    @NonNull
    public Error errorForMissingExpectations(@NonNull ExpectedInvocation invocation,
            @NonNull List<ExpectedInvocation> nonMatchingInvocations) {
        return invocation.errorForMissingInvocations(minInvocations - invocationCount, nonMatchingInvocations);
    }
}
