/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.argumentMatching;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Map;

public final class LenientEqualityMatcher extends EqualityMatcher {
    @NonNull
    private final Map<Object, Object> instanceMap;

    public LenientEqualityMatcher(@Nullable Object equalArg, @NonNull Map<Object, Object> instanceMap) {
        super(equalArg);
        this.instanceMap = instanceMap;
    }

    @Override
    public boolean matches(@Nullable Object argValue) {
        if (argValue == null) {
            return object == null;
        }
        if (object == null) {
            return false;
        }
        if (argValue == object || instanceMap.get(argValue) == object) {
            return true;
        }

        return areEqualWhenNonNull(argValue, object);
    }
}
