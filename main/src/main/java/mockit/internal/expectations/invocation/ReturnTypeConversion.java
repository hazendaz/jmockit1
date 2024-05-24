/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.invocation;

import static java.util.Collections.singletonList;

import static mockit.internal.reflection.ConstructorReflection.newInstanceUsingPublicConstructorIfAvailable;
import static mockit.internal.reflection.MethodReflection.JAVA_LANG;
import static mockit.internal.reflection.MethodReflection.invokePublicIfAvailable;
import static mockit.internal.util.Utilities.JAVA8;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Stream;

import mockit.internal.util.AutoBoxing;
import mockit.internal.util.MethodFormatter;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class ReturnTypeConversion {
    private static final Class<?>[] STRING = { String.class };

    @NonNull
    private ExpectedInvocation invocation;
    @Nullable
    private final InvocationResults invocationResults;
    @NonNull
    private final Class<?> returnType;
    @NonNull
    private final Object valueToReturn;

    public ReturnTypeConversion(@NonNull ExpectedInvocation invocation, @NonNull InvocationResults invocationResults,
            @NonNull Class<?> returnType, @NonNull Object value) {
        this.invocation = invocation;
        this.invocationResults = invocationResults;
        this.returnType = returnType;
        valueToReturn = value;
    }

    ReturnTypeConversion(@NonNull ExpectedInvocation invocation, @NonNull Class<?> returnType, @NonNull Object value) {
        this.invocation = invocation;
        invocationResults = null;
        this.returnType = returnType;
        valueToReturn = value;
    }

    @NonNull
    Object getConvertedValue() {
        Class<?> wrapperType = getWrapperType();
        Class<?> valueType = valueToReturn.getClass();

        if (valueType == wrapperType) {
            return valueToReturn;
        }

        if (wrapperType != null && AutoBoxing.isWrapperOfPrimitiveType(valueType)) {
            return getPrimitiveValueConvertingAsNeeded(wrapperType);
        }

        throw newIncompatibleTypesException();
    }

    @Nullable
    private Class<?> getWrapperType() {
        return AutoBoxing.isWrapperOfPrimitiveType(returnType) ? returnType : AutoBoxing.getWrapperType(returnType);
    }

    public void addConvertedValue() {
        Class<?> wrapperType = getWrapperType();
        Class<?> valueType = valueToReturn.getClass();

        if (valueType == wrapperType) {
            addReturnValue(valueToReturn);
        } else if (wrapperType != null && AutoBoxing.isWrapperOfPrimitiveType(valueType)) {
            addPrimitiveValueConvertingAsNeeded(wrapperType);
        } else {
            boolean valueIsArray = valueType.isArray();

            if (valueIsArray || valueToReturn instanceof Iterable<?> || valueToReturn instanceof Iterator<?>) {
                assert invocationResults != null;
                new MultiValuedConversion(invocationResults, returnType, valueToReturn)
                        .addMultiValuedResultBasedOnTheReturnType(valueIsArray);
            } else if (wrapperType != null) {
                throw newIncompatibleTypesException();
            } else {
                addResultFromSingleValue();
            }
        }
    }

    private void addReturnValue(@NonNull Object returnValue) {
        assert invocationResults != null;
        invocationResults.addReturnValueResult(returnValue);
    }

    private void addPrimitiveValueConvertingAsNeeded(@NonNull Class<?> targetType) {
        Object convertedValue = getPrimitiveValueConvertingAsNeeded(targetType);
        addReturnValue(convertedValue);
    }

    private void addResultFromSingleValue() {
        if (returnType == Object.class) {
            addReturnValue(valueToReturn);
        } else if (returnType == void.class) {
            throw newIncompatibleTypesException();
        } else if (addByteArrayIfApplicable()) {
            // Do nothing
        } else if (returnType.isArray()) {
            addArray();
        } else if (returnType.isAssignableFrom(ListIterator.class)) {
            addListIterator();
        } else if (addCollectionWithSingleElement() || JAVA8 && addJava8ObjectIfApplicable()) {
            // Do nothing
        } else if (valueToReturn instanceof CharSequence) {
            addCharSequence((CharSequence) valueToReturn);
        } else {
            addPrimitiveValue();
        }
    }

    @NonNull
    private IllegalArgumentException newIncompatibleTypesException() {
        String valueTypeName = JAVA_LANG.matcher(valueToReturn.getClass().getName()).replaceAll("");
        String returnTypeName = JAVA_LANG.matcher(returnType.getName()).replaceAll("");

        MethodFormatter methodDesc = new MethodFormatter(invocation.getClassDesc(),
                invocation.getMethodNameAndDescription());
        String msg = "Value of type " + valueTypeName + " incompatible with return type " + returnTypeName + " of "
                + methodDesc;

        return new IllegalArgumentException(msg);
    }

    private void addArray() {
        Object array = Array.newInstance(returnType.getComponentType(), 1);
        Array.set(array, 0, valueToReturn);
        addReturnValue(array);
    }

    private void addListIterator() {
        List<Object> l = new ArrayList<>(1);
        l.add(valueToReturn);
        ListIterator<Object> iterator = l.listIterator();
        addReturnValue(iterator);
    }

    private void addCharSequence(@NonNull CharSequence textualValue) {
        @NonNull
        Object convertedValue = textualValue;

        if (returnType.isAssignableFrom(ByteArrayInputStream.class)) {
            // noinspection resource
            convertedValue = new ByteArrayInputStream(textualValue.toString().getBytes());
        } else if (returnType.isAssignableFrom(StringReader.class)) {
            // noinspection resource
            convertedValue = new StringReader(textualValue.toString());
        } else if (!(textualValue instanceof StringBuilder) && returnType.isAssignableFrom(StringBuilder.class)) {
            convertedValue = new StringBuilder(textualValue);
        } else if (!(textualValue instanceof CharBuffer) && returnType.isAssignableFrom(CharBuffer.class)) {
            convertedValue = CharBuffer.wrap(textualValue);
        } else {
            Object valueFromText = newInstanceUsingPublicConstructorIfAvailable(returnType, STRING, textualValue);

            if (valueFromText != null) {
                convertedValue = valueFromText;
            }
        }

        addReturnValue(convertedValue);
    }

    private boolean addByteArrayIfApplicable() {
        if (returnType == byte[].class && valueToReturn instanceof CharSequence) {
            addReturnValue(valueToReturn.toString().getBytes());
            return true;
        }

        return false;
    }

    private boolean addCollectionWithSingleElement() {
        Collection<Object> container = null;

        if (returnType.isAssignableFrom(ArrayList.class)) {
            container = new ArrayList<>(1);
        } else if (returnType.isAssignableFrom(LinkedList.class)) {
            container = new LinkedList<>();
        } else if (returnType.isAssignableFrom(HashSet.class)) {
            container = new HashSet<>(1);
        } else if (returnType.isAssignableFrom(TreeSet.class)) {
            container = new TreeSet<>();
        }

        if (container != null) {
            container.add(valueToReturn);
            addReturnValue(container);
            return true;
        }

        return false;
    }

    @SuppressWarnings("Since15")
    private boolean addJava8ObjectIfApplicable() {
        if (returnType == Optional.class) {
            addReturnValue(Optional.of(valueToReturn));
            return true;
        }
        if (returnType.isAssignableFrom(Stream.class)) {
            addReturnValue(singletonList(valueToReturn).stream());
            return true;
        }

        return false;
    }

    private void addPrimitiveValue() {
        Class<?> primitiveType = AutoBoxing.getPrimitiveType(valueToReturn.getClass());

        if (primitiveType != null) {
            Class<?>[] parameterType = { primitiveType };
            Object convertedValue = newInstanceUsingPublicConstructorIfAvailable(returnType, parameterType,
                    valueToReturn);

            if (convertedValue == null) {
                convertedValue = invokePublicIfAvailable(returnType, null, "valueOf", parameterType, valueToReturn);
            }

            if (convertedValue != null) {
                addReturnValue(convertedValue);
                return;
            }
        }

        throw newIncompatibleTypesException();
    }

    @NonNull
    private Object getPrimitiveValueConvertingAsNeeded(@NonNull Class<?> targetType) {
        Object convertedValue = null;

        if (valueToReturn instanceof Number) {
            convertedValue = convertFromNumber(targetType, (Number) valueToReturn);
        } else if (valueToReturn instanceof Character) {
            convertedValue = convertFromChar(targetType, (Character) valueToReturn);
        }

        if (convertedValue == null) {
            throw newIncompatibleTypesException();
        }

        return convertedValue;
    }

    @Nullable
    private static Object convertFromNumber(@NonNull Class<?> targetType, @NonNull Number number) {
        if (targetType == Integer.class) {
            return number.intValue();
        }

        if (targetType == Short.class) {
            return number.shortValue();
        }

        if (targetType == Long.class) {
            return number.longValue();
        }

        if (targetType == Byte.class) {
            return number.byteValue();
        }

        if (targetType == Double.class) {
            return number.doubleValue();
        }

        if (targetType == Float.class) {
            return number.floatValue();
        }

        if (targetType == Character.class) {
            return (char) number.intValue();
        }

        return null;
    }

    @Nullable
    private static Object convertFromChar(@NonNull Class<?> targetType, char c) {
        if (targetType == Integer.class) {
            return (int) c;
        }

        if (targetType == Short.class) {
            return (short) c;
        }

        if (targetType == Long.class) {
            return (long) c;
        }

        if (targetType == Byte.class) {
            // noinspection NumericCastThatLosesPrecision
            return (byte) c;
        }

        if (targetType == Double.class) {
            return (double) c;
        }

        if (targetType == Float.class) {
            return (float) c;
        }

        return null;
    }
}
