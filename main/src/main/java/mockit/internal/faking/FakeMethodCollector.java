/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.faking;

import static mockit.asm.jvmConstants.Access.ABSTRACT;
import static mockit.asm.jvmConstants.Access.BRIDGE;
import static mockit.asm.jvmConstants.Access.NATIVE;
import static mockit.asm.jvmConstants.Access.SYNTHETIC;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.reflect.Method;
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
import mockit.internal.util.TypeDescriptor;

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
            addFakeMethods(classToCollectFakesFrom, methods);

            classToCollectFakesFrom = classToCollectFakesFrom.getSuperclass();
            collectingFromSuperClass = true;
        } while (classToCollectFakesFrom != MockUp.class);
    }

    private void addFakeMethods(@NonNull Class<?> fakeClass, @NonNull List<MethodInfo> methods) {
        for (MethodInfo method : methods) {
            int access = method.accessFlags;

            if ((access & INVALID_METHOD_ACCESSES) == 0 && method.isMethod() && method.hasAnnotation("Lmockit/Mock;")) {
                FakeMethod fakeMethod = fakeMethods.addMethod(collectingFromSuperClass, access, method.name,
                        method.desc);

                if (fakeMethod != null) {
                    FakeState fakeState = createFakeStateIfRequired(fakeMethod);
                    applyInvocationConstraintsIfAny(fakeClass, method, fakeMethod, fakeState);
                }
            }
        }
    }

    @Nullable
    private FakeState createFakeStateIfRequired(@NonNull FakeMethod fakeMethod) {
        if (!fakeMethod.requiresFakeState()) {
            return null;
        }

        FakeState fakeState = new FakeState(fakeMethod);
        fakeMethods.addFakeState(fakeState);
        return fakeState;
    }

    private void applyInvocationConstraintsIfAny(@NonNull Class<?> fakeClass, @NonNull MethodInfo methodInfo,
            @NonNull FakeMethod fakeMethod, @Nullable FakeState existingFakeState) {
        Method javaMethod = findJavaMethod(fakeClass, methodInfo);
        Mock annotation = javaMethod.getAnnotation(Mock.class);

        if (annotation == null) {
            return;
        }

        int expectedInvocations = annotation.invocations();
        int minInvocations = annotation.minInvocations();
        int maxInvocations = annotation.maxInvocations();

        boolean hasConstraints = expectedInvocations >= 0 || minInvocations > 0 || maxInvocations >= 0;

        if (!hasConstraints) {
            return;
        }

        FakeState fakeState = existingFakeState;

        if (fakeState == null) {
            fakeState = new FakeState(fakeMethod);
        }

        if (expectedInvocations >= 0) {
            fakeState.setExpectedInvocations(expectedInvocations);
        }

        if (minInvocations > 0) {
            fakeState.setMinExpectedInvocations(minInvocations);
        }

        if (maxInvocations >= 0) {
            fakeState.setMaxExpectedInvocations(maxInvocations);
        }

        if (existingFakeState == null) {
            fakeMethods.addFakeState(fakeState);
        }
    }

    @NonNull
    private Method findJavaMethod(@NonNull Class<?> fakeClass, @NonNull MethodInfo methodInfo) {
        Class<?>[] parameterTypes = TypeDescriptor.getParameterTypes(methodInfo.desc);

        try {
            Method method = fakeClass.getDeclaredMethod(methodInfo.name, parameterTypes);

            try {
                method.setAccessible(true);
            } catch (SecurityException ignore) {
                // Best-effort only; access will still work for public/protected members.
            }

            return method;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Unable to resolve @Mock method " + fakeClass.getName() + '#' + methodInfo.name, e);
        }
    }
}
