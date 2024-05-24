/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import mockit.internal.util.StackTrace;

import edu.umd.cs.findbugs.annotations.NonNull;

final class ExpectationError extends AssertionError {
    private static final long serialVersionUID = 1L;
    private String message;

    @NonNull
    @Override
    public String toString() {
        return message;
    }

    void defineCause(@NonNull String title, @NonNull Throwable error) {
        message = title;
        StackTrace.filterStackTrace(this);
        error.initCause(this);
    }
}
