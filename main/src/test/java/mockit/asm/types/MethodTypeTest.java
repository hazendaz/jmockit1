/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class MethodTypeTest {

    @Test
    void createFromMethodDescriptor() {
        MethodType type = MethodType.create("(Ljava/lang/String;I)V");
        assertNotNull(type);
    }

    @Test
    void getInternalName() {
        MethodType type = MethodType.create("(I)V");
        assertEquals("(I)V", type.getInternalName());
    }

    @Test
    void getClassNameThrows() {
        MethodType type = MethodType.create("(I)V");
        assertThrows(UnsupportedOperationException.class, type::getClassName);
    }

    @Test
    void getSizeThrows() {
        MethodType type = MethodType.create("(I)V");
        assertThrows(UnsupportedOperationException.class, type::getSize);
    }

    @Test
    void getOpcodeThrows() {
        MethodType type = MethodType.create("(I)V");
        assertThrows(UnsupportedOperationException.class, () -> type.getOpcode(0));
    }
}
