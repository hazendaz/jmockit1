package integration.tests.other.control.structures;

import org.junit.jupiter.api.Test;

import integration.tests.CoverageTest;

class TryCatchFinallyStatementsTest extends CoverageTest {
    TryCatchFinallyStatements tested;

    @Test
    void tryCatch() {
        tested.tryCatch();
    }

    @Test
    void tryCatchWhichThrowsAndCatchesException() {
        tested.tryCatchWhichThrowsAndCatchesException();
    }
}
