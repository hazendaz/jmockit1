/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.argumentMatching;

import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class PatternMatcher implements ArgumentMatcher<PatternMatcher> {
    @NonNull
    private final Pattern pattern;

    public PatternMatcher(@NonNull String regex) {
        pattern = Pattern.compile(regex);
    }

    @Override
    public boolean same(@NonNull PatternMatcher other) {
        return pattern == other.pattern;
    }

    @Override
    public boolean matches(@Nullable Object argValue) {
        return argValue instanceof CharSequence && pattern.matcher((CharSequence) argValue).matches();
    }

    @Override
    public void writeMismatchPhrase(@NonNull ArgumentMismatch argumentMismatch) {
        argumentMismatch.append("a string matching \"").append(pattern.toString()).append('"');
    }
}
