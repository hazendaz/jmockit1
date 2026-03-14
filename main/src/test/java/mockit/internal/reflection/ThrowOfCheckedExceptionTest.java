/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.reflection;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

final class ThrowOfCheckedExceptionTest {

    @Test
    void doThrowThrowsCheckedException() {
        // ThrowOfCheckedException.doThrow should throw the given checked exception
        assertThrows(IOException.class, () -> {
            try {
                ThrowOfCheckedException.doThrow(new IOException("test checked exception"));
            } catch (Exception e) {
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw e;
            }
        });
    }

    @Test
    void doThrowWithRuntimeExceptionSubclass() {
        // Test with a non-runtime checked exception
        assertThrows(Exception.class, () -> ThrowOfCheckedException.doThrow(new Exception("generic")));
    }
}
