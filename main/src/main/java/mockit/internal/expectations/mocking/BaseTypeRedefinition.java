/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isPublic;

import static mockit.internal.util.GeneratedClasses.getNameForGeneratedClass;
import static mockit.internal.util.Utilities.JAVA8;
import static mockit.internal.util.Utilities.getClassType;

import java.lang.instrument.ClassDefinition;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.asm.classes.ClassReader;
import mockit.asm.classes.ClassVisitor;
import mockit.asm.jvmConstants.ClassVersion;
import mockit.internal.ClassFile;
import mockit.internal.classGeneration.ImplementationClass;
import mockit.internal.expectations.mocking.InstanceFactory.ClassInstanceFactory;
import mockit.internal.expectations.mocking.InstanceFactory.InterfaceInstanceFactory;
import mockit.internal.reflection.ConstructorReflection;
import mockit.internal.reflection.EmptyProxy.Impl;
import mockit.internal.state.TestRun;
import mockit.internal.util.ClassLoad;
import mockit.internal.util.VisitInterruptedException;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

class BaseTypeRedefinition {
    private static final ClassDefinition[] CLASS_DEFINITIONS = {};

    private static final class MockedClass {
        @Nullable
        final InstanceFactory instanceFactory;
        @NonNull
        final ClassDefinition[] mockedClassDefinitions;

        MockedClass(@Nullable InstanceFactory instanceFactory, @NonNull ClassDefinition[] classDefinitions) {
            this.instanceFactory = instanceFactory;
            mockedClassDefinitions = classDefinitions;
        }

        void redefineClasses() {
            TestRun.mockFixture().redefineClasses(mockedClassDefinitions);
        }
    }

    @NonNull
    private static final Map<Integer, MockedClass> mockedClasses = new HashMap<>();
    @NonNull
    private static final Map<Type, Class<?>> mockImplementations = new HashMap<>();

    Class<?> targetClass;
    @Nullable
    MockedType typeMetadata;
    @Nullable
    private InstanceFactory instanceFactory;
    @Nullable
    private List<ClassDefinition> mockedClassDefinitions;

    BaseTypeRedefinition() {
    }

    BaseTypeRedefinition(@NonNull MockedType typeMetadata) {
        targetClass = typeMetadata.getClassType();
        this.typeMetadata = typeMetadata;
    }

    @Nullable
    final InstanceFactory redefineType(@NonNull Type typeToMock) {
        if (targetClass == TypeVariable.class || targetClass.isInterface()) {
            createMockedInterfaceImplementationAndInstanceFactory(typeToMock);
        } else {
            if (typeMetadata == null || !typeMetadata.isClassInitializationToBeStubbedOut()) {
                TestRun.ensureThatClassIsInitialized(targetClass);
            }
            redefineTargetClassAndCreateInstanceFactory(typeToMock);
        }

        if (instanceFactory != null) {
            Class<?> mockedType = getClassType(typeToMock);
            TestRun.mockFixture().registerInstanceFactoryForMockedType(mockedType, instanceFactory);
        }

        return instanceFactory;
    }

    private void createMockedInterfaceImplementationAndInstanceFactory(@NonNull Type interfaceToMock) {
        Class<?> mockedInterface = interfaceToMock(interfaceToMock);
        Object mockedInstance;

        if (mockedInterface == null) {
            mockedInstance = createMockInterfaceImplementationUsingStandardProxy(interfaceToMock);
        } else {
            mockedInstance = createMockInterfaceImplementationDirectly(interfaceToMock);
        }

        redefinedImplementedInterfacesIfRunningOnJava8(targetClass);
        instanceFactory = new InterfaceInstanceFactory(mockedInstance);
    }

