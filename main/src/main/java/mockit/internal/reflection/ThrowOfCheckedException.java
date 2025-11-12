/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.reflection;

import edu.umd.cs.findbugs.annotations.NonNull;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
final class ThrowOfCheckedException {
    private static Exception exceptionToThrow;

    ThrowOfCheckedException() throws Exception {
        throw exceptionToThrow;
    }

    @SuppressWarnings("deprecation")
    static synchronized void doThrow(@NonNull Exception checkedException) {
        exceptionToThrow = checkedException;
        try {
            ThrowOfCheckedException.class.newInstance();
        } catch (InstantiationException | IllegalAccessException ignore) {
        }
    }
}
