/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.argumentMatching;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Method;

import mockit.internal.util.ObjectMethods;

public final class ArgumentMismatch {
    @NonNull
    private final StringBuilder out = new StringBuilder(50);
    @Nullable
    private String parameterType;

    @Nullable
    public String getParameterType() {
        return parameterType;
    }

    @Override
    public String toString() {
        return out.toString();
    }

    @NonNull
    public ArgumentMismatch append(char c) {
        out.append(c);
        return this;
    }

    @NonNull
    public ArgumentMismatch append(int i) {
        out.append(i);
        return this;
    }

    @NonNull
    public ArgumentMismatch append(double d) {
        out.append(d);
        return this;
    }

    @NonNull
    public ArgumentMismatch append(@Nullable CharSequence str) {
        out.append(str);
        return this;
    }

    public void appendFormatted(@Nullable String parameterTypeName, @Nullable Object argumentValue,
            @Nullable ArgumentMatcher<?> matcher) {
        if (matcher == null) {
            appendFormatted(argumentValue);
        } else {
            parameterType = parameterTypeName;
            matcher.writeMismatchPhrase(this);
        }
    }

    @SuppressWarnings("OverlyComplexMethod")
    void appendFormatted(@Nullable Object value) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof CharSequence) {
            appendCharacters((CharSequence) value);
        } else if (value instanceof Character) {
            out.append('\'');
            appendEscapedOrPlainCharacter('\'', (Character) value);
            out.append('\'');
        } else if (value instanceof Byte) {
            out.append(value).append('b');
        } else if (value instanceof Short) {
            out.append(value).append('s');
        } else if (value instanceof Long) {
            out.append(value).append('L');
        } else if (value instanceof Float) {
            out.append(value).append('F');
        } else if (value instanceof Number || value instanceof Boolean) {
            out.append(value);
        } else if (value.getClass().isArray()) {
            appendArray(value);
        } else if (value instanceof ArgumentMatcher) {
            ((ArgumentMatcher<?>) value).writeMismatchPhrase(this);
        } else {
            appendArbitraryArgument(value);
        }
    }

    private void appendArray(@NonNull Object array) {
        out.append('[');
        String separator = "";

        for (int i = 0, n = Array.getLength(array); i < n; i++) {
            Object nextValue = Array.get(array, i);
            out.append(separator);
            appendFormatted(nextValue);
            separator = ", ";
        }

        out.append(']');
    }

    private void appendCharacters(@NonNull CharSequence characters) {
        out.append('"');

        for (int i = 0, n = characters.length(); i < n; i++) {
            char c = characters.charAt(i);
            appendEscapedOrPlainCharacter('"', c);
        }

        out.append('"');
    }

    private void appendEscapedOrPlainCharacter(char quoteCharacter, char c) {
        if (c == quoteCharacter) {
            out.append('\\').append(c);
        } else {
            switch (c) {
                case '\t':
                    out.append("\\t");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                default:
                    out.append(c);
            }
        }
    }

    private void appendArbitraryArgument(@NonNull Object value) {
        Class<?> valueClass = value.getClass();

        Method toStringMethod;
        try {
            toStringMethod = valueClass.getMethod("toString");
        } catch (NoSuchMethodException ignored) {
            return;
        }

        if (toStringMethod.getDeclaringClass() == Object.class) {
            out.append(value);
        } else {
            String valueAsString = value.toString();

            if (valueAsString != null && !valueAsString.isEmpty()) {
                appendCharacters(valueAsString);
            } else {
                out.append(ObjectMethods.objectIdentity(value));
            }
        }
    }

    public void appendFormatted(@NonNull Object[] values) {
        String separator = "";

        for (Object value : values) {
            append(separator).appendFormatted(value);
            separator = ", ";
        }
    }
}
