/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.faking;

import static mockit.asm.jvmConstants.Access.ABSTRACT;
import static mockit.asm.jvmConstants.Access.BRIDGE;
import static mockit.asm.jvmConstants.Access.NATIVE;
import static mockit.asm.jvmConstants.Access.SYNTHETIC;

import java.util.EnumSet;
import java.util.List;

import mockit.Mock;
import mockit.MockUp;
import mockit.asm.metadata.ClassMetadataReader;
import mockit.asm.metadata.ClassMetadataReader.Attribute;
import mockit.asm.metadata.ClassMetadataReader.MethodInfo;
import mockit.asm.types.JavaType;
import mockit.internal.ClassFile;
import mockit.internal.faking.FakeMethods.FakeMethod;
import mockit.internal.util.ClassLoad;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Responsible for collecting the signatures of all methods defined in a given fake class which are explicitly annotated
 * as {@link Mock fakes}.
 */
final class FakeMethodCollector {
    private static final int INVALID_METHOD_ACCESSES = BRIDGE + SYNTHETIC + ABSTRACT + NATIVE;
    private static final EnumSet<Attribute> ANNOTATIONS = EnumSet.of(Attribute.Annotations);

    @NonNull
    private final FakeMethods fakeMethods;
    private boolean collectingFromSuperClass;

    FakeMethodCollector(@NonNull FakeMethods fakeMethods) {
        this.fakeMethods = fakeMethods;
    }

    void collectFakeMethods(@NonNull Class<?> fakeClass) {
        ClassLoad.registerLoadedClass(fakeClass);
        fakeMethods.setFakeClassInternalName(JavaType.getInternalName(fakeClass));

        Class<?> classToCollectFakesFrom = fakeClass;

        do {
            byte[] classfileBytes = ClassFile.readBytesFromClassFile(classToCollectFakesFrom);
            ClassMetadataReader cmr = new ClassMetadataReader(classfileBytes, ANNOTATIONS);
            List<MethodInfo> methods = cmr.getMethods();
            addFakeMethods(methods);

            classToCollectFakesFrom = classToCollectFakesFrom.getSuperclass();
            collectingFromSuperClass = true;
        } while (classToCollectFakesFrom != MockUp.class);
    }

    private void addFakeMethods(@NonNull List<MethodInfo> methods) {
        for (MethodInfo method : methods) {
            int access = method.accessFlags;

            if ((access & INVALID_METHOD_ACCESSES) == 0 && method.isMethod() && method.hasAnnotation("Lmockit/Mock;")) {
                FakeMethod fakeMethod = fakeMethods.addMethod(collectingFromSuperClass, access, method.name,
                        method.desc);

                if (fakeMethod != null && fakeMethod.requiresFakeState()) {
                    FakeState fakeState = new FakeState(fakeMethod);
                    fakeMethods.addFakeState(fakeState);
                }
            }
        }
    }
}
