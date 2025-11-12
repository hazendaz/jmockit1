/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests.loops;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import integration.tests.CoverageTest;

class WhileStatementsTest extends CoverageTest {
    WhileStatements tested;

    @Test
    void whileBlockInSeparateLines() {
        tested.whileBlockInSeparateLines();

        assertLines(17, 22, 4);
        assertLine(17, 1, 1, 1);
        assertLine(19, 1, 1, 6);
        assertLine(20, 1, 1, 5);
        assertLine(22, 1, 1, 1);
    }

    @Test
    void whileBlockInSingleLine() {
        tested.whileBlockInSingleLine(0);
        tested.whileBlockInSingleLine(1);
        tested.whileBlockInSingleLine(2);

        assertLines(25, 28, 2);
        assertLine(26, 2, 2, 6);
        assertLine(28, 1, 1, 3);
    }

    @Test
    void whileWithIfElse() {
        tested.whileWithIfElse(0);
        tested.whileWithIfElse(1);
        tested.whileWithIfElse(2);

        assertLines(131, 142, 5);
    }

    @Test
    void whileWithContinue() {
        tested.whileWithContinue(0);
        tested.whileWithContinue(1);
        tested.whileWithContinue(2);

        assertLines(31, 40, 6);
        assertLine(31, 1, 1, 6);
        assertLine(32, 1, 1, 3);
        assertLine(33, 1, 1, 2);
        assertLine(34, 1, 1, 2);
        assertLine(37, 1, 1, 1);
        assertLine(40, 1, 1, 3);
    }

    @Test
    void whileWithBreak() {
        tested.whileWithBreak(0);
        tested.whileWithBreak(1);
        tested.whileWithBreak(2);

        assertLines(44, 52, 5);
        assertLine(44, 2, 2, 4);
        assertLine(45, 1, 1, 3);
        assertLine(46, 1, 1, 2);
        assertLine(49, 1, 1, 1);
        assertLine(52, 1, 1, 3);
    }

    @Test
    void nestedWhile() {
        tested.nestedWhile(0, 2);
        tested.nestedWhile(1, 1);

        assertLines(56, 63, 4);
        assertLine(56, 2, 2, 3);
        assertLine(57, 1, 1, 1);
        assertLine(58, 1, 0, 0);
        assertLine(61, 1, 1, 1);
        assertLine(63, 1, 1, 2);
    }

    @Test
    void doWhileInSeparateLines() {
        tested.doWhileInSeparateLines();

        assertLines(66, 71, 4);
        assertLine(66, 1, 1, 1);
        assertLine(69, 1, 1, 3);
        assertLine(70, 2, 2, 3);
        assertLine(71, 1, 1, 1);
    }

    @Test
    void bothKindsOfWhileCombined() {
        tested.bothKindsOfWhileCombined(0, 0);
        tested.bothKindsOfWhileCombined(0, 2);
        tested.bothKindsOfWhileCombined(1, 1);

        assertLines(76, 84, 4);
        assertLine(76, 1, 1, 5);
        assertLine(79, 2, 2, 5);
        assertLine(82, 1, 1, 4);
        assertLine(84, 2, 2, 4);
    }

    @Test
    void whileTrueEndingWithAnIf() {
        tested.whileTrueEndingWithAnIf(0);

        // TODO: assertions
    }

    @Test
    void whileTrueStartingWithAnIf() {
        tested.whileTrueStartingWithAnIf(0);

        // TODO: assertions
    }

    @Test
    void whileTrueWithoutExitCondition() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            tested.whileTrueWithoutExitCondition();
        });
    }

    @Test
    public void whileTrueContainingTryFinally() {
        Assertions.assertThrows(IllegalStateException.class, () -> {
            tested.whileTrueContainingTryFinally();
        });
    }
}
