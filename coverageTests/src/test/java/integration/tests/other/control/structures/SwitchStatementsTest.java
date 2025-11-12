/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests.other.control.structures;

import org.junit.jupiter.api.Test;

import integration.tests.CoverageTest;

class SwitchStatementsTest extends CoverageTest {
    final SwitchStatements tested = new SwitchStatements();

    @Test
    void switchStatementWithSparseCasesAndDefault() {
        tested.switchStatementWithSparseCasesAndDefault('A');
        tested.switchStatementWithSparseCasesAndDefault('\0');

        assertLines(10, 25, 5);
        assertLine(10, 1, 1, 2);
        assertLine(12, 1, 1, 1);
        assertLine(13, 1, 1, 1);
        assertLine(21, 1, 1, 1);
        assertLine(23, 1, 0, 0);
        assertLine(25, 1, 1, 1);
    }

    @Test
    void switchStatementWithSparseCasesAndDefaultOnDefaultCase() {
        tested.anotherSwitchStatementWithSparseCasesAndDefault('x');

        // TODO: assertions
    }

    @Test
    void switchStatementWithCompactCasesAndDefault() {
        tested.switchStatementWithCompactCasesAndDefault(2);
        tested.switchStatementWithCompactCasesAndDefault(4);

        // TODO: assertions
    }

    @Test
    void switchStatementWithCompactCasesAndDefaultOnDefaultCase() {
        tested.anotherSwitchStatementWithCompactCasesAndDefault(1);
        tested.anotherSwitchStatementWithCompactCasesAndDefault(5);

        assertLines(56, 62, 4);
        assertLine(57, 1, 1, 2);
        assertLine(58, 1, 1, 1);
        assertLine(59, 1, 1, 1);
        assertLine(62, 1, 1, 2);
    }

    @Test
    void switchStatementWithSparseCasesAndNoDefault() {
        tested.switchStatementWithSparseCasesAndNoDefault('f');
        tested.switchStatementWithSparseCasesAndNoDefault('b');

        assertLines(65, 73, 3);
        assertLine(65, 1, 1, 2);
        assertLine(67, 1, 0, 0);
        assertLine(68, 1, 0, 0);
        assertLine(70, 1, 1, 1);
        assertLine(71, 0, 0, 0);
        assertLine(73, 1, 1, 2);
    }

    @Test
    void switchStatementWithCompactCasesAndNoDefault() {
        tested.switchStatementWithCompactCasesAndNoDefault(0);
        tested.switchStatementWithCompactCasesAndNoDefault(4);
        tested.switchStatementWithCompactCasesAndNoDefault(5);

        assertLines(76, 86, 4);
        assertLine(76, 1, 1, 3);
        assertLine(79, 1, 1, 3);
        assertLine(80, 1, 0, 0);
        assertLine(81, 1, 0, 0);
        assertLine(82, 1, 1, 1);
        assertLine(86, 1, 1, 3);
    }

    @Test
    void switchStatementWithExitInAllCases() {
        tested.switchStatementWithExitInAllCases(1);
        tested.switchStatementWithExitInAllCases(2);

        assertLines(90, 96, 3);
        assertLine(90, 1, 1, 2);
        assertLine(92, 1, 1, 1);
        assertLine(94, 1, 1, 1);
        assertLine(96, 1, 0, 0);
    }

    @Test
    void switchOnString() {
        tested.switchOnString("A", true);
        tested.switchOnString("M", false);

        assertLines(101, 105, 3);
        assertLine(101, 1, 1, 2);
        assertLine(105, 1, 1, 1);
    }
}
