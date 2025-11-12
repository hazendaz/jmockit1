/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.classGeneration;

import static mockit.asm.jvmConstants.Access.PUBLIC;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import mockit.asm.classes.ClassReader;
import mockit.asm.classes.ClassVisitor;

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
