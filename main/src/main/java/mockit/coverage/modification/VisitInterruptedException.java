/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.modification;

final class VisitInterruptedException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    static final VisitInterruptedException INSTANCE = new VisitInterruptedException();
}
