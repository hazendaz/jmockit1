package integrationTests.loops;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import integrationTests.CoverageTest;

class WhileStatementsTest extends CoverageTest {
    WhileStatements tested;

    @Test
    void whileBlockInSeparateLines() {
        tested.whileBlockInSeparateLines();

        assertLines(12, 17, 4);
        assertLine(12, 1, 1, 1);
        assertLine(14, 1, 1, 6);
        assertLine(15, 1, 1, 5);
        assertLine(17, 1, 1, 1);
    }

    @Test
    void whileBlockInSingleLine() {
        tested.whileBlockInSingleLine(0);
        tested.whileBlockInSingleLine(1);
        tested.whileBlockInSingleLine(2);

        assertLines(20, 23, 2);
        assertLine(21, 2, 2, 6);
        assertLine(23, 1, 1, 3);
    }

    @Test
    void whileWithIfElse() {
        tested.whileWithIfElse(0);
        tested.whileWithIfElse(1);
        tested.whileWithIfElse(2);

        assertLines(126, 137, 5);
    }

    @Test
    void whileWithContinue() {
        tested.whileWithContinue(0);
        tested.whileWithContinue(1);
        tested.whileWithContinue(2);

        assertLines(26, 35, 6);
        assertLine(26, 1, 1, 6);
        assertLine(27, 1, 1, 3);
        assertLine(28, 1, 1, 2);
        assertLine(29, 1, 1, 2);
        assertLine(32, 1, 1, 1);
        assertLine(35, 1, 1, 3);
    }

    @Test
    void whileWithBreak() {
        tested.whileWithBreak(0);
        tested.whileWithBreak(1);
        tested.whileWithBreak(2);

        assertLines(39, 47, 5);
        assertLine(39, 2, 2, 4);
        assertLine(40, 1, 1, 3);
        assertLine(41, 1, 1, 2);
        assertLine(44, 1, 1, 1);
        assertLine(47, 1, 1, 3);
    }

    @Test
    void nestedWhile() {
        tested.nestedWhile(0, 2);
        tested.nestedWhile(1, 1);

        assertLines(51, 58, 4);
        assertLine(51, 2, 2, 3);
        assertLine(52, 1, 1, 1);
        assertLine(53, 1, 0, 0);
        assertLine(56, 1, 1, 1);
        assertLine(58, 1, 1, 2);
    }

    @Test
    void doWhileInSeparateLines() {
        tested.doWhileInSeparateLines();

        assertLines(61, 66, 4);
        assertLine(61, 1, 1, 1);
        assertLine(64, 1, 1, 3);
        assertLine(65, 2, 2, 3);
        assertLine(66, 1, 1, 1);
    }

    @Test
    void bothKindsOfWhileCombined() {
        tested.bothKindsOfWhileCombined(0, 0);
        tested.bothKindsOfWhileCombined(0, 2);
        tested.bothKindsOfWhileCombined(1, 1);

        assertLines(69, 83, 5);
        assertLine(71, 1, 1, 5);
        assertLine(74, 2, 2, 5);
        assertLine(77, 1, 1, 4);
        assertLine(79, 2, 2, 4);
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
