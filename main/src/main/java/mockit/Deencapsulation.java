/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mockit.internal.reflection.FieldReflection;
import mockit.internal.reflection.MethodReflection;
import mockit.internal.util.ClassLoad;

/**
 * Provides utility methods that enable access to ("de-encapsulate") otherwise non-accessible fields.
 *
 * @see #getField(Object, String)
 * @see #setField(Object, String, Object)
 */
public final class Deencapsulation {
    private Deencapsulation() {
    }

    /**
     * Gets the value of a non-accessible (eg <code>private</code>) field from a given object.
     *
     * @param objectWithField
     *            the instance from which to get the field value
     * @param fieldName
     *            the name of the field to get
     * @param <T>
     *            interface or class type to which the returned value should be assignable
     *
     * @throws IllegalArgumentException
     *             if the desired field is not found
     *
     * @see #getField(Object, Class)
     * @see #getField(Class, String)
     * @see #setField(Object, String, Object)
     */
    @Nullable
    public static <T> T getField(@Nonnull Object objectWithField, @Nonnull String fieldName) {
        return FieldReflection.getField(objectWithField.getClass(), fieldName, objectWithField);
    }

    /**
     * Gets the value of a non-accessible (eg <code>private</code>) field from a given object, <em>assuming</em> there
     * is only one field declared in the class of the given object whose type can receive values of the specified field
     * type.
     *
     * @param objectWithField
     *            the instance from which to get the field value
     * @param fieldType
     *            the declared type of the field, or a sub-type of the declared field type
     *
     * @throws IllegalArgumentException
     *             if either the desired field is not found, or more than one is
     *
     * @see #getField(Object, String)
     * @see #getField(Class, String)
     * @see #setField(Object, Object)
     */
    @Nullable
    public static <T> T getField(@Nonnull Object objectWithField, @Nonnull Class<T> fieldType) {
        return FieldReflection.getField(objectWithField.getClass(), fieldType, objectWithField);
    }

    /**
     * Gets the value of a non-accessible static field defined in a given class.
     *
     * @param classWithStaticField
     *            the class from which to get the field value
     * @param fieldName
     *            the name of the static field to get
     * @param <T>
     *            interface or class type to which the returned value should be assignable
     *
     * @throws IllegalArgumentException
     *             if the desired field is not found
     *
     * @see #getField(Class, Class)
     * @see #getField(Object, String)
     * @see #setField(Class, String, Object)
     */
    @Nullable
    public static <T> T getField(@Nonnull Class<?> classWithStaticField, @Nonnull String fieldName) {
        return FieldReflection.getField(classWithStaticField, fieldName, null);
    }

    /**
     * Gets the value of a non-accessible static field defined in a given class, <em>assuming</em> there is only one
     * field declared in the given class whose type can receive values of the specified field type.
     *
     * @param classWithStaticField
     *            the class from which to get the field value
     * @param fieldType
     *            the declared type of the field, or a sub-type of the declared field type
     * @param <T>
     *            interface or class type to which the returned value should be assignable
     *
     * @throws IllegalArgumentException
     *             if either the desired field is not found, or more than one is
     *
     * @see #getField(Class, String)
     * @see #getField(Object, Class)
     * @see #setField(Class, Object)
     */
    @Nullable
    public static <T> T getField(@Nonnull Class<?> classWithStaticField, @Nonnull Class<T> fieldType) {
        return FieldReflection.getField(classWithStaticField, fieldType, null);
    }

    /**
     * Sets the value of a non-accessible field on a given object.
     *
     * @param objectWithField
     *            the instance on which to set the field value
     * @param fieldName
     *            the name of the field to set
     * @param fieldValue
     *            the value to set the field to
     *
     * @throws IllegalArgumentException
     *             if the desired field is not found
     *
     * @see #setField(Class, String, Object)
     * @see #setField(Object, Object)
     * @see #getField(Object, String)
     */
    public static void setField(@Nonnull Object objectWithField, @Nonnull String fieldName,
            @Nullable Object fieldValue) {
        FieldReflection.setField(objectWithField.getClass(), objectWithField, fieldName, fieldValue);
    }

    /**
     * Sets the value of a non-accessible field on a given object. The field is looked up by the type of the given field
     * value instead of by name.
     *
     * @throws IllegalArgumentException
     *             if either the desired field is not found, or more than one is
     *
     * @see #setField(Object, String, Object)
     * @see #setField(Class, String, Object)
     * @see #getField(Object, String)
     */
    public static void setField(@Nonnull Object objectWithField, @Nonnull Object fieldValue) {
        FieldReflection.setField(objectWithField.getClass(), objectWithField, null, fieldValue);
    }

    /**
     * Sets the value of a non-accessible static field on a given class.
     *
     * @param classWithStaticField
     *            the class on which the static field is defined
     * @param fieldName
     *            the name of the field to set
     * @param fieldValue
     *            the value to set the field to
     *
     * @throws IllegalArgumentException
     *             if the desired field is not found
     *
     * @see #setField(Class, Object)
     * @see #setField(Object, String, Object)
     * @see #getField(Class, String)
     */
    public static void setField(@Nonnull Class<?> classWithStaticField, @Nonnull String fieldName,
            @Nullable Object fieldValue) {
        FieldReflection.setField(classWithStaticField, null, fieldName, fieldValue);
    }

