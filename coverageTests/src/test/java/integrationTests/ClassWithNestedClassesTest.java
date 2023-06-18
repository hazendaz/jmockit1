package integrationTests;

import static org.junit.Assert.*;

import org.junit.*;

public final class ClassWithNestedClassesTest extends CoverageTest {
    final ClassWithNestedClasses tested = null;

    @Test
    public void exerciseNestedClasses() {
        ClassWithNestedClasses.doSomething();
        ClassWithNestedClasses.methodContainingAnonymousClass(1);

        assertEquals(12, fileData.lineCoverageInfo.getExecutableLineCount());
        assertEquals(71, fileData.lineCoverageInfo.getCoveragePercentage());
        assertEquals(14, fileData.lineCoverageInfo.getTotalItems());
        assertEquals(10, fileData.lineCoverageInfo.getCoveredItems());
    }
}
