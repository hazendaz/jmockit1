/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.argumentMatching;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An argument matcher for the recording/verification of expectations.
 */
public interface ArgumentMatcher<M extends ArgumentMatcher<M>> {
    /**
     * Indicates whether this matcher instance is functionally the same as another one of the same type.
     */
    boolean same(@NonNull M other);

    /**
     * Evaluates the matcher for the given argument.
     */
    boolean matches(@Nullable Object argValue);

    /**
     * Writes a phrase to be part of an error message describing an argument mismatch.
     */
    void writeMismatchPhrase(@NonNull ArgumentMismatch argumentMismatch);
}
