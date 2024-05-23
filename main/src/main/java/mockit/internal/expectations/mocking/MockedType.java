/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import static java.lang.reflect.Modifier.isFinal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import javax.annotation.Nullable;

import mockit.Capturing;
import mockit.Injectable;
import mockit.Mocked;
import mockit.internal.expectations.MockingFilters;
import mockit.internal.expectations.state.CascadingTypes;
import mockit.internal.expectations.state.ExecutingTest;
import mockit.internal.injection.InjectionProvider;
import mockit.internal.reflection.FieldReflection;
import mockit.internal.state.ParameterNames;
import mockit.internal.state.TestRun;
import mockit.internal.util.DefaultValues;
import mockit.internal.util.TestMethod;
import mockit.internal.util.TypeConversion;
import mockit.internal.util.Utilities;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("EqualsAndHashcode")
public final class MockedType extends InjectionProvider {
    @Mocked
    private static final Object DUMMY = null;
    private static final int DUMMY_HASHCODE;

    static {
        int h = 0;

        try {
            Field dummyField = MockedType.class.getDeclaredField("DUMMY");
            Mocked mocked = dummyField.getAnnotation(Mocked.class);
            h = mocked.hashCode();
        } catch (NoSuchFieldException ignore) {
        }

        DUMMY_HASHCODE = h;
    }

    @Nullable
    public final Field field;
    final boolean fieldFromTestClass;
    private final int accessModifiers;
    @Nullable
    private final Mocked mocked;
    @Nullable
    private final Capturing capturing;
    @Nullable
    private final Class<?> parameterImplementationClass;
    public final boolean injectable;
    @Nullable
    Object providedValue;

    public MockedType(@NonNull Field field) {
        super(field.getGenericType(), field.getName());
        this.field = field;
        fieldFromTestClass = true;
        accessModifiers = field.getModifiers();
        mocked = field.getAnnotation(Mocked.class);
        capturing = field.getAnnotation(Capturing.class);
        parameterImplementationClass = null;
        Injectable injectableAnnotation = field.getAnnotation(Injectable.class);
        injectable = injectableAnnotation != null;
        providedValue = getProvidedInjectableValue(injectableAnnotation);
        registerCascadingAsNeeded();
    }

    @Nullable
    private Object getProvidedInjectableValue(@Nullable Injectable annotation) {
        if (annotation != null) {
            String value = annotation.value();

            if (!value.isEmpty()) {
                Class<?> injectableClass = getClassType();

                if (injectableClass != TypeVariable.class) {
                    return TypeConversion.convertFromString(injectableClass, value);
                }
            }
        }

        return null;
    }

    private void registerCascadingAsNeeded() {
        if (isMockableType()) {
            Type mockedType = declaredType;

            if (!(mockedType instanceof TypeVariable<?>)) {
                ExecutingTest executingTest = TestRun.getExecutingTest();
                CascadingTypes types = executingTest.getCascadingTypes();
                types.add(fieldFromTestClass, mockedType);
            }
        }
    }

    MockedType(@NonNull TestMethod testMethod, @NonNegative int paramIndex, @NonNull Type parameterType,
            @NonNull Annotation[] annotationsOnParameter, @Nullable Class<?> parameterImplementationClass) {
        super(parameterType, ParameterNames.getName(testMethod, paramIndex));
        field = null;
        fieldFromTestClass = false;
        accessModifiers = 0;
        mocked = getAnnotation(annotationsOnParameter, Mocked.class);
        capturing = getAnnotation(annotationsOnParameter, Capturing.class);
        this.parameterImplementationClass = parameterImplementationClass;
        Injectable injectableAnnotation = getAnnotation(annotationsOnParameter, Injectable.class);
        injectable = injectableAnnotation != null;
        providedValue = getProvidedInjectableValue(injectableAnnotation);

        if (providedValue == null && parameterType instanceof Class<?>) {
            Class<?> parameterClass = (Class<?>) parameterType;

            if (parameterClass.isPrimitive()) {
                providedValue = DefaultValues.defaultValueForPrimitiveType(parameterClass);
            }
        }

        registerCascadingAsNeeded();
    }

