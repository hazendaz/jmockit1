package integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClassWithReferenceToNestedClassTest extends CoverageTest {
    final ClassWithReferenceToNestedClass tested = null;

    @Test
    void exerciseOnePathOfTwo() {
        ClassWithReferenceToNestedClass.doSomething();

        assertEquals(4, fileData.lineCoverageInfo.getExecutableLineCount());
        assertEquals(25, fileData.lineCoverageInfo.getCoveragePercentage());
        assertEquals(4, fileData.lineCoverageInfo.getTotalItems());
        assertEquals(1, fileData.lineCoverageInfo.getCoveredItems());
    }
}
