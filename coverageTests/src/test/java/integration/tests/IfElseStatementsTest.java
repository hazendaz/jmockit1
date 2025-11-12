/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class IfElseStatementsTest extends CoverageTest {
    IfElseStatements tested;

    @Test
    void simpleIf() {
        tested.simpleIf(true);
        tested.simpleIf(false);

        assertLines(16, 20, 4);
        assertLine(16, 3, 3, 2);
        assertLine(17, 1, 1, 1);
        assertLine(20, 1, 1, 2);

        assertBranchingPoints(16, 2, 2);
        assertBranchingPoints(17, 0, 0);
    }

    @Test
    void ifAndElse() {
        tested.ifAndElse(true);
        tested.ifAndElse(false);
    }

    @Test
    void singleLineIf() {
        tested.singleLineIf(true);
        tested.singleLineIf(false);

        assertLines(43, 46, 2);
        assertLine(44, 3, 3, 2);
        assertLine(46, 1, 1, 2);
    }

    @Test
    void singleLineIfAndElse() {
        tested.singleLineIfAndElse(true);
        tested.singleLineIfAndElse(false);

        assertLines(55, 58, 2);
        assertLine(56, 3, 3, 2, 1, 1);
        assertLine(58, 1, 1, 2);
    }

    @Test
    void singleLineIfAndElseWhereOnlyTheElseIsExecuted() {
        tested.anotherSingleLineIfAndElse(false);

        assertLines(248, 248, 1);
        assertLine(248, 3, 2, 1, 0, 1);
    }

    @Test
    void singleLineIfAndElseWhereElseIsExecutedMoreTimes() {
        tested.yetAnotherSingleLineIfAndElse(false);
        tested.yetAnotherSingleLineIfAndElse(true);
        tested.yetAnotherSingleLineIfAndElse(false);

        assertLines(262, 262, 1);
        assertLine(262, 3, 3, 3, 1, 2);
    }

    @Test
    void ifWithBooleanAndOperator() {
        tested.ifWithBooleanAndOperator(true, false);
        tested.ifWithBooleanAndOperator(false, true);

        assertLines(275, 278, 2);
        assertLine(275, 5, 4, 2, 1);
        assertLine(276, 1, 0, 0);
        assertLine(278, 1, 1, 2);
    }

    @Disabled
    @Test
    void anotherIfWithBooleanAndOperator() {
        tested.anotherIfWithBooleanAndOperator(true, true);
        tested.anotherIfWithBooleanAndOperator(true, false);

        assertLines(172, 175, 3);
        assertLine(172, 3, 2, 2, 2, 0);
        assertLine(173, 1, 1, 1);
        assertLine(175, 1, 1, 2);
    }

    @Test
    void ifWithBooleanOrOperator() {
        tested.ifWithBooleanOrOperator(false, false);
        tested.ifWithBooleanOrOperator(true, true);

        assertLines(289, 292, 3);
        assertLine(289, 5, 4, 2, 1);
        assertLine(290, 1, 1, 1);
        assertLine(292, 1, 1, 2);
    }

    @Test
    void methodWithFourDifferentPathsAndSimpleLines_exerciseTwoOppositePaths() {
        tested.methodWithFourDifferentPathsAndSimpleLines(true, 0);
        tested.methodWithFourDifferentPathsAndSimpleLines(false, 1);

        // TODO: assertions
    }

    @Test
    void methodWithFourDifferentPathsAndSegmentedLines_exerciseTwoOppositePaths() {
        tested.methodWithFourDifferentPathsAndSegmentedLines(false, -1);
        tested.methodWithFourDifferentPathsAndSegmentedLines(true, 1);

        // TODO: assertions
    }

    @Test
    void ifElseWithComplexBooleanCondition() {
        tested.ifElseWithComplexBooleanCondition(true, false);

        // TODO: assertions
    }

    @Test
    void returnInput() {
        assertEquals(2, tested.returnInput(1, true, false, false));
        assertEquals(2, tested.returnInput(2, false, false, false));
        assertEquals(2, tested.returnInput(3, false, true, false));
        assertEquals(4, tested.returnInput(4, false, false, true));
        assertEquals(5, tested.returnInput(5, true, true, false));
        assertEquals(5, tested.returnInput(6, false, true, true));
        assertEquals(7, tested.returnInput(7, true, true, true));
        assertEquals(9, tested.returnInput(8, true, false, true));
    }

    @Test
    void nestedIf() {
        assertEquals(1, tested.nestedIf(false, false));
        assertEquals(2, tested.nestedIf(true, true));

        // TODO: assertions
    }

    @Test
    void ifElseWithNestedIf() {
        assertEquals(1, tested.ifElseWithNestedIf(true, false));
        assertEquals(2, tested.ifElseWithNestedIf(true, true));
        assertEquals(3, tested.ifElseWithNestedIf(false, false));

        // TODO: assertions
    }

    @Test
    void nestedIfElse() {
        assertEquals(1, tested.nestedIfElse(false, false));
        assertEquals(2, tested.nestedIfElse(true, true));
        assertEquals(3, tested.nestedIfElse(true, false));
        assertEquals(4, tested.nestedIfElse(false, true));

        // TODO: assertions
    }

    @Test
    void infeasiblePaths() {
        tested.infeasiblePaths(true);
        tested.infeasiblePaths(false);

        // TODO: assertions
    }

    @Test
    void ifSpanningMultipleLines() {
        tested.ifSpanningMultipleLines(true, 0);
        tested.ifSpanningMultipleLines(false, -1);
        tested.ifSpanningMultipleLines(false, 1);

        assertLines(317, 325, 3);
        assertLine(318, 5, 5, 3);
        assertLine(322, 1, 1, 2);
        assertLine(325, 1, 1, 3);
    }
}