    @Nullable
    private static <A extends Annotation> A getAnnotation(@NonNull Annotation[] annotations,
            @NonNull Class<A> annotation) {
        for (Annotation paramAnnotation : annotations) {
            if (paramAnnotation.annotationType() == annotation) {
                // noinspection unchecked
                return (A) paramAnnotation;
            }
        }

        return null;
    }

    MockedType(@NonNull String cascadingMethodName, @NonNull Type cascadedType) {
        super(cascadedType, cascadingMethodName);
        field = null;
        fieldFromTestClass = false;
        accessModifiers = 0;
        mocked = null;
        capturing = null;
        injectable = true;
        parameterImplementationClass = null;
    }

    @NonNull
    @Override
    public Class<?> getClassOfDeclaredType() {
        return getClassType();
    }

    /**
     * @return the class object corresponding to the type to be mocked, or <code>TypeVariable.class</code> in case the
     *         mocked type is a type variable (which usually occurs when the mocked type implements/extends multiple
     *         types)
     */
    @NonNull
    public Class<?> getClassType() {
        if (parameterImplementationClass != null) {
            return parameterImplementationClass;
        }

        Type mockedType = declaredType;

        if (mockedType instanceof Class<?>) {
            return (Class<?>) mockedType;
        }

        if (mockedType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) mockedType;
            return (Class<?>) parameterizedType.getRawType();
        }

        // Occurs when declared type is a TypeVariable, usually having two or more bound types.
        // In such cases, there isn't a single class type.
        return TypeVariable.class;
    }

    boolean isMockableType() {
        if (mocked == null && !injectable && capturing == null) {
            return false;
        }

        Class<?> classType = Utilities.getClassType(declaredType);

        if (isUnmockableJREType(classType)) {
            return false;
        }

        MockingFilters.validateAsMockable(classType);

        if (injectable) {
            return !isJREValueType(classType) && !classType.isEnum();
        }

        return true;
    }

    private static boolean isUnmockableJREType(@NonNull Class<?> type) {
        return type.isPrimitive() || type.isArray() || type == Integer.class || type == String.class;
    }

    private static boolean isJREValueType(@NonNull Class<?> type) {
        return type == String.class || type == Boolean.class || type == Character.class
                || Number.class.isAssignableFrom(type);
    }

    boolean isFinalFieldOrParameter() {
        return field == null || isFinal(accessModifiers);
    }

    boolean isClassInitializationToBeStubbedOut() {
        return mocked != null && mocked.stubOutClassInitialization();
    }

    boolean withInstancesToCapture() {
        return getMaxInstancesToCapture() > 0;
    }

    public int getMaxInstancesToCapture() {
        return capturing == null ? 0 : capturing.maxInstances();
    }

    @Nullable
    @Override
    public Object getValue(@Nullable Object owner) {
        if (field == null) {
            return providedValue;
        }

        Object value = FieldReflection.getFieldValue(field, owner);

        if (!injectable) {
            return value;
        }

        Class<?> fieldType = field.getType();

        if (value == null) {
            if (providedValue != null) {
                return providedValue;
            }

            if (isFinalFieldOrParameter()) {
                return NULL;
            }

            if (fieldType == String.class) {
                return "";
            }

            return null;
        }

        if (providedValue == null || !fieldType.isPrimitive()) {
            return value;
        }

        Object defaultValue = DefaultValues.defaultValueForPrimitiveType(fieldType);

        return value.equals(defaultValue) ? providedValue : value;
    }

    @Override
    public int hashCode() {
        int result = declaredType.hashCode();

        if (isFinal(accessModifiers)) {
            result *= 31;
        }

        if (injectable) {
            result *= 37;
        }

        if (mocked != null) {
            int h = mocked.hashCode();

            if (h != DUMMY_HASHCODE) {
                result = 31 * result + h;
            }
        }

        return result;
    }
}
