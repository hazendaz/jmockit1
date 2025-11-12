/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
class UnreachableStatementsTest extends CoverageTest {
    UnreachableStatements tested;

    @Test
    void staticClassInitializerShouldHaveNoBranches() {
        assertLine(8, 0, 0, 0); // one execution for each test (the constructor), plus one for the static initializer
    }

    @Test
    void nonBranchingMethodWithUnreachableLines() {
        try {
            tested.nonBranchingMethodWithUnreachableLines();
        } catch (AssertionError ignore) {
        }

        assertLines(17, 20, 2);
        assertLine(17, 1, 1, 1);
        assertLine(18, 1, 1, 1);
        assertLine(19, 1, 0, 0);
        assertLine(20, 1, 0, 0);
    }

    @Test
    void branchingMethodWithUnreachableLines_avoidAssertion() {
        tested.branchingMethodWithUnreachableLines(0);

        assertLines(29, 35, 3);
        assertLine(29, 3, 2, 1);
        assertLine(30, 1, 0, 0);
        assertLine(31, 1, 0, 0);
        assertLine(34, 1, 1, 1);
        assertLine(35, 1, 1, 1);
    }

    @Test
    void branchingMethodWithUnreachableLines_hitAndFailAssertion() {
        try {
            tested.branchingMethodWithUnreachableLines(1);
        } catch (AssertionError ignore) {
        }

        // Accounts for executions from previous test.
        assertLines(29, 35, 4);
        assertLine(29, 3, 3, 2);
        assertLine(30, 1, 1, 1);
        assertLine(31, 1, 0, 0);
    }
}
