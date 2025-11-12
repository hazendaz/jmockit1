/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.invocation;

import edu.umd.cs.findbugs.annotations.NonNull;

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
