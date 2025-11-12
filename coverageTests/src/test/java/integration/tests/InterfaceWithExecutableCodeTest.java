/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class InterfaceWithExecutableCodeTest extends CoverageTest {
    InterfaceWithExecutableCode tested;

    @Test
    void exerciseExecutableLineInInterface() {
        assertTrue(InterfaceWithExecutableCode.N > 0);

        assertLines(16, 16, 1);
        assertLine(16, 1, 1, 1);
    }
}
