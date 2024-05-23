/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.argumentMatching;

import java.util.List;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class CaptureMatcher<T> implements ArgumentMatcher<CaptureMatcher<T>> {
    @NonNull
    private final List<T> valueHolder;
    @Nullable
    private Class<?> expectedType;

    public CaptureMatcher(@NonNull List<T> valueHolder) {
        this.valueHolder = valueHolder;
    }

    public void setExpectedType(@NonNull Class<?> expectedType) {
        this.expectedType = expectedType;
    }

    @Override
    public boolean same(@NonNull CaptureMatcher<T> other) {
        return false;
    }

    @Override
    public boolean matches(@Nullable Object argValue) {
        if (expectedType == null || expectedType.isInstance(argValue)
                || argValue == null && !expectedType.isPrimitive()) {
            // noinspection unchecked
            valueHolder.add((T) argValue);
        }

        return true;
    }

    @Override
    public void writeMismatchPhrase(@NonNull ArgumentMismatch argumentMismatch) {
    }
}
