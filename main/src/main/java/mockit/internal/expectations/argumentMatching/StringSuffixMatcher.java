/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.argumentMatching;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class StringSuffixMatcher extends SubstringMatcher {
    public StringSuffixMatcher(@NonNull CharSequence substring) {
        super(substring);
    }

    @Override
    public boolean matches(@Nullable Object argValue) {
        return argValue instanceof CharSequence && argValue.toString().endsWith(substring);
    }

    @Override
    public void writeMismatchPhrase(@NonNull ArgumentMismatch argumentMismatch) {
        argumentMismatch.append("a string ending with ").appendFormatted(substring);
    }
}
