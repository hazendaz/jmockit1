package integration.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClassWithNestedClassesTest extends CoverageTest {
    final ClassWithNestedClasses tested = null;

    @Test
    void exerciseNestedClasses() {
        ClassWithNestedClasses.doSomething();
        ClassWithNestedClasses.methodContainingAnonymousClass(1);

        assertEquals(17, fileData.lineCoverageInfo.getExecutableLineCount());
        assertEquals(57, fileData.lineCoverageInfo.getCoveragePercentage());
        assertEquals(21, fileData.lineCoverageInfo.getTotalItems());
        assertEquals(12, fileData.lineCoverageInfo.getCoveredItems());
    }
}
