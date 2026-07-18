/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.coverage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class CoveragePercentageTest {

    @Test
    void calculateReturnsMinusOneWhenTotalIsZero() {
        assertEquals(-1, CoveragePercentage.calculate(0, 0));
    }

    @Test
    void calculateReturnsZeroWhenNothingCovered() {
        assertEquals(0, CoveragePercentage.calculate(0, 10));
    }

    @Test
    void calculateReturnsHundredWhenFullyCovered() {
        assertEquals(100, CoveragePercentage.calculate(10, 10));
    }

    @Test
    void calculateRoundsToNearestInteger() {
        assertEquals(33, CoveragePercentage.calculate(1, 3));
        assertEquals(67, CoveragePercentage.calculate(2, 3));
    }

    @Test
    void percentageColorIsPureRedWhenNothingCovered() {
        assertEquals("ff0000", CoveragePercentage.percentageColor(0, 10));
    }

    @Test
    void percentageColorIsPureGreenWhenFullyCovered() {
        assertEquals("00ff00", CoveragePercentage.percentageColor(10, 10));
    }

    @Test
    void percentageColorIsIntermediateForPartialCoverage() {
        String color = CoveragePercentage.percentageColor(5, 10);

        assertEquals(6, color.length());
        assertEquals("00", color.substring(4));
    }
}
