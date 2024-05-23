/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Thrown to indicate that one or more unexpected invocations occurred during the test.
 */
public final class UnexpectedInvocation extends Error {
    private static final long serialVersionUID = 1L;

    public UnexpectedInvocation(@NonNull String detailMessage) {
        super(detailMessage);
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
