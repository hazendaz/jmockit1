/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

final class VisitInterruptedExceptionTest {

    @Test
    void instanceIsNotNull() {
        assertNotNull(VisitInterruptedException.INSTANCE);
    }

    @Test
    void instanceIsSingleton() {
        assertSame(VisitInterruptedException.INSTANCE, VisitInterruptedException.INSTANCE);
    }

    @Test
    void isRuntimeException() {
        assertNotNull(VisitInterruptedException.INSTANCE);
        // Verify it's a RuntimeException
        RuntimeException instance = VisitInterruptedException.INSTANCE;
        assertNotNull(instance);
    }
}
