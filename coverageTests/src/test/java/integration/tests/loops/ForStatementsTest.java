/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests.loops;

import static java.util.Arrays.asList;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import integration.tests.CoverageTest;

class ForStatementsTest extends CoverageTest {
    ForStatements tested;

    @Test
    void forInSeparateLines() {
        tested.forInSeparateLines();
        tested.forInSeparateLines();

        assertLines(9, 12, 3);
        assertLine(9, 2, 2, 6); // TODO: should have 3 segments
        assertLine(10, 1, 1, 4);
        assertLine(12, 1, 1, 2);
    }

    @Test
    void forInSingleLine() {
        tested.forInSingleLine(1);
        tested.forInSingleLine(2);

        assertLines(15, 18, 2);
        assertLine(16, 2, 2, 3); // TODO: should have 5 segments
        assertLine(18, 1, 1, 2);
    }

    @Test
    void forEachArrayElement() {
        int sum = tested.forEachArrayElement(1, 2, 3);
        assertEquals(6, sum);

        assertLines(21, 28, 4);
        assertLine(21, 1, 1, 1);
        assertLine(23, 2, 2, 4);
        assertLine(24, 1, 1, 3);
        assertLine(27, 1, 1, 1);
    }

    @Test
    void forEachCollectionElement() {
        String result = tested.forEachCollectionElement(asList("a", "b", "c"));
        assertEquals("abc", result);

        assertLines(31, 38, 5);
        assertLine(31, 1, 1, 1);
        assertLine(33, 2, 2, 1);
        assertLine(34, 1, 1, 3);
        assertLine(37, 1, 1, 1);
    }

    @Test
    void forUsingIterator() {
        List<? extends Number> numbers = new ArrayList<Number>(asList(1, 0L, 2.0));
        tested.forUsingIterator(numbers);

        assertLines(41, 48, 6);
        assertLine(41, 2, 2, 1);
        assertLine(42, 1, 1, 3);
        assertLine(44, 3, 3, 3);
        assertLine(45, 1, 1, 1);
        assertLine(48, 1, 1, 1);
    }

    @Disabled("for issue #254")
    @Test
    void forWithNestedIfWhichReturns() {
        tested.forWithNestedIfWhichReturns(2, 1, 2, 3);
    }
}
