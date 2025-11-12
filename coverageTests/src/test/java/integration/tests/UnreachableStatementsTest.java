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
        assertLine(3, 0, 0, 0); // one execution for each test (the constructor), plus one for the static initializer
    }

    @Test
    void nonBranchingMethodWithUnreachableLines() {
        try {
            tested.nonBranchingMethodWithUnreachableLines();
        } catch (AssertionError ignore) {
        }

        assertLines(12, 15, 2);
        assertLine(12, 1, 1, 1);
        assertLine(13, 1, 1, 1);
        assertLine(14, 1, 0, 0);
        assertLine(15, 1, 0, 0);
    }

    @Test
    void branchingMethodWithUnreachableLines_avoidAssertion() {
        tested.branchingMethodWithUnreachableLines(0);

        assertLines(24, 30, 3);
        assertLine(24, 3, 2, 1);
        assertLine(25, 1, 0, 0);
        assertLine(26, 1, 0, 0);
        assertLine(29, 1, 1, 1);
        assertLine(30, 1, 1, 1);
    }

    @Test
    void branchingMethodWithUnreachableLines_hitAndFailAssertion() {
        try {
            tested.branchingMethodWithUnreachableLines(1);
        } catch (AssertionError ignore) {
        }

        // Accounts for executions from previous test.
        assertLines(24, 30, 4);
        assertLine(24, 3, 3, 2);
        assertLine(25, 1, 1, 1);
        assertLine(26, 1, 0, 0);
    }
}
