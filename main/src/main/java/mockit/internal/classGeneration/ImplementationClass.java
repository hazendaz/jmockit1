/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.classGeneration;

import java.lang.reflect.Type;

import mockit.asm.classes.ClassReader;
import mockit.asm.classes.ClassVisitor;
import mockit.internal.ClassFile;
import mockit.internal.util.ClassLoad;
import mockit.internal.util.GeneratedClasses;
import mockit.internal.util.Utilities;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Allows the creation of new implementation classes for interfaces and abstract classes.
 */
public abstract class ImplementationClass<T> {
    @NonNull
    protected final Class<?> sourceClass;
    @NonNull
    protected String generatedClassName;
    @Nullable
    private byte[] generatedBytecode;

    protected ImplementationClass(@NonNull Type mockedType) {
        this(Utilities.getClassType(mockedType));
    }

    protected ImplementationClass(@NonNull Class<?> mockedClass) {
        this(mockedClass, GeneratedClasses.getNameForGeneratedClass(mockedClass, null));
    }

    protected ImplementationClass(@NonNull Class<?> sourceClass, @NonNull String desiredClassName) {
        this.sourceClass = sourceClass;
        generatedClassName = desiredClassName;
    }

    @NonNull
    public final Class<T> generateClass() {
        ClassReader classReader = ClassFile.createReaderOrGetFromCache(sourceClass);

        ClassVisitor modifier = createMethodBodyGenerator(classReader);
        classReader.accept(modifier);

        return defineNewClass(modifier);
    }

    @NonNull
    protected abstract ClassVisitor createMethodBodyGenerator(@NonNull ClassReader cr);

    @NonNull
    private Class<T> defineNewClass(@NonNull ClassVisitor modifier) {
        final ClassLoader parentLoader = ClassLoad.getClassLoaderWithAccess(sourceClass);
        final byte[] modifiedClassfile = modifier.toByteArray();

        try {
            @SuppressWarnings("unchecked")
            Class<T> generatedClass = (Class<T>) new ClassLoader(parentLoader) {
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    if (!name.equals(generatedClassName)) {
                        return parentLoader.loadClass(name);
                    }

                    return defineClass(name, modifiedClassfile, 0, modifiedClassfile.length);
                }
            }.findClass(generatedClassName);
            generatedBytecode = modifiedClassfile;

            return generatedClass;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to define class: " + generatedClassName, e);
        }
    }

    @Nullable
    public final byte[] getGeneratedBytecode() {
        return generatedBytecode;
    }
}
