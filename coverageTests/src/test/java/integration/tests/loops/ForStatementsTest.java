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

        assertLines(14, 17, 3);
        assertLine(14, 2, 2, 6); // TODO: should have 3 segments
        assertLine(15, 1, 1, 4);
        assertLine(17, 1, 1, 2);
    }

    @Test
    void forInSingleLine() {
        tested.forInSingleLine(1);
        tested.forInSingleLine(2);

        assertLines(20, 23, 2);
        assertLine(21, 2, 2, 3); // TODO: should have 5 segments
        assertLine(23, 1, 1, 2);
    }

    @Test
    void forEachArrayElement() {
        int sum = tested.forEachArrayElement(1, 2, 3);
        assertEquals(6, sum);

        assertLines(26, 32, 4);
        assertLine(26, 1, 1, 1);
        assertLine(28, 2, 2, 4);
        assertLine(29, 1, 1, 3);
        assertLine(32, 1, 1, 1);
    }

    @Test
    void forEachCollectionElement() {
        String result = tested.forEachCollectionElement(asList("a", "b", "c"));
        assertEquals("abc", result);

        assertLines(36, 42, 5);
        assertLine(36, 1, 1, 1);
        assertLine(38, 2, 2, 1);
        assertLine(39, 1, 1, 3);
        assertLine(42, 1, 1, 1);
    }

    @Test
    void forUsingIterator() {
        List<? extends Number> numbers = new ArrayList<Number>(asList(1, 0L, 2.0));
        tested.forUsingIterator(numbers);

        assertLines(46, 53, 6);
        assertLine(46, 2, 2, 1);
        assertLine(47, 1, 1, 3);
        assertLine(49, 3, 3, 3);
        assertLine(50, 1, 1, 1);
        assertLine(53, 1, 1, 1);
    }

    @Disabled("for issue #254")
    @Test
    void forWithNestedIfWhichReturns() {
        tested.forWithNestedIfWhichReturns(2, 1, 2, 3);
    }
}
