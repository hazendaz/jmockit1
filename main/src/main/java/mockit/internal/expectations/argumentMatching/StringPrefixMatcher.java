/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.argumentMatching;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class StringPrefixMatcher extends SubstringMatcher {
    public StringPrefixMatcher(@NonNull CharSequence substring) {
        super(substring);
    }

    @Override
    public boolean matches(@Nullable Object argValue) {
        return argValue instanceof CharSequence && argValue.toString().startsWith(substring);
    }

    @Override
    public void writeMismatchPhrase(@NonNull ArgumentMismatch argumentMismatch) {
        argumentMismatch.append("a string starting with ").appendFormatted(substring);
    }
}
