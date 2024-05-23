/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.argumentMatching;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class NonNullityMatcher implements ArgumentMatcher<NonNullityMatcher> {
    public static final ArgumentMatcher<?> INSTANCE = new NonNullityMatcher();

    private NonNullityMatcher() {
    }

    @Override
    public boolean same(@NonNull NonNullityMatcher other) {
        return true;
    }

    @Override
    public boolean matches(@Nullable Object argValue) {
        return argValue != null;
    }

    @Override
    public void writeMismatchPhrase(@NonNull ArgumentMismatch argumentMismatch) {
        argumentMismatch.append("not null");
    }
}
