/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AnEnumTest extends CoverageTest {
    AnEnum tested;

    @Test
    void useAnEnum() {
        tested = AnEnum.ONE_VALUE;

        assertEquals(100, fileData.lineCoverageInfo.getCoveragePercentage());
    }
}
