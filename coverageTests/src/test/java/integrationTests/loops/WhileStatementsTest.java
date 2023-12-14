package integrationTests.loops;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import integrationTests.CoverageTest;

class WhileStatementsTest extends CoverageTest {
    WhileStatements tested;

    @Test
    void whileBlockInSeparateLines() {
        tested.whileBlockInSeparateLines();

        assertLines(5, 10, 4);
        assertLine(5, 1, 1, 1);
        assertLine(7, 1, 1, 6);
        assertLine(8, 1, 1, 5);
        assertLine(10, 1, 1, 1);
    }

    @Test
    void whileBlockInSingleLine() {
        tested.whileBlockInSingleLine(0);
        tested.whileBlockInSingleLine(1);
        tested.whileBlockInSingleLine(2);

        assertLines(13, 16, 2);
        assertLine(14, 2, 2, 6);
        assertLine(16, 1, 1, 3);
    }

    @Test
    void whileWithIfElse() {
        tested.whileWithIfElse(0);
        tested.whileWithIfElse(1);
        tested.whileWithIfElse(2);

        assertLines(119, 130, 5);
    }

    @Test
    void whileWithContinue() {
        tested.whileWithContinue(0);
        tested.whileWithContinue(1);
        tested.whileWithContinue(2);

        assertLines(19, 28, 6);
        assertLine(19, 1, 1, 6);
        assertLine(20, 1, 1, 3);
        assertLine(21, 1, 1, 2);
        assertLine(22, 1, 1, 2);
        assertLine(25, 1, 1, 1);
        assertLine(28, 1, 1, 3);
    }

    @Test
    void whileWithBreak() {
        tested.whileWithBreak(0);
        tested.whileWithBreak(1);
        tested.whileWithBreak(2);

        assertLines(32, 40, 5);
        assertLine(32, 2, 2, 4);
        assertLine(33, 1, 1, 3);
        assertLine(34, 1, 1, 2);
        assertLine(37, 1, 1, 1);
        assertLine(40, 1, 1, 3);
    }

    @Test
    void nestedWhile() {
        tested.nestedWhile(0, 2);
        tested.nestedWhile(1, 1);

        assertLines(44, 51, 4);
        assertLine(44, 2, 2, 3);
        assertLine(45, 1, 1, 1);
        assertLine(46, 1, 0, 0);
        assertLine(49, 1, 1, 1);
        assertLine(51, 1, 1, 2);
    }

    @Test
    void doWhileInSeparateLines() {
        tested.doWhileInSeparateLines();

        assertLines(54, 59, 4);
        assertLine(54, 1, 1, 1);
        assertLine(57, 1, 1, 3);
        assertLine(58, 2, 2, 3);
        assertLine(59, 1, 1, 1);
    }

    @Test
    void bothKindsOfWhileCombined() {
        tested.bothKindsOfWhileCombined(0, 0);
        tested.bothKindsOfWhileCombined(0, 2);
        tested.bothKindsOfWhileCombined(1, 1);

        assertLines(62, 76, 5);
        assertLine(64, 1, 1, 5);
        assertLine(67, 2, 2, 5);
        assertLine(70, 1, 1, 4);
        assertLine(72, 2, 2, 4);
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
