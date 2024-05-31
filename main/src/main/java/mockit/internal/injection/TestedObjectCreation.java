/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.injection;

import static java.lang.reflect.Modifier.isAbstract;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import mockit.asm.classes.ClassReader;
import mockit.asm.classes.ClassVisitor;
import mockit.internal.classGeneration.ImplementationClass;
import mockit.internal.expectations.mocking.SubclassGenerationModifier;
import mockit.internal.injection.constructor.ConstructorInjection;
import mockit.internal.injection.constructor.ConstructorSearch;
import mockit.internal.injection.full.FullInjection;
import mockit.internal.state.TestRun;

public final class TestedObjectCreation {
    @NonNull
    private final InjectionState injectionState;
    @Nullable
    private final FullInjection fullInjection;
    @NonNull
    final TestedClass testedClass;

    TestedObjectCreation(@NonNull InjectionState injectionState, @Nullable FullInjection fullInjection,
            @NonNull Type declaredType, @NonNull Class<?> declaredClass) {
        this.injectionState = injectionState;
        this.fullInjection = fullInjection;
        Class<?> actualTestedClass = isAbstract(declaredClass.getModifiers())
                ? generateSubclass(declaredType, declaredClass) : declaredClass;
        testedClass = new TestedClass(declaredType, actualTestedClass);
    }

    @NonNull
    private static Class<?> generateSubclass(@NonNull final Type testedType, @NonNull final Class<?> abstractClass) {
        Class<?> generatedSubclass = new ImplementationClass<>(abstractClass) {
            @NonNull
            @Override
            protected ClassVisitor createMethodBodyGenerator(@NonNull ClassReader cr) {
                return new SubclassGenerationModifier(abstractClass, testedType, cr, generatedClassName, true);
            }
        }.generateClass();

        TestRun.mockFixture().registerMockedClass(generatedSubclass);
        return generatedSubclass;
    }

    public TestedObjectCreation(@NonNull InjectionState injectionState, @Nullable FullInjection fullInjection,
            @NonNull Class<?> implementationClass) {
        this.injectionState = injectionState;
        this.fullInjection = fullInjection;
        testedClass = new TestedClass(implementationClass, implementationClass);
    }

    @Nullable
    public Object create(boolean required, boolean needToConstruct) {
        ConstructorSearch constructorSearch = new ConstructorSearch(injectionState, testedClass, fullInjection != null);
        Constructor<?> constructor = constructorSearch.findConstructorToUse();

        if (constructor == null) {
            String description = constructorSearch.getDescription();
            throw new IllegalArgumentException(
                    "No constructor in tested class that can be satisfied by available tested/injectable values"
                            + description);
        }

        ConstructorInjection constructorInjection = new ConstructorInjection(injectionState, fullInjection,
                constructor);
        return constructorInjection.instantiate(constructorSearch.parameterProviders, testedClass, required,
                needToConstruct);
    }
}
