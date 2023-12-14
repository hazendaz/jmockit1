package integrationTests.otherControlStructures;

import org.junit.jupiter.api.Test;

import integrationTests.CoverageTest;

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
