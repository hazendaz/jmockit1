package integration.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClassInRegularPackageTest {
    @Test
    void firstTest() {
        ClassInRegularPackage.NestedEnum value = ClassInRegularPackage.NestedEnum.FIRST;
        ClassInRegularPackage obj = new ClassInRegularPackage();
        assertTrue(obj.doSomething(value));
    }

    @Test
    void secondTest() {
        assertFalse(new ClassInRegularPackage().doSomething(ClassInRegularPackage.NestedEnum.SECOND));
    }
}
