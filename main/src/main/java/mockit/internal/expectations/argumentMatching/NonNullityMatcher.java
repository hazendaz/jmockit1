/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.argumentMatching;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

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
