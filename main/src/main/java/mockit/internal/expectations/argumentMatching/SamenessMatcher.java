/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.argumentMatching;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class SamenessMatcher implements ArgumentMatcher<SamenessMatcher> {
    @Nullable
    private final Object object;

    public SamenessMatcher(@Nullable Object object) {
        this.object = object;
    }

    @Override
    public boolean same(@NonNull SamenessMatcher other) {
        return object == other.object;
    }

    @Override
    public boolean matches(@Nullable Object argValue) {
        return argValue == object;
    }

    @Override
    public void writeMismatchPhrase(@NonNull ArgumentMismatch argumentMismatch) {
        argumentMismatch.append("same instance as ").appendFormatted(object);
    }
}
