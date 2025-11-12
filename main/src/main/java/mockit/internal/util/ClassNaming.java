/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.util;

import edu.umd.cs.findbugs.annotations.NonNull;

public final class ClassNaming {
    private ClassNaming() {
    }

    /**
     * This method was created to work around an issue in the standard {@link Class#isAnonymousClass()} method, which
     * causes a sibling nested class to be loaded when called on a nested class. If that sibling nested class is not in
     * the classpath, a <code>ClassNotFoundException</code> would result.
     * <p>
     * This method checks only the given class name, never causing any other classes to be loaded.
     */
    public static boolean isAnonymousClass(@NonNull Class<?> aClass) {
        return isAnonymousClass(aClass.getName());
    }

    public static boolean isAnonymousClass(@NonNull String className) {
        int positionJustBefore = className.lastIndexOf('$');

        if (positionJustBefore <= 0) {
            return false;
        }

        int nextPos = positionJustBefore + 1;
        int n = className.length();

        while (nextPos < n) {
            char c = className.charAt(nextPos);
            if (c < '0' || c > '9') {
                return false;
            }
            nextPos++;
        }

        return true;
    }
}
