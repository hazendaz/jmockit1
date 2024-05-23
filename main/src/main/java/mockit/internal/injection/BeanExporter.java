/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import mockit.internal.injection.InjectionPoint.KindOfInjectionPoint;
import mockit.internal.injection.field.FieldInjection;
import mockit.internal.injection.full.FullInjection;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class BeanExporter {
    @NonNull
    private final InjectionState injectionState;

    BeanExporter(@NonNull InjectionState injectionState) {
        this.injectionState = injectionState;
    }

    @Nullable
    public Object getBean(@NonNull String name) {
        InjectionPoint injectionPoint = new InjectionPoint(Object.class, name, true);
        return injectionState.getInstantiatedDependency(null, injectionPoint);
    }

    @Nullable
    public <T> T getBean(@NonNull Class<T> beanType) {
        TestedClass testedClass = new TestedClass(beanType, beanType);
        String beanName = getBeanNameFromType(beanType);

        injectionState.injectionProviders.setTypeOfInjectionPoint(beanType, KindOfInjectionPoint.NotAnnotated);
        InjectionProvider injectable = injectionState.injectionProviders.findInjectableByTypeAndName(beanName,
                testedClass);

        if (injectable != null) {
            Object testInstance = injectionState.getCurrentTestClassInstance();
            return (T) injectable.getValue(testInstance);
        }

        FullInjection injection = new FullInjection(injectionState, beanType, beanName);
        Injector injector = new FieldInjection(injectionState, injection);

        return (T) injection.createOrReuseInstance(testedClass, injector, null, beanName);
    }

    @NonNull
    private static String getBeanNameFromType(@NonNull Class<?> beanType) {
        String name = beanType.getSimpleName();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
