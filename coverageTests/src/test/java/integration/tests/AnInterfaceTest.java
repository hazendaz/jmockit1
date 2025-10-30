package integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnInterfaceTest extends CoverageTest {
    AnInterface tested;

    @BeforeEach
    void setUp() {
        tested = new AnInterface() {
            @Override
            public void doSomething(String s, boolean b) {
            }

            @Override
            public int returnValue() {
                return 0;
            }
        };
    }

    @Test
    void useAnInterface() {
        tested.doSomething("test", true);

        assertEquals(0, fileData.lineCoverageInfo.getExecutableLineCount());
        assertEquals(-1, fileData.lineCoverageInfo.getCoveragePercentage());
        assertEquals(0, fileData.lineCoverageInfo.getTotalItems());
        assertEquals(0, fileData.lineCoverageInfo.getCoveredItems());
    }
}
