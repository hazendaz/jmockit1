/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Thrown to indicate that one or more expected invocations still had not occurred by the end of the test.
 */
public final class MissingInvocation extends Error {
    private static final long serialVersionUID = 1L;

    public MissingInvocation(@NonNull String detailMessage) {
        super(detailMessage);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
