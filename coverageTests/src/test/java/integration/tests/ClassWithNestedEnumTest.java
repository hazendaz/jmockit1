package integration.tests;

import org.junit.jupiter.api.Test;

class ClassWithNestedEnumTest {
    @Test
    void useNestedEnumFromNestedClass() {
        ClassWithNestedEnum.NestedClass.useEnumFromOuterClass();
    }
}
