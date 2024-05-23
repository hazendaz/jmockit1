/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import java.lang.reflect.Type;

import mockit.asm.classes.ClassReader;
import mockit.asm.jvmConstants.Access;
import mockit.internal.classGeneration.BaseSubclassGenerator;
import mockit.internal.util.ObjectMethods;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class SubclassGenerationModifier extends BaseSubclassGenerator {
    public SubclassGenerationModifier(@NonNull Class<?> baseClass, @NonNull Type mockedType, @NonNull ClassReader cr,
            @NonNull String subclassName, boolean copyConstructors) {
        super(baseClass, cr, mockedType, subclassName, copyConstructors);
    }

    @Override
    protected void generateMethodImplementation(@NonNull String className, int access, @NonNull String name,
            @NonNull String desc, @Nullable String signature, @Nullable String[] exceptions) {
        if (signature != null && mockedTypeInfo != null) {
            signature = mockedTypeInfo.genericTypeMap.resolveSignature(className, signature);
        }

        mw = cw.visitMethod(Access.PUBLIC, name, desc, signature, exceptions);

        if (ObjectMethods.isMethodFromObject(name, desc)) {
            generateEmptyImplementation(desc);
        } else {
            generateDirectCallToHandler(className, access, name, desc, signature);
            generateReturnWithObjectAtTopOfTheStack(desc);
            mw.visitMaxStack(1);
        }
    }
}
