package integration.tests.other.control.structures;

import org.junit.jupiter.api.Test;

import integration.tests.CoverageTest;

class SwitchStatementsTest extends CoverageTest {
    final SwitchStatements tested = new SwitchStatements();

    @Test
    void switchStatementWithSparseCasesAndDefault() {
        tested.switchStatementWithSparseCasesAndDefault('A');
        tested.switchStatementWithSparseCasesAndDefault('\0');

        assertLines(5, 20, 5);
        assertLine(5, 1, 1, 2);
        assertLine(7, 1, 1, 1);
        assertLine(8, 1, 1, 1);
        assertLine(16, 1, 1, 1);
        assertLine(18, 1, 0, 0);
        assertLine(20, 1, 1, 1);
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

        assertLines(51, 57, 4);
        assertLine(52, 1, 1, 2);
        assertLine(53, 1, 1, 1);
        assertLine(54, 1, 1, 1);
        assertLine(57, 1, 1, 2);
    }

    @Test
    void switchStatementWithSparseCasesAndNoDefault() {
        tested.switchStatementWithSparseCasesAndNoDefault('f');
        tested.switchStatementWithSparseCasesAndNoDefault('b');

        assertLines(60, 68, 3);
        assertLine(60, 1, 1, 2);
        assertLine(62, 1, 0, 0);
        assertLine(63, 1, 0, 0);
        assertLine(65, 1, 1, 1);
        assertLine(66, 0, 0, 0);
        assertLine(68, 1, 1, 2);
    }

    @Test
    void switchStatementWithCompactCasesAndNoDefault() {
        tested.switchStatementWithCompactCasesAndNoDefault(0);
        tested.switchStatementWithCompactCasesAndNoDefault(4);
        tested.switchStatementWithCompactCasesAndNoDefault(5);

        assertLines(71, 81, 4);
        assertLine(71, 1, 1, 3);
        assertLine(74, 1, 1, 3);
        assertLine(75, 1, 0, 0);
        assertLine(76, 1, 0, 0);
        assertLine(77, 1, 1, 1);
        assertLine(81, 1, 1, 3);
    }

    @Test
    void switchStatementWithExitInAllCases() {
        tested.switchStatementWithExitInAllCases(1);
        tested.switchStatementWithExitInAllCases(2);

        assertLines(85, 91, 3);
        assertLine(85, 1, 1, 2);
        assertLine(87, 1, 1, 1);
        assertLine(89, 1, 1, 1);
        assertLine(91, 1, 0, 0);
    }

    @Test
    void switchOnString() {
        tested.switchOnString("A", true);
        tested.switchOnString("M", false);

        assertLines(96, 100, 3);
        assertLine(96, 1, 1, 2);
        assertLine(100, 1, 1, 1);
    }
}
