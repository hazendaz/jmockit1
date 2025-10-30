package integrationTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AnEnumTest extends CoverageTest {
    AnEnum tested;

    @Test
    void useAnEnum() {
        tested = AnEnum.ONE_VALUE;

        assertEquals(100, fileData.lineCoverageInfo.getCoveragePercentage());
    }
}
