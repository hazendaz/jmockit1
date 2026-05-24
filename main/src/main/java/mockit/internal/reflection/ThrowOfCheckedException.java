/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.reflection;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.lang.reflect.InvocationTargetException;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
final class ThrowOfCheckedException {
    private static Exception exceptionToThrow;

    ThrowOfCheckedException() throws Exception {
        throw exceptionToThrow;
    }

    static synchronized void doThrow(@NonNull Exception checkedException) {
        exceptionToThrow = checkedException;
        try {
            ThrowOfCheckedException.class.getDeclaredConstructor().newInstance();
        } catch (InvocationTargetException e) {
            rethrow(e.getCause());
        } catch (ReflectiveOperationException ignore) {
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void rethrow(Throwable cause) throws T {
        throw (T) cause;
    }
}
