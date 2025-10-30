package integration.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
class BooleanExpressionsTest extends CoverageTest {
    BooleanExpressions tested;

    @Test
    void evalBuggyCombination() {
        // Only this combination will fail if the third condition in eval1 is changed to "z < 0",
        // which demonstrates that a more sophisticated metric than branch coverage is needed.
        assertTrue(tested.eval1(true, false, 1));

        // assertLine(7, 3, 3, 1, 1, 1);
    }

    @Test // includes executions from the previous test
    void evalOnlySomeCombinations() {
        assertTrue(tested.eval1(true, true, 0));
        assertFalse(tested.eval1(true, false, 0));

        // assertLine(7, 3, 3, 3, 3, 2);
    }

    @Test
    void evalAllCombinations() {
        assertTrue(tested.eval2(true, true, 0));
        assertTrue(tested.eval2(true, false, 1));
        assertFalse(tested.eval2(true, false, 0));
        assertFalse(tested.eval2(false, true, 0));

        // assertLine(12, 3, 3, 4, 3, 2);
    }

    @Test
    void evalAllPaths() {
        assertFalse(tested.eval3(false, true, false));
        assertTrue(tested.eval3(true, true, false));
        assertTrue(tested.eval3(true, false, true));
        assertFalse(tested.eval3(true, false, false));

        // assertLine(17, 3, 3, 4, 3, 2);
    }

    @Test
    void evalOnlyFirstAndSecondBranches() {
        assertFalse(tested.eval4(false, true, false));
        assertFalse(tested.eval4(false, false, false));
        assertFalse(tested.eval4(false, true, true));
        assertFalse(tested.eval4(false, false, true));
        assertTrue(tested.eval4(true, false, false));
        assertTrue(tested.eval4(true, false, true));

        // assertLine(22, 3, 2, 6, 2, 0);
    }

    @Test
    void eval5() {
        assertFalse(tested.eval5(false, true, true));
        assertTrue(tested.eval5(false, false, false));

        // assertLine(30, 1, 1, 1);
    }

    @Test
    void methodWithComplexExpressionWhichCallsAnotherInSameClass() {
        BooleanExpressions.isSameTypeIgnoringAutoBoxing(int.class, Integer.class);

        // TODO: assertions
    }

    @Test
    void trivialMethodWhichReturnsBooleanInput() {
        assertTrue(tested.simplyReturnsInput(true));
        assertFalse(tested.simplyReturnsInput(false));

        assertLine(137, 1, 1, 2);
    }

    @Test
    void methodWhichReturnsNegatedBoolean() {
        assertTrue(tested.returnsNegatedInput(false));

        // assertLine(58, 1, 1, 1);
    }

    @Test
    void methodWithIfElseAndTrivialTernaryOperator() {
        assertTrue(tested.returnsTrivialResultFromInputAfterIfElse(false, 1));
        assertFalse(tested.returnsTrivialResultFromInputAfterIfElse(true, 0));
    }

    @Test
    void methodWithTrivialTernaryOperatorAndTrivialIfElse() {
        assertTrue(tested.returnsResultPreviouslyComputedFromInput(false, 1));
        assertFalse(tested.returnsResultPreviouslyComputedFromInput(false, 0));
        assertTrue(tested.returnsResultPreviouslyComputedFromInput(true, 1));
        assertTrue(tested.returnsResultPreviouslyComputedFromInput(true, -1));

        assertLines(185, 195, 6);
        assertLine(185, 3, 3, 4, 2, 2);
        assertLine(188, 1, 1, 4, 3);
        assertLine(191, 1, 1, 1);
        assertLine(192, 1, 1, 1);
        assertLine(195, 1, 1, 4);
    }
}
