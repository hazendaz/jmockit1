/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.argumentMatching;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

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
