/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ReferenceTypeTest {

    @Test
    void createFromTypeDescriptorForObjectType() {
        ReferenceType type = ReferenceType.createFromTypeDescriptor("Ljava/lang/String;");
        assertNotNull(type);
        assertTrue(type instanceof ObjectType);
        assertEquals("java/lang/String", type.getInternalName());
    }

    @Test
    void createFromTypeDescriptorForArrayType() {
        ReferenceType type = ReferenceType.createFromTypeDescriptor("[Ljava/lang/String;");
        assertNotNull(type);
        assertTrue(type instanceof ArrayType);
    }

    @Test
    void createFromTypeDescriptorForMethodType() {
        ReferenceType type = ReferenceType.createFromTypeDescriptor("(Ljava/lang/String;)V");
        assertNotNull(type);
        assertTrue(type instanceof MethodType);
    }

    @Test
    void createFromTypeDescriptorInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> ReferenceType.createFromTypeDescriptor("InvalidDescriptor"));
    }

    @Test
    void createFromInternalNameForObjectType() {
        ReferenceType type = ReferenceType.createFromInternalName("java/lang/String");
        assertNotNull(type);
        assertTrue(type instanceof ObjectType);
        assertEquals("java/lang/String", type.getInternalName());
    }

    @Test
    void createFromInternalNameForArrayType() {
        ReferenceType type = ReferenceType.createFromInternalName("[Ljava/lang/String;");
        assertNotNull(type);
        assertTrue(type instanceof ArrayType);
    }

    @Test
    void equalsSameInstance() {
        ObjectType type = ObjectType.create("java/lang/String");
        assertEquals(type, type);
    }

    @Test
    void equalsNullReturnsFalse() {
        ObjectType type = ObjectType.create("java/lang/String");
        assertNotEquals(null, type);
    }

    @Test
    void equalsNonReferenceTypeReturnsFalse() {
        ObjectType type = ObjectType.create("java/lang/String");
        assertFalse(type.equals("not a ReferenceType"));
    }

    @Test
    void equalsSameTypeDescriptor() {
        ObjectType type1 = ObjectType.create("java/lang/String");
        ObjectType type2 = ObjectType.create("java/lang/String");
        assertEquals(type1, type2);
    }

    @Test
    void equalsDifferentTypeDescriptor() {
        ObjectType type1 = ObjectType.create("java/lang/String");
        ObjectType type2 = ObjectType.create("java/lang/Integer");
        assertNotEquals(type1, type2);
    }

    @Test
    void equalsDifferentClass() {
        ReferenceType objType = ReferenceType.createFromInternalName("java/lang/String");
        ReferenceType arrType = ReferenceType.createFromInternalName("[Ljava/lang/String;");
        assertNotEquals(objType, arrType);
    }

    @Test
    void equalsDifferentLength() {
        ObjectType type1 = ObjectType.create("java/lang/String");
        ObjectType type2 = ObjectType.create("java/lang/Integer");
        assertNotEquals(type1, type2);
    }

    @Test
    void hashCodeConsistentWithEquals() {
        ObjectType type1 = ObjectType.create("java/lang/String");
        ObjectType type2 = ObjectType.create("java/lang/String");
        assertEquals(type1, type2);
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    void hashCodeDifferentForDifferentTypes() {
        ObjectType type1 = ObjectType.create("java/lang/String");
        ObjectType type2 = ObjectType.create("java/lang/Integer");
        assertNotEquals(type1.hashCode(), type2.hashCode());
    }

    @Test
    void getInternalName() {
        String internalName = "java/util/List";
        ReferenceType type = ReferenceType.createFromInternalName(internalName);
        assertEquals(internalName, type.getInternalName());
    }

    @Test
    void getSize() {
        ObjectType type = ObjectType.create("java/lang/String");
        assertEquals(1, type.getSize());
    }
}
