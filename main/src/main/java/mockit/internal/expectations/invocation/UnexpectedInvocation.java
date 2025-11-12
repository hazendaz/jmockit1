/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
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
