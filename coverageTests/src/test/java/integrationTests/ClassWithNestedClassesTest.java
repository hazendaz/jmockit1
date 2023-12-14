package integrationTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ClassWithNestedClassesTest extends CoverageTest {
    final ClassWithNestedClasses tested = null;

    @Test
    void exerciseNestedClasses() {
        ClassWithNestedClasses.doSomething();
        ClassWithNestedClasses.methodContainingAnonymousClass(1);

        assertEquals(16, fileData.lineCoverageInfo.getExecutableLineCount());
        assertEquals(55, fileData.lineCoverageInfo.getCoveragePercentage());
        assertEquals(20, fileData.lineCoverageInfo.getTotalItems());
        assertEquals(11, fileData.lineCoverageInfo.getCoveredItems());
    }
}
