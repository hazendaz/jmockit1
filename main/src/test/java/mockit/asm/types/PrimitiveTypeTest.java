/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.types;

import static mockit.asm.jvmConstants.Opcodes.DCONST_0;
import static mockit.asm.jvmConstants.Opcodes.DLOAD;
import static mockit.asm.jvmConstants.Opcodes.FCONST_0;
import static mockit.asm.jvmConstants.Opcodes.FLOAD;
import static mockit.asm.jvmConstants.Opcodes.IALOAD;
import static mockit.asm.jvmConstants.Opcodes.IASTORE;
import static mockit.asm.jvmConstants.Opcodes.ICONST_0;
import static mockit.asm.jvmConstants.Opcodes.ILOAD;
import static mockit.asm.jvmConstants.Opcodes.LCONST_0;
import static mockit.asm.jvmConstants.Opcodes.LLOAD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import mockit.asm.jvmConstants.ArrayElementType;

import org.junit.jupiter.api.Test;

final class PrimitiveTypeTest {

    @Test
    void getPrimitiveTypeByTypeCodeForInt() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('I');
        assertNotNull(type);
        assertEquals('I', type.getTypeCode());
        assertEquals(int.class, type.getType());
        assertEquals("int", type.getClassName());
    }

    @Test
    void getPrimitiveTypeByTypeCodeForBoolean() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('Z');
        assertNotNull(type);
        assertEquals('Z', type.getTypeCode());
        assertEquals(boolean.class, type.getType());
        assertEquals("boolean", type.getClassName());
    }

    @Test
    void getPrimitiveTypeByTypeCodeForChar() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('C');
        assertNotNull(type);
        assertEquals('C', type.getTypeCode());
        assertEquals(char.class, type.getType());
        assertEquals("char", type.getClassName());
    }

    @Test
    void getPrimitiveTypeByTypeCodeForByte() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('B');
        assertNotNull(type);
        assertEquals('B', type.getTypeCode());
        assertEquals(byte.class, type.getType());
        assertEquals("byte", type.getClassName());
    }

    @Test
    void getPrimitiveTypeByTypeCodeForShort() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('S');
        assertNotNull(type);
        assertEquals('S', type.getTypeCode());
        assertEquals(short.class, type.getType());
        assertEquals("short", type.getClassName());
    }

    @Test
    void getPrimitiveTypeByTypeCodeForFloat() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('F');
        assertNotNull(type);
        assertEquals('F', type.getTypeCode());
        assertEquals(float.class, type.getType());
        assertEquals("float", type.getClassName());
    }

    @Test
    void getPrimitiveTypeByTypeCodeForLong() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('J');
        assertNotNull(type);
        assertEquals('J', type.getTypeCode());
        assertEquals(long.class, type.getType());
        assertEquals("long", type.getClassName());
    }

    @Test
    void getPrimitiveTypeByTypeCodeForDouble() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('D');
        assertNotNull(type);
        assertEquals('D', type.getTypeCode());
        assertEquals(double.class, type.getType());
        assertEquals("double", type.getClassName());
    }

    @Test
    void getPrimitiveTypeByTypeCodeForVoid() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('V');
        assertNotNull(type);
        assertEquals('V', type.getTypeCode());
        assertEquals(void.class, type.getType());
        assertEquals("void", type.getClassName());
    }

    @Test
    void getPrimitiveTypeByTypeCodeReturnsNullForInvalidCode() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('X');
        assertNull(type);
    }

    @Test
    void getPrimitiveTypeByClassForInt() {
        PrimitiveType type = PrimitiveType.getPrimitiveType(int.class);
        assertNotNull(type);
        assertEquals('I', type.getTypeCode());
    }

    @Test
    void getPrimitiveTypeByClassForBoolean() {
        PrimitiveType type = PrimitiveType.getPrimitiveType(boolean.class);
        assertNotNull(type);
        assertEquals('Z', type.getTypeCode());
    }

    @Test
    void getPrimitiveTypeByClassForLong() {
        PrimitiveType type = PrimitiveType.getPrimitiveType(long.class);
        assertNotNull(type);
        assertEquals('J', type.getTypeCode());
    }

    @Test
    void getPrimitiveTypeByClassForDouble() {
        PrimitiveType type = PrimitiveType.getPrimitiveType(double.class);
        assertNotNull(type);
        assertEquals('D', type.getTypeCode());
    }

    @Test
    void getWrapperTypeDescForInt() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('I');
        assertNotNull(type);
        assertEquals("java/lang/Integer", type.getWrapperTypeDesc());
    }

    @Test
    void getWrapperTypeDescForBoolean() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('Z');
        assertNotNull(type);
        assertEquals("java/lang/Boolean", type.getWrapperTypeDesc());
    }

    @Test
    void getWrapperTypeDescForVoid() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('V');
        assertNotNull(type);
        assertEquals("java/lang/Void", type.getWrapperTypeDesc());
    }

    @Test
    void getCorrespondingPrimitiveTypeIfWrapperTypeForInteger() {
        PrimitiveType type = PrimitiveType.getCorrespondingPrimitiveTypeIfWrapperType("java/lang/Integer");
        assertNotNull(type);
        assertEquals('I', type.getTypeCode());
    }

    @Test
    void getCorrespondingPrimitiveTypeIfWrapperTypeForBoolean() {
        PrimitiveType type = PrimitiveType.getCorrespondingPrimitiveTypeIfWrapperType("java/lang/Boolean");
        assertNotNull(type);
        assertEquals('Z', type.getTypeCode());
    }

    @Test
    void getCorrespondingPrimitiveTypeIfWrapperTypeReturnsNullForNonWrapper() {
        PrimitiveType type = PrimitiveType.getCorrespondingPrimitiveTypeIfWrapperType("java/lang/String");
        assertNull(type);
    }

    @Test
    void getTypeByTypeCodeForInt() {
        Class<?> type = PrimitiveType.getType('I');
        assertEquals(int.class, type);
    }

    @Test
    void getArrayElementTypeForBoolean() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('Z');
        assertNotNull(type);
        assertEquals(ArrayElementType.BOOLEAN, PrimitiveType.getArrayElementType(type));
    }

    @Test
    void getArrayElementTypeForInt() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('I');
        assertNotNull(type);
        assertEquals(ArrayElementType.INT, PrimitiveType.getArrayElementType(type));
    }

    @Test
    void getSizeForIntIsOne() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('I');
        assertNotNull(type);
        assertEquals(1, type.getSize());
    }

    @Test
    void getSizeForLongIsTwo() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('J');
        assertNotNull(type);
        assertEquals(2, type.getSize());
    }

    @Test
    void getSizeForDoubleIsTwo() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('D');
        assertNotNull(type);
        assertEquals(2, type.getSize());
    }

    @Test
    void getSizeForVoidIsZero() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('V');
        assertNotNull(type);
        assertEquals(0, type.getSize());
    }

    @Test
    void getLoadOpcodeForInt() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('I');
        assertNotNull(type);
        assertEquals(ILOAD, type.getLoadOpcode());
    }

    @Test
    void getLoadOpcodeForFloat() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('F');
        assertNotNull(type);
        assertEquals(FLOAD, type.getLoadOpcode());
    }

    @Test
    void getLoadOpcodeForLong() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('J');
        assertNotNull(type);
        assertEquals(LLOAD, type.getLoadOpcode());
    }

    @Test
    void getLoadOpcodeForDouble() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('D');
        assertNotNull(type);
        assertEquals(DLOAD, type.getLoadOpcode());
    }

    @Test
    void getLoadOpcodeForVoidIsZero() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('V');
        assertNotNull(type);
        assertEquals(0, type.getLoadOpcode());
    }

    @Test
    void getConstOpcodeForInt() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('I');
        assertNotNull(type);
        assertEquals(ICONST_0, type.getConstOpcode());
    }

    @Test
    void getConstOpcodeForFloat() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('F');
        assertNotNull(type);
        assertEquals(FCONST_0, type.getConstOpcode());
    }

    @Test
    void getConstOpcodeForLong() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('J');
        assertNotNull(type);
        assertEquals(LCONST_0, type.getConstOpcode());
    }

    @Test
    void getConstOpcodeForDouble() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('D');
        assertNotNull(type);
        assertEquals(DCONST_0, type.getConstOpcode());
    }

    @Test
    void equalsSameInstance() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('I');
        assertEquals(type, type);
    }

    @Test
    void equalsNullReturnsFalse() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('I');
        assertNotNull(type);
        assertFalse(type.equals(null));
    }

    @Test
    void equalsNonPrimitiveTypeReturnsFalse() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('I');
        assertNotNull(type);
        assertFalse(type.equals("not a PrimitiveType"));
    }

    @Test
    void equalsSamePrimitiveType() {
        PrimitiveType type1 = PrimitiveType.getPrimitiveType('I');
        PrimitiveType type2 = PrimitiveType.getPrimitiveType('I');
        assertEquals(type1, type2);
    }

    @Test
    void equalsDifferentPrimitiveType() {
        PrimitiveType type1 = PrimitiveType.getPrimitiveType('I');
        PrimitiveType type2 = PrimitiveType.getPrimitiveType('J');
        assertNotEquals(type1, type2);
    }

    @Test
    void hashCodeConsistentWithEquals() {
        PrimitiveType type1 = PrimitiveType.getPrimitiveType('I');
        PrimitiveType type2 = PrimitiveType.getPrimitiveType('I');
        assertNotNull(type1);
        assertNotNull(type2);
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    void hashCodeDifferentForDifferentTypes() {
        PrimitiveType type1 = PrimitiveType.getPrimitiveType('I');
        PrimitiveType type2 = PrimitiveType.getPrimitiveType('J');
        assertNotNull(type1);
        assertNotNull(type2);
        assertNotEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    void getDescriptorForInt() {
        PrimitiveType type = PrimitiveType.getPrimitiveType('I');
        assertNotNull(type);
        assertEquals("I", type.getDescriptor());
    }

    @Test
    void getOpcodeWithIALOAD() {
        PrimitiveType intType = PrimitiveType.getPrimitiveType('I');
        assertNotNull(intType);
        assertEquals(IALOAD, intType.getOpcode(IALOAD));
    }

    @Test
    void getOpcodeWithIASTORE() {
        PrimitiveType intType = PrimitiveType.getPrimitiveType('I');
        assertNotNull(intType);
        assertEquals(IASTORE, intType.getOpcode(IASTORE));
    }
}
