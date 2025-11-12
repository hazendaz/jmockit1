/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.argumentMatching;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Array;

public class EqualityMatcher implements ArgumentMatcher<EqualityMatcher> {
    @Nullable
    final Object object;

    EqualityMatcher(@Nullable Object equalArg) {
        object = equalArg;
    }

    @Override
    public final boolean same(@NonNull EqualityMatcher other) {
        return object == other.object;
    }

    @Override
    public boolean matches(@Nullable Object argValue) {
        return areEqual(argValue, object);
    }

    @Override
    public void writeMismatchPhrase(@NonNull ArgumentMismatch argumentMismatch) {
        argumentMismatch.appendFormatted(object);
    }

    public static boolean areEqual(@Nullable Object o1, @Nullable Object o2) {
        if (o1 == null) {
            return o2 == null;
        }

        return o2 != null && (o1 == o2 || areEqualWhenNonNull(o1, o2));
    }

    public static boolean areEqualWhenNonNull(@NonNull Object o1, @NonNull Object o2) {
        if (isArray(o1)) {
            return isArray(o2) && areArraysEqual(o1, o2);
        }

        return o1.equals(o2);
    }

    private static boolean isArray(@NonNull Object o) {
        return o.getClass().isArray();
    }

    private static boolean areArraysEqual(@NonNull Object array1, @NonNull Object array2) {
        int length1 = Array.getLength(array1);

        if (length1 != Array.getLength(array2)) {
            return false;
        }

        for (int i = 0; i < length1; i++) {
            Object value1 = Array.get(array1, i);
            Object value2 = Array.get(array2, i);

            if (!areEqual(value1, value2)) {
                return false;
            }
        }

        return true;
    }
}