    /**
     * Sets the value of a non-accessible static field on a given class. The field is looked up by the type of the given
     * field value instead of by name.
     *
     * @param classWithStaticField
     *            the class on which the static field is defined
     * @param fieldValue
     *            the value to set the field to
     *
     * @throws IllegalArgumentException
     *             if either the desired field is not found, or more than one is
     *
     * @see #setField(Class, String, Object)
     * @see #setField(Object, Object)
     * @see #getField(Class, Class)
     */
    public static void setField(@Nonnull Class<?> classWithStaticField, @Nonnull Object fieldValue) {
        FieldReflection.setField(classWithStaticField, null, null, fieldValue);
    }

    /**
     * Invokes a non-accessible (eg {@code private}) instance method from a given class with the given arguments.
     *
     * @param objectWithMethod
     *            the instance on which the invocation is to be done
     * @param methodName
     *            the name of the method to invoke
     * @param parameterTypes
     *            the types of the parameters as declared in the desired method
     * @param methodArgs
     *            zero or more parameter values for the invocation
     * @param <T>
     *            type to which the returned value should be assignable
     *
     * @return the return value from the invoked method
     *
     * @throws IllegalArgumentException
     *             if the desired method is not found
     *
     * @see #invoke(Class, String, Object...)
     */
    public static <T> T invoke(Object objectWithMethod, String methodName, Class<?>[] parameterTypes,
            Object... methodArgs) {
        Class<?> theClass = objectWithMethod.getClass();
        return MethodReflection.invoke(theClass, objectWithMethod, methodName, parameterTypes, methodArgs);
    }

    /**
     * Invokes a non-accessible (eg {@code private}) instance method from a given class with the given arguments.
     *
     * @param objectWithMethod
     *            the instance on which the invocation is to be done
     * @param methodName
     *            the name of the method to invoke
     * @param nonNullArgs
     *            zero or more non-null parameter values for the invocation; if a null value needs to be passed, the
     *            {@code Class} object for the corresponding parameter type must be passed instead
     * @param <T>
     *            type to which the returned value should be assignable
     *
     * @return the return value from the invoked method
     *
     * @throws IllegalArgumentException
     *             if the desired method is not found, or a null reference was provided for a parameter
     *
     * @see #invoke(Class, String, Object...)
     */
    public static <T> T invoke(Object objectWithMethod, String methodName, Object... nonNullArgs) {
        Class<?> theClass = objectWithMethod.getClass();
        return MethodReflection.invoke(theClass, objectWithMethod, methodName, nonNullArgs);
    }

    /**
     * Invokes a non-accessible (eg {@code private}) {@code static} method with the given arguments.
     *
     * @param classWithStaticMethod
     *            the class on which the invocation is to be done
     * @param methodName
     *            the name of the static method to invoke
     * @param parameterTypes
     *            the types of the parameters as declared in the desired method
     * @param methodArgs
     *            zero or more parameter values for the invocation
     * @param <T>
     *            type to which the returned value should be assignable
     *
     * @return the return value from the invoked method
     *
     * @see #invoke(String, String, Object...)
     */
    public static <T> T invoke(Class<?> classWithStaticMethod, String methodName, Class<?>[] parameterTypes,
            Object... methodArgs) {
        return MethodReflection.invoke(classWithStaticMethod, null, methodName, parameterTypes, methodArgs);
    }

    /**
     * Invokes a non-accessible (eg {@code private}) {@code static} method with the given arguments.
     *
     * @param classWithStaticMethod
     *            the class on which the invocation is to be done
     * @param methodName
     *            the name of the static method to invoke
     * @param nonNullArgs
     *            zero or more non-null parameter values for the invocation; if a null value needs to be passed, the
     *            {@code Class} object for the corresponding parameter type must be passed instead
     * @param <T>
     *            type to which the returned value should be assignable
     *
     * @return the return value from the invoked method
     *
     * @throws IllegalArgumentException
     *             if the desired method is not found, or a null reference was provided for a parameter
     *
     * @see #invoke(String, String, Object...)
     */
    public static <T> T invoke(Class<?> classWithStaticMethod, String methodName, Object... nonNullArgs) {
        return MethodReflection.invoke(classWithStaticMethod, null, methodName, nonNullArgs);
    }

    /**
     * Invokes a non-accessible (eg {@code private}) {@code static} method with the given arguments.
     *
     * @param classWithStaticMethod
     *            the (fully qualified) name of the class on which the invocation is to be done; must not be null
     * @param methodName
     *            the name of the static method to invoke
     * @param nonNullArgs
     *            zero or more non-null parameter values for the invocation; if a null value needs to be passed, the
     *            {@code Class} object for the corresponding parameter type must be passed instead
     * @param <T>
     *            type to which the returned value should be assignable
     *
     * @return the return value from the invoked method
     *
     * @throws IllegalArgumentException
     *             if the desired method is not found, or a null reference was provided for a parameter
     *
     * @see #invoke(Class, String, Object...)
     */
    public static <T> T invoke(String classWithStaticMethod, String methodName, Object... nonNullArgs) {
        Class<Object> theClass = ClassLoad.loadClass(classWithStaticMethod);
        return MethodReflection.invoke(theClass, null, methodName, nonNullArgs);
    }
}
