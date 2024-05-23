/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.classGeneration;

import static mockit.asm.jvmConstants.Access.PUBLIC;

import mockit.asm.classes.ClassReader;
import mockit.asm.classes.ClassVisitor;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Generates a concrete subclass for an {@code abstract} base class.
 */
public final class ConcreteSubclass<T> extends ImplementationClass<T> {
    public ConcreteSubclass(@NonNull Class<?> baseClass) {
        super(baseClass);
    }

    @NonNull
    @Override
    protected ClassVisitor createMethodBodyGenerator(@NonNull ClassReader typeReader) {
        return new BaseSubclassGenerator(sourceClass, typeReader, null, generatedClassName, false) {
            @Override
            protected void generateMethodImplementation(String className, int access, @NonNull String name,
                    @NonNull String desc, @Nullable String signature, @Nullable String[] exceptions) {
                mw = cw.visitMethod(PUBLIC, name, desc, signature, exceptions);
                generateEmptyImplementation(desc);
            }
        };
    }
}
