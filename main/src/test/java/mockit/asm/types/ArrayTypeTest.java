/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

final class ArrayTypeTest {

    @Test
    void createFromStringDescriptor() {
        ArrayType type = ArrayType.create("[Ljava/lang/String;");
        assertNotNull(type);
    }

    @Test
    void getDimensionsForSingleDimension() {
        ArrayType type = ArrayType.create("[Ljava/lang/String;");
        assertEquals(1, type.getDimensions());
    }

    @Test
    void getDimensionsForMultiDimension() {
        ArrayType type = ArrayType.create("[[I");
        assertEquals(2, type.getDimensions());
    }

    @Test
    void getElementTypeForObjectArray() {
        ArrayType type = ArrayType.create("[Ljava/lang/String;");
        JavaType elementType = type.getElementType();
        assertNotNull(elementType);
    }

    @Test
    void getElementTypeForPrimitiveArray() {
        ArrayType type = ArrayType.create("[I");
        JavaType elementType = type.getElementType();
        assertNotNull(elementType);
    }

    @Test
    void getClassNameForObjectArray() {
        ArrayType type = ArrayType.create("[Ljava/lang/String;");
        String className = type.getClassName();
        assertEquals("java.lang.String[]", className);
    }

    @Test
    void getClassNameForMultiDimensionalPrimitiveArray() {
        ArrayType type = ArrayType.create("[[I");
        String className = type.getClassName();
        assertEquals("int[][]", className);
    }

    @Test
    void getClassNameFor2DObjectArray() {
        ArrayType type = ArrayType.create("[[Ljava/lang/Integer;");
        String className = type.getClassName();
        assertEquals("java.lang.Integer[][]", className);
    }
}
