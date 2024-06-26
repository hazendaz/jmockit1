/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.argumentMatching;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Matches a decimal argument against another within a given margin of error.
 */
public final class NumericEqualityMatcher implements ArgumentMatcher<NumericEqualityMatcher> {
    private final double value;
    private final double delta;

    public NumericEqualityMatcher(double value, double delta) {
        this.value = value;
        this.delta = delta;
    }

    @Override
    @SuppressWarnings("FloatingPointEquality")
    public boolean same(@NonNull NumericEqualityMatcher other) {
        return value == other.value && delta == other.delta;
    }

    @Override
    @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
    public boolean matches(@Nullable Object decimalValue) {
        return decimalValue instanceof Number && actualDelta((Number) decimalValue) <= delta;
    }

    private double actualDelta(@NonNull Number decimalValue) {
        return Math.abs(decimalValue.doubleValue() - value);
    }

    @Override
    public void writeMismatchPhrase(@NonNull ArgumentMismatch argumentMismatch) {
        argumentMismatch.append("a numeric value within ").append(delta).append(" of ").append(value);
    }
}
