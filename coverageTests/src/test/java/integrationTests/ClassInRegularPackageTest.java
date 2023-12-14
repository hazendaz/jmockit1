package integrationTests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClassInRegularPackageTest {
    @Test
    void firstTest() {
        ClassInRegularPackage.NestedEnum value = ClassInRegularPackage.NestedEnum.First;
        ClassInRegularPackage obj = new ClassInRegularPackage();
        assertTrue(obj.doSomething(value));
    }

    @Test
    void secondTest() {
        assertFalse(new ClassInRegularPackage().doSomething(ClassInRegularPackage.NestedEnum.Second));
    }
}