    @Nullable
    private static Class<?> interfaceToMock(@NonNull Type typeToMock) {
        while (true) {
            if (typeToMock instanceof Class<?>) {
                Class<?> theInterface = (Class<?>) typeToMock;

                if (isPublic(theInterface.getModifiers()) && !theInterface.isAnnotation()) {
                    return theInterface;
                }
            } else if (typeToMock instanceof ParameterizedType) {
                typeToMock = ((ParameterizedType) typeToMock).getRawType();
                continue;
            }

            return null;
        }
    }

    @NonNull
    private Object createMockInterfaceImplementationUsingStandardProxy(@NonNull Type typeToMock) {
        ClassLoader loader = getClass().getClassLoader();
        Object mockedInstance = Impl.newEmptyProxy(loader, typeToMock);
        targetClass = mockedInstance.getClass();
        redefineClass(targetClass);
        return mockedInstance;
    }

    @NonNull
    private Object createMockInterfaceImplementationDirectly(@NonNull Type interfaceToMock) {
        Class<?> previousMockImplementationClass = mockImplementations.get(interfaceToMock);

        if (previousMockImplementationClass == null) {
            generateNewMockImplementationClassForInterface(interfaceToMock);
            mockImplementations.put(interfaceToMock, targetClass);
        } else {
            targetClass = previousMockImplementationClass;
        }

        return ConstructorReflection.newInstanceUsingDefaultConstructor(targetClass);
    }

    private void redefineClass(@NonNull Class<?> realClass) {
        ClassReader classReader = ClassFile.createReaderOrGetFromCache(realClass);

        if (realClass.isInterface() && classReader.getVersion() < ClassVersion.V8) {
            return;
        }

        ClassLoader loader = realClass.getClassLoader();
        MockedClassModifier modifier = createClassModifier(loader, classReader);
        redefineClass(realClass, classReader, modifier);
    }

    @NonNull
    private MockedClassModifier createClassModifier(@Nullable ClassLoader loader, @NonNull ClassReader classReader) {
        MockedClassModifier modifier = new MockedClassModifier(loader, classReader, typeMetadata);
        configureClassModifier(modifier);
        return modifier;
    }

    void configureClassModifier(@NonNull MockedClassModifier modifier) {
    }

    private void generateNewMockImplementationClassForInterface(@NonNull final Type interfaceToMock) {
        ImplementationClass<?> implementationGenerator = new ImplementationClass<>(interfaceToMock) {
            @NonNull
            @Override
            protected ClassVisitor createMethodBodyGenerator(@NonNull ClassReader cr) {
                return new InterfaceImplementationGenerator(cr, interfaceToMock, generatedClassName);
            }
        };

        targetClass = implementationGenerator.generateClass();
    }

    private void redefinedImplementedInterfacesIfRunningOnJava8(@NonNull Class<?> aClass) {
        if (JAVA8) {
            redefineImplementedInterfaces(aClass.getInterfaces());
        }
    }

    final boolean redefineMethodsAndConstructorsInTargetType() {
        return redefineClassAndItsSuperClasses(targetClass);
    }

    private boolean redefineClassAndItsSuperClasses(@NonNull Class<?> realClass) {
        ClassLoader loader = realClass.getClassLoader();
        ClassReader classReader = ClassFile.createReaderOrGetFromCache(realClass);
        MockedClassModifier modifier = createClassModifier(loader, classReader);

        try {
            redefineClass(realClass, classReader, modifier);
        } catch (VisitInterruptedException ignore) {
            // As defined in MockedClassModifier, some critical JRE classes have all methods excluded from mocking by
            // default. This exception occurs when they are visited.
            // In this case, we simply stop class redefinition for the rest of the class hierarchy.
            return false;
        }

        redefineElementSubclassesOfEnumTypeIfAny(modifier.enumSubclasses);
        redefinedImplementedInterfacesIfRunningOnJava8(realClass);

        Class<?> superClass = realClass.getSuperclass();
        boolean redefined = true;

        if (superClass != null && superClass != Object.class && superClass != Proxy.class && superClass != Enum.class) {
            redefined = redefineClassAndItsSuperClasses(superClass);
        }

        return redefined;
    }

