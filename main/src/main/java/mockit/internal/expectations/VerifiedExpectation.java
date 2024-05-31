/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.List;

import mockit.internal.expectations.argumentMatching.ArgumentMatcher;

import org.checkerframework.checker.index.qual.NonNegative;

final class VerifiedExpectation {
    @NonNull
    final Expectation expectation;
    @NonNull
    final Object[] arguments;
    @Nullable
    final List<ArgumentMatcher<?>> argMatchers;
    private final int replayIndex;

    VerifiedExpectation(@NonNull Expectation expectation, @NonNull Object[] arguments,
            @Nullable List<ArgumentMatcher<?>> argMatchers, int replayIndex) {
        this.expectation = expectation;
        this.arguments = arguments;
        this.argMatchers = argMatchers;
        this.replayIndex = replayIndex;
    }

    boolean matchesReplayIndex(@NonNegative int expectationIndex) {
        return replayIndex < 0 || replayIndex == expectationIndex;
    }

    @Nullable
    Object captureNewInstance() {
        return expectation.invocation.instance;
    }
}
