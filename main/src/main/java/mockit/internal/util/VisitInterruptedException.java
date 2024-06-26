/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.util;

public final class VisitInterruptedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public static final VisitInterruptedException INSTANCE = new VisitInterruptedException();

    private VisitInterruptedException() {
    }
}
