/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.argumentMatching;

import org.checkerframework.checker.nullness.qual.NonNull;

abstract class SubstringMatcher implements ArgumentMatcher<SubstringMatcher> {
    @NonNull
    final String substring;

    SubstringMatcher(@NonNull CharSequence substring) {
        this.substring = substring.toString();
    }

    @Override
    public final boolean same(@NonNull SubstringMatcher other) {
        return substring.equals(other.substring);
    }
}
