package integrationTests.loops;

import static java.util.Arrays.*;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.*;

import integrationTests.*;

public final class ForStatementsTest extends CoverageTest {
    ForStatements tested;

    @Test
    public void forInSeparateLines() {
        tested.forInSeparateLines();
        tested.forInSeparateLines();

        assertLines(9, 12, 3);
        assertLine(9, 1, 1, 6); // TODO: should have 3 segments
        assertLine(10, 1, 1, 4);
        assertLine(12, 1, 1, 2);
    }

    @Test
    public void forInSingleLine() {
        tested.forInSingleLine(1);
        tested.forInSingleLine(2);

        assertLines(15, 18, 2);
        assertLine(16, 1, 1, 2); // TODO: should have 5 segments
        assertLine(18, 1, 1, 2);
    }

    @Test
    public void forEachArrayElement() {
        int sum = tested.forEachArrayElement(1, 2, 3);
        assertEquals(6, sum);

        assertLines(21, 27, 4);
        assertLine(21, 1, 1, 1);
        assertLine(23, 1, 1, 4);
        assertLine(24, 1, 1, 3);
        assertLine(27, 1, 1, 1);
    }

    @Test
    public void forEachCollectionElement() {
        String result = tested.forEachCollectionElement(asList("a", "b", "c"));
        assertEquals("abc", result);

        assertLines(31, 37, 4);
        assertLine(31, 1, 1, 1);
        assertLine(33, 1, 1, 5);
        assertLine(34, 1, 1, 3);
        assertLine(37, 1, 1, 1);
    }

    @Test
    public void forUsingIterator() {
        List<? extends Number> numbers = new ArrayList<Number>(asList(1, 0L, 2.0));
        tested.forUsingIterator(numbers);

        assertLines(41, 48, 5);
        assertLine(41, 1, 1, 5); // TODO: should have 2 segments
        assertLine(42, 1, 1, 3);
        assertLine(44, 1, 1, 3);
        assertLine(45, 1, 1, 1);
        assertLine(48, 1, 1, 1);
    }

    @Test
    @Ignore("for issue #254")
    public void forWithNestedIfWhichReturns() {
        tested.forWithNestedIfWhichReturns(2, 1, 2, 3);
    }
}