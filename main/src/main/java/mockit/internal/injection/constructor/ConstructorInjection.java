/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection.constructor;

import static mockit.internal.injection.InjectionPoint.getQualifiedName;
import static mockit.internal.injection.InjectionPoint.getTypeOfInjectionPointFromVarargsParameter;
import static mockit.internal.injection.InjectionPoint.kindOfInjectionPoint;
import static mockit.internal.injection.InjectionPoint.wrapInProviderIfNeeded;
import static mockit.internal.injection.InjectionProvider.NULL;
import static mockit.internal.reflection.ConstructorReflection.invokeAccessible;
import static mockit.internal.util.Utilities.NO_ARGS;
import static mockit.internal.util.Utilities.ensureThatMemberIsAccessible;
import static mockit.internal.util.Utilities.getClassType;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import mockit.asm.types.JavaType;
import mockit.internal.injection.InjectionPoint.KindOfInjectionPoint;
import mockit.internal.injection.InjectionProvider;
import mockit.internal.injection.InjectionProviders;
import mockit.internal.injection.InjectionState;
import mockit.internal.injection.Injector;
import mockit.internal.injection.TestedClass;
import mockit.internal.injection.full.FullInjection;
import mockit.internal.state.ParameterNames;
import mockit.internal.state.TestRun;
import mockit.internal.util.MethodFormatter;
import mockit.internal.util.StackTrace;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class ConstructorInjection extends Injector {
    @NonNull
    private final Constructor<?> constructor;

    public ConstructorInjection(@NonNull InjectionState injectionState, @Nullable FullInjection fullInjection,
            @NonNull Constructor<?> constructor) {
        super(injectionState, fullInjection);
        ensureThatMemberIsAccessible(constructor);
        this.constructor = constructor;
    }

    @Nullable
    public Object instantiate(@NonNull List<InjectionProvider> parameterProviders, @NonNull TestedClass testedClass,
            boolean required, boolean needToConstruct) {
        Type[] parameterTypes = constructor.getGenericParameterTypes();
        int n = parameterTypes.length;
        List<InjectionProvider> consumedInjectables = n == 0 ? null
                : injectionState.injectionProviders.saveConsumedInjectionProviders();
        Object[] arguments = n == 0 ? NO_ARGS : new Object[n];
        boolean varArgs = constructor.isVarArgs();

        if (varArgs) {
            n--;
        }

        for (int i = 0; i < n; i++) {
            @NonNull
            InjectionProvider parameterProvider = parameterProviders.get(i);
            Object value;

            if (parameterProvider instanceof ConstructorParameter) {
                value = createOrReuseArgumentValue((ConstructorParameter) parameterProvider, required);

                if (value == null && !needToConstruct) {
                    return null;
                }
            } else {
                value = getArgumentValueToInject(parameterProvider, i);
            }

            if (value != NULL) {
                Type parameterType = parameterTypes[i];
                arguments[i] = wrapInProviderIfNeeded(parameterType, value);
            }
        }

        if (varArgs) {
            Type parameterType = parameterTypes[n];
            arguments[n] = obtainInjectedVarargsArray(parameterType, testedClass);
        }

        if (consumedInjectables != null) {
            injectionState.injectionProviders.restoreConsumedInjectionProviders(consumedInjectables);
        }

        return invokeConstructor(arguments);
    }

    @Nullable
    private Object createOrReuseArgumentValue(@NonNull ConstructorParameter constructorParameter, boolean required) {
        Object givenValue = constructorParameter.getValue(null);

        if (givenValue != null) {
            return givenValue;
        }

        assert fullInjection != null;

        Class<?> parameterClass = constructorParameter.getClassOfDeclaredType();
        Object newOrReusedValue = null;

        if (FullInjection.isInstantiableType(parameterClass)) {
            Type parameterType = constructorParameter.getDeclaredType();
            KindOfInjectionPoint kindOfInjectionPoint = kindOfInjectionPoint(constructor);
            injectionState.injectionProviders.setTypeOfInjectionPoint(parameterType, kindOfInjectionPoint);
            String qualifiedName = getQualifiedName(constructorParameter.getAnnotations());
            TestedClass nextTestedClass = new TestedClass(parameterType, parameterClass);

            newOrReusedValue = fullInjection.createOrReuseInstance(nextTestedClass, this, constructorParameter,
                    qualifiedName);
        } else {
            fullInjection.setInjectionProvider(constructorParameter);
        }

        if (newOrReusedValue == null && required) {
            String parameterName = constructorParameter.getName();
            String message = "Missing @Tested or @Injectable" + missingValueDescription(parameterName)
                    + "\r\n  when initializing " + fullInjection;
            IllegalStateException injectionFailure = new IllegalStateException(message);
            StackTrace.filterStackTrace(injectionFailure);
            throw injectionFailure;
        }

        return newOrReusedValue;
    }

    @NonNull
    private Object getArgumentValueToInject(@NonNull InjectionProvider injectable, int parameterIndex) {
        Object argument = injectionState.getValueToInject(injectable);

        if (argument == null) {
            String classDesc = getClassDesc();
            String constructorDesc = getConstructorDesc();
            String parameterName = ParameterNames.getName(classDesc, constructorDesc, parameterIndex);

            if (parameterName == null) {
                parameterName = injectable.getName();
            }

            throw new IllegalArgumentException(
                    "No injectable value available" + missingValueDescription(parameterName));
        }

        return argument;
    }

    @NonNull
    private String getClassDesc() {
        return JavaType.getInternalName(constructor.getDeclaringClass());
    }

    @NonNull
    private String getConstructorDesc() {
        return "<init>" + JavaType.getConstructorDescriptor(constructor);
    }

    @NonNull
    private Object obtainInjectedVarargsArray(@NonNull Type parameterType, @NonNull TestedClass testedClass) {
        Type varargsElementType = getTypeOfInjectionPointFromVarargsParameter(parameterType);
        KindOfInjectionPoint kindOfInjectionPoint = kindOfInjectionPoint(constructor);
        InjectionProviders injectionProviders = injectionState.injectionProviders;
        injectionProviders.setTypeOfInjectionPoint(varargsElementType, kindOfInjectionPoint);

        List<Object> varargValues = new ArrayList<>();
        InjectionProvider injectable;

        while ((injectable = injectionProviders.findNextInjectableForInjectionPoint(testedClass)) != null) {
            Object value = injectionState.getValueToInject(injectable);

            if (value != null) {
                value = wrapInProviderIfNeeded(varargsElementType, value);
                varargValues.add(value);
            }
        }

        return newArrayFromList(varargsElementType, varargValues);
    }

    @NonNull
    private static Object newArrayFromList(@NonNull Type elementType, @NonNull List<Object> values) {
        Class<?> componentType = getClassType(elementType);
        int elementCount = values.size();
        Object array = Array.newInstance(componentType, elementCount);

        for (int i = 0; i < elementCount; i++) {
            Array.set(array, i, values.get(i));
        }

        return array;
    }

    @NonNull
    private String missingValueDescription(@NonNull String name) {
        String classDesc = getClassDesc();
        String constructorDesc = getConstructorDesc();
        String constructorDescription = new MethodFormatter(classDesc, constructorDesc).toString();
        int p = constructorDescription.indexOf('#');
        // noinspection DynamicRegexReplaceableByCompiledPattern
        String friendlyConstructorDesc = constructorDescription.substring(p + 1).replace("java.lang.", "");

        return " for parameter \"" + name + "\" in constructor " + friendlyConstructorDesc;
    }

    @NonNull
    private Object invokeConstructor(@NonNull Object[] arguments) {
        TestRun.exitNoMockingZone();

        try {
            return invokeAccessible(constructor, arguments);
        } finally {
            TestRun.enterNoMockingZone();
        }
    }
}
