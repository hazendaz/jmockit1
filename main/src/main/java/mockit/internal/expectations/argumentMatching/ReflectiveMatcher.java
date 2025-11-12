/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.argumentMatching;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Method;

import mockit.Delegate;
import mockit.internal.reflection.MethodReflection;

public final class ReflectiveMatcher implements ArgumentMatcher<ReflectiveMatcher> {
    @NonNull
    private final Delegate<?> delegate;
    @Nullable
    private Method handlerMethod;
    @Nullable
    private Object matchedValue;

    public ReflectiveMatcher(@NonNull Delegate<?> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean same(@NonNull ReflectiveMatcher other) {
        return delegate == other.delegate;
    }

    @Override
    public boolean matches(@Nullable Object argValue) {
        if (handlerMethod == null) {
            handlerMethod = MethodReflection.findNonPrivateHandlerMethod(delegate);
        }

        matchedValue = argValue;
        Boolean result = MethodReflection.invoke(delegate, handlerMethod, argValue);

        return result == null || result;
    }

    @Override
    public void writeMismatchPhrase(@NonNull ArgumentMismatch argumentMismatch) {
        if (handlerMethod != null) {
            argumentMismatch.append(handlerMethod.getName()).append('(');
            argumentMismatch.appendFormatted(matchedValue);
            argumentMismatch.append(") (should return true, was false)");
        } else {
            argumentMismatch.append('?');
        }
    }
}
