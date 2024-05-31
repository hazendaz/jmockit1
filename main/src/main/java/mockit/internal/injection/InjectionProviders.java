/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import static mockit.internal.injection.InjectionPoint.INJECT_CLASS;
import static mockit.internal.util.Utilities.getClassType;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import mockit.internal.injection.InjectionPoint.KindOfInjectionPoint;
import mockit.internal.reflection.GenericTypeReflection;

public final class InjectionProviders {
    @NonNull
    private List<InjectionProvider> injectables;
    @NonNull
    private List<InjectionProvider> consumedInjectionProviders;
    private Type typeOfInjectionPoint;
    private KindOfInjectionPoint kindOfInjectionPoint;

    InjectionProviders(@NonNull LifecycleMethods lifecycleMethods) {
        injectables = Collections.emptyList();
        consumedInjectionProviders = new ArrayList<>();
    }

    boolean setInjectables(
            @SuppressWarnings("ParameterHidesMemberVariable") @NonNull List<? extends InjectionProvider> injectables) {
        if (injectables.isEmpty()) {
            this.injectables = Collections.emptyList();
            return false;
        }

        this.injectables = new ArrayList<>(injectables);
        return true;
    }

    @NonNull
    List<InjectionProvider> addInjectables(@NonNull List<? extends InjectionProvider> injectablesToAdd) {
        if (!injectablesToAdd.isEmpty()) {
            if (injectables.isEmpty()) {
                injectables = new ArrayList<>(injectablesToAdd);
            } else {
                injectables.addAll(injectablesToAdd);
            }
        }

        return injectables;
    }

    public void setTypeOfInjectionPoint(@NonNull Type typeOfInjectionPoint,
            @NonNull KindOfInjectionPoint kindOfInjectionPoint) {
        this.typeOfInjectionPoint = typeOfInjectionPoint;
        this.kindOfInjectionPoint = kindOfInjectionPoint;
    }

    @Nullable
    public InjectionProvider getProviderByTypeAndOptionallyName(@NonNull String nameOfInjectionPoint,
            @NonNull TestedClass testedClass) {
        if (kindOfInjectionPoint == KindOfInjectionPoint.Required) {
            Type elementTypeOfIterable = getElementTypeIfIterable(typeOfInjectionPoint);

            if (elementTypeOfIterable != null) {
                return findInjectablesByTypeOnly(elementTypeOfIterable, testedClass);
            }
        }

        return findInjectableByTypeAndOptionallyName(nameOfInjectionPoint, testedClass);
    }

    @Nullable
    private static Type getElementTypeIfIterable(@NonNull Type injectableType) {
        if (injectableType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) injectableType;
            Class<?> classOfInjectionPoint = (Class<?>) parameterizedType.getRawType();

            if (Iterable.class.isAssignableFrom(classOfInjectionPoint)) {
                return parameterizedType.getActualTypeArguments()[0];
            }
        }

        return null;
    }

    @Nullable
    public InjectionProvider findNextInjectableForInjectionPoint(@NonNull TestedClass testedClass) {
        for (InjectionProvider injectable : injectables) {
            if (hasTypeAssignableToInjectionPoint(injectable, testedClass)
                    && !consumedInjectionProviders.contains(injectable)) {
                return injectable;
            }
        }

        return null;
    }

    private boolean hasTypeAssignableToInjectionPoint(@NonNull InjectionProvider injectable,
            @NonNull TestedClass testedClass) {
        Type declaredType = injectable.getDeclaredType();
        return isAssignableToInjectionPoint(declaredType, testedClass);
    }

    boolean isAssignableToInjectionPoint(@NonNull Type injectableType, @NonNull TestedClass testedClass) {
        if (testedClass.reflection.areMatchingTypes(typeOfInjectionPoint, injectableType)) {
            return true;
        }

        if (typeOfInjectionPoint instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) typeOfInjectionPoint;
            Class<?> classOfInjectionPoint = (Class<?>) parameterizedType.getRawType();

            if (kindOfInjectionPoint == KindOfInjectionPoint.Required
                    && Iterable.class.isAssignableFrom(classOfInjectionPoint)
                    || INJECT_CLASS != null && Provider.class.isAssignableFrom(classOfInjectionPoint)) {
                Type providedType = parameterizedType.getActualTypeArguments()[0];

                if (providedType.equals(injectableType)) {
                    return true;
                }

                Class<?> injectableClass = getClassType(injectableType);
                Class<?> providedClass = getClassType(providedType);

                return providedClass.isAssignableFrom(injectableClass);
            }
        }

        return false;
    }

    @Nullable
    private InjectionProvider findInjectablesByTypeOnly(@NonNull Type elementType, @NonNull TestedClass testedClass) {
        GenericTypeReflection typeReflection = testedClass.reflection;
        MultiValuedProvider found = null;

        for (InjectionProvider injectable : injectables) {
            Type injectableType = injectable.getDeclaredType();
            Type elementTypeOfIterable = getElementTypeIfIterable(injectableType);

            if (elementTypeOfIterable != null && typeReflection.areMatchingTypes(elementType, elementTypeOfIterable)) {
                return injectable;
            }

            if (isAssignableToInjectionPoint(injectableType, testedClass)) {
                if (found == null) {
                    found = new MultiValuedProvider(elementType);
                }

                found.addInjectable(injectable);
            }
        }

        return found;
    }

    @Nullable
    private InjectionProvider findInjectableByTypeAndOptionallyName(@NonNull String nameOfInjectionPoint,
            @NonNull TestedClass testedClass) {
        InjectionProvider foundInjectable = null;

        for (InjectionProvider injectable : injectables) {
            if (hasTypeAssignableToInjectionPoint(injectable, testedClass)) {
                if (nameOfInjectionPoint.equals(injectable.getName())) {
                    return injectable;
                }

                if (foundInjectable == null) {
                    foundInjectable = injectable;
                }
            }
        }

        return foundInjectable;
    }

    @Nullable
    InjectionProvider findInjectableByTypeAndName(@NonNull String nameOfInjectionPoint,
            @NonNull TestedClass testedClass) {
        for (InjectionProvider injectable : injectables) {
            if (hasTypeAssignableToInjectionPoint(injectable, testedClass)
                    && nameOfInjectionPoint.equals(injectable.getName())) {
                return injectable;
            }
        }

        return null;
    }

    @Nullable
    Object getValueToInject(@NonNull InjectionProvider injectionProvider, @Nullable Object currentTestClassInstance) {
        if (consumedInjectionProviders.contains(injectionProvider)) {
            return null;
        }

        Object value = injectionProvider.getValue(currentTestClassInstance);

        if (value != null) {
            consumedInjectionProviders.add(injectionProvider);
        }

        return value;
    }

    void resetConsumedInjectionProviders() {
        consumedInjectionProviders.clear();
    }

    @NonNull
    public List<InjectionProvider> saveConsumedInjectionProviders() {
        List<InjectionProvider> previouslyConsumed = consumedInjectionProviders;
        consumedInjectionProviders = new ArrayList<>();
        return previouslyConsumed;
    }

    public void restoreConsumedInjectionProviders(@NonNull List<InjectionProvider> previouslyConsumed) {
        consumedInjectionProviders = previouslyConsumed;
    }
}
