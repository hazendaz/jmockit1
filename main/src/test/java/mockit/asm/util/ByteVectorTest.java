/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ByteVectorTest {

    @Test
    void constructorDefaultSize() {
        ByteVector bv = new ByteVector();
        assertNotNull(bv.getData());
        assertEquals(0, bv.getLength());
    }

    @Test
    void constructorWithInitialSize() {
        ByteVector bv = new ByteVector(128);
        assertNotNull(bv.getData());
        assertEquals(0, bv.getLength());
    }

    @Test
    void putByteIncreasesLength() {
        ByteVector bv = new ByteVector();
        bv.putByte(42);
        assertEquals(1, bv.getLength());
        assertEquals(42, bv.getData()[0]);
    }

    @Test
    void putShortIncreasesLength() {
        ByteVector bv = new ByteVector();
        bv.putShort(0x1234);
        assertEquals(2, bv.getLength());
        assertEquals(0x12, bv.getData()[0] & 0xFF);
        assertEquals(0x34, bv.getData()[1] & 0xFF);
    }

    @Test
    void putIntIncreasesLength() {
        ByteVector bv = new ByteVector();
        bv.putInt(0x12345678);
        assertEquals(4, bv.getLength());
        assertEquals(0x12, bv.getData()[0] & 0xFF);
        assertEquals(0x34, bv.getData()[1] & 0xFF);
        assertEquals(0x56, bv.getData()[2] & 0xFF);
        assertEquals(0x78, bv.getData()[3] & 0xFF);
    }

    @Test
    void putLongIncreasesLength() {
        ByteVector bv = new ByteVector();
        bv.putLong(0x1234567890ABCDEFL);
        assertEquals(8, bv.getLength());
    }

    @Test
    void putLongValueZero() {
        ByteVector bv = new ByteVector();
        bv.putLong(0L);
        assertEquals(8, bv.getLength());
        for (int i = 0; i < 8; i++) {
            assertEquals(0, bv.getData()[i]);
        }
    }

    @Test
    void putLongMaxValue() {
        ByteVector bv = new ByteVector();
        bv.putLong(Long.MAX_VALUE);
        assertEquals(8, bv.getLength());
    }

    @Test
    void putUTF8AsciiString() {
        ByteVector bv = new ByteVector();
        bv.putUTF8("hello");
        // 2 bytes for length + 5 bytes for chars
        assertEquals(7, bv.getLength());
    }

    @Test
    void putUTF8EmptyString() {
        ByteVector bv = new ByteVector();
        bv.putUTF8("");
        // 2 bytes for length, 0 bytes for content
        assertEquals(2, bv.getLength());
        assertEquals(0, bv.getData()[0]); // length high byte
        assertEquals(0, bv.getData()[1]); // length low byte
    }

    @Test
    void putUTF8WithNonAsciiChars() {
        ByteVector bv = new ByteVector();
        // String with characters > 127 (non-ASCII)
        bv.putUTF8("\u00e9"); // é character
        // At minimum, the 2-byte length prefix is written
        assertTrue(bv.getLength() >= 2);
    }

    @Test
    void putUTF8WithHighUnicodeChars() {
        ByteVector bv = new ByteVector();
        // Character > U+07FF (3 bytes in UTF-8)
        bv.putUTF8("\u0800");
        // At minimum, the 2-byte length prefix is written
        assertTrue(bv.getLength() >= 2);
    }

    @Test
    void putUTF8TooLongThrows() {
        ByteVector bv = new ByteVector();
        // Create a string longer than 65535 characters
        StringBuilder sb = new StringBuilder(65536);
        for (int i = 0; i < 65536; i++) {
            sb.append('a');
        }
        assertThrows(IllegalArgumentException.class, () -> bv.putUTF8(sb.toString()));
    }

    @Test
    void setLengthUpdatesLength() {
        ByteVector bv = new ByteVector();
        bv.putInt(0);
        assertEquals(4, bv.getLength());
        bv.setLength(2);
        assertEquals(2, bv.getLength());
    }

    @Test
    void put11TwoBytes() {
        ByteVector bv = new ByteVector();
        bv.put11(0xAB, 0xCD);
        assertEquals(2, bv.getLength());
        assertEquals(0xAB, bv.getData()[0] & 0xFF);
        assertEquals(0xCD, bv.getData()[1] & 0xFF);
    }

    @Test
    void put12ThreeBytes() {
        ByteVector bv = new ByteVector();
        bv.put12(0xAB, 0x1234);
        assertEquals(3, bv.getLength());
        assertEquals(0xAB, bv.getData()[0] & 0xFF);
        assertEquals(0x12, bv.getData()[1] & 0xFF);
        assertEquals(0x34, bv.getData()[2] & 0xFF);
    }

    @Test
    void autoEnlargeWhenFull() {
        ByteVector bv = new ByteVector(4);
        // Put more than 4 bytes to trigger enlargement
        for (int i = 0; i < 10; i++) {
            bv.putByte(i);
        }
        assertEquals(10, bv.getLength());
    }
}
