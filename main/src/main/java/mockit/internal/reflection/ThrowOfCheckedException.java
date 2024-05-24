/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
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
