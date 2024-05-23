/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.argumentMatching;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class NullityMatcher implements ArgumentMatcher<NullityMatcher> {
    public static final ArgumentMatcher<?> INSTANCE = new NullityMatcher();

    private NullityMatcher() {
    }

    @Override
    public boolean same(@NonNull NullityMatcher other) {
        return true;
    }

    @Override
    public boolean matches(@Nullable Object argValue) {
        return argValue == null;
    }

    @Override
    public void writeMismatchPhrase(@NonNull ArgumentMismatch argumentMismatch) {
        argumentMismatch.append("null");
    }
}
