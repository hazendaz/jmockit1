/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests;

import org.junit.jupiter.api.Test;

class ClassWithNestedEnumTest {
    @Test
    void useNestedEnumFromNestedClass() {
        ClassWithNestedEnum.NestedClass.useEnumFromOuterClass();
    }
}
