/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.faking;

import static java.lang.reflect.Modifier.isPublic;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import mockit.MockUp;
import mockit.asm.classes.ClassReader;
import mockit.asm.classes.ClassVisitor;
import mockit.internal.classGeneration.ImplementationClass;
import mockit.internal.expectations.mocking.InterfaceImplementationGenerator;
import mockit.internal.util.Utilities;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class FakedImplementationClass<T> {
    private static final ClassLoader THIS_CL = FakedImplementationClass.class.getClassLoader();

    @NonNull
    private final MockUp<?> fakeInstance;
    @Nullable
    private ImplementationClass<T> implementationClass;
    private Class<T> generatedClass;

    public FakedImplementationClass(@NonNull MockUp<?> fakeInstance) {
        this.fakeInstance = fakeInstance;
    }

    @NonNull
    public Class<T> createImplementation(@NonNull Class<T> interfaceToBeFaked, @Nullable Type typeToFake) {
        createImplementation(interfaceToBeFaked);
        byte[] generatedBytecode = implementationClass == null ? null : implementationClass.getGeneratedBytecode();

        FakeClassSetup fakeClassSetup = new FakeClassSetup(generatedClass, typeToFake, fakeInstance, generatedBytecode);
        fakeClassSetup.redefineMethodsInGeneratedClass();

        return generatedClass;
    }

    @NonNull
    Class<T> createImplementation(@NonNull Class<T> interfaceToBeFaked) {
        if (isPublic(interfaceToBeFaked.getModifiers())) {
            generateImplementationForPublicInterface(interfaceToBeFaked);
        } else {
            // noinspection unchecked
            generatedClass = (Class<T>) Proxy.getProxyClass(interfaceToBeFaked.getClassLoader(), interfaceToBeFaked);
        }

        return generatedClass;
    }

    private void generateImplementationForPublicInterface(@NonNull Class<T> interfaceToBeFaked) {
        implementationClass = new ImplementationClass<T>(interfaceToBeFaked) {
            @NonNull
            @Override
            protected ClassVisitor createMethodBodyGenerator(@NonNull ClassReader typeReader) {
                return new InterfaceImplementationGenerator(typeReader, interfaceToBeFaked, generatedClassName);
            }
        };

        generatedClass = implementationClass.generateClass();
    }

    @NonNull
    public Class<T> createImplementation(@NonNull Type[] interfacesToBeFaked) {
        Class<?>[] interfacesToFake = new Class<?>[interfacesToBeFaked.length];

        for (int i = 0; i < interfacesToFake.length; i++) {
            interfacesToFake[i] = Utilities.getClassType(interfacesToBeFaked[i]);
        }

        // noinspection unchecked
        generatedClass = (Class<T>) Proxy.getProxyClass(THIS_CL, interfacesToFake);
        new FakeClassSetup(generatedClass, null, fakeInstance, null).redefineMethods();

        return generatedClass;
    }
}
