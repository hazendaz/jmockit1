/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.argumentMatching;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

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
