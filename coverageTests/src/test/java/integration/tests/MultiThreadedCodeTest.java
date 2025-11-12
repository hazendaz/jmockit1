/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests;

import org.junit.jupiter.api.Test;

class MultiThreadedCodeTest extends CoverageTest {
    MultiThreadedCode tested;

    @Test
    void nonBlockingOperation() throws Exception {
        Thread worker = MultiThreadedCode.nonBlockingOperation();
        worker.join();

        assertLines(14, 19, 4);
        assertLine(14, 1, 1, 1);
        assertLine(18, 1, 1, 1);
        assertLine(19, 1, 1, 1);
    }
}