    private void redefineClass(@NonNull Class<?> realClass, @NonNull ClassReader classReader,
            @NonNull MockedClassModifier modifier) {
        classReader.accept(modifier);

        if (modifier.wasModified()) {
            byte[] modifiedClass = modifier.toByteArray();
            applyClassRedefinition(realClass, modifiedClass);
        }
    }

    void applyClassRedefinition(@NonNull Class<?> realClass, @NonNull byte[] modifiedClass) {
        ClassDefinition classDefinition = new ClassDefinition(realClass, modifiedClass);
        TestRun.mockFixture().redefineClasses(classDefinition);

        if (mockedClassDefinitions != null) {
            mockedClassDefinitions.add(classDefinition);
        }
    }

    private void redefineElementSubclassesOfEnumTypeIfAny(@Nullable List<String> enumSubclasses) {
        if (enumSubclasses != null) {
            for (String enumSubclassDesc : enumSubclasses) {
                Class<?> enumSubclass = ClassLoad.loadByInternalName(enumSubclassDesc);
                redefineClass(enumSubclass);
            }
        }
    }

    private void redefineImplementedInterfaces(@NonNull Class<?>[] implementedInterfaces) {
        for (Class<?> implementedInterface : implementedInterfaces) {
            redefineClass(implementedInterface);
            redefineImplementedInterfaces(implementedInterface.getInterfaces());
        }
    }

    private void redefineTargetClassAndCreateInstanceFactory(@NonNull Type typeToMock) {
        Integer mockedClassId = redefineClassesFromCache();

        if (mockedClassId == null) {
            return;
        }

        boolean redefined = redefineMethodsAndConstructorsInTargetType();
        instanceFactory = createInstanceFactory(typeToMock);

        if (redefined) {
            storeRedefinedClassesInCache(mockedClassId);
        }
    }

    @NonNull
    final InstanceFactory createInstanceFactory(@NonNull Type typeToMock) {
        Class<?> classToInstantiate = targetClass;

        if (isAbstract(classToInstantiate.getModifiers())) {
            classToInstantiate = generateConcreteSubclassForAbstractType(typeToMock);
        }

        return new ClassInstanceFactory(classToInstantiate);
    }

    @Nullable
    private Integer redefineClassesFromCache() {
        // noinspection ConstantConditions
        Integer mockedClassId = typeMetadata.hashCode();
        MockedClass mockedClass = mockedClasses.get(mockedClassId);

        if (mockedClass != null) {
            mockedClass.redefineClasses();
            instanceFactory = mockedClass.instanceFactory;
            return null;
        }

        mockedClassDefinitions = new ArrayList<>();
        return mockedClassId;
    }

    private void storeRedefinedClassesInCache(@NonNull Integer mockedClassId) {
        assert mockedClassDefinitions != null;
        ClassDefinition[] classDefs = mockedClassDefinitions.toArray(CLASS_DEFINITIONS);
        MockedClass mockedClass = new MockedClass(instanceFactory, classDefs);

        mockedClasses.put(mockedClassId, mockedClass);
    }

    @NonNull
    private Class<?> generateConcreteSubclassForAbstractType(@NonNull final Type typeToMock) {
        final String subclassName = getNameForConcreteSubclassToCreate();

        return new ImplementationClass<>(targetClass, subclassName) {
            @NonNull
            @Override
            protected ClassVisitor createMethodBodyGenerator(@NonNull ClassReader cr) {
                return new SubclassGenerationModifier(targetClass, typeToMock, cr, subclassName, false);
            }
        }.generateClass();
    }

    @NonNull
    private String getNameForConcreteSubclassToCreate() {
        String mockId = typeMetadata == null ? null : typeMetadata.getName();
        return getNameForGeneratedClass(targetClass, mockId);
    }
}
