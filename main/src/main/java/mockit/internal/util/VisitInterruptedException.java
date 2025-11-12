/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.util;

public final class VisitInterruptedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public static final VisitInterruptedException INSTANCE = new VisitInterruptedException();

    private VisitInterruptedException() {
    }
}
