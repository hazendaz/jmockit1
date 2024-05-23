/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.capturing;

import static mockit.internal.capturing.CapturedType.isNotToBeCaptured;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import mockit.asm.classes.ClassReader;
import mockit.asm.classes.ClassVisitor;
import mockit.asm.metadata.ClassMetadataReader;
import mockit.asm.types.JavaType;
import mockit.internal.ClassFile;
import mockit.internal.ClassIdentification;
import mockit.internal.startup.Startup;
import mockit.internal.state.TestRun;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class CaptureTransformer<M> implements ClassFileTransformer {
    @NonNull
    private final CapturedType capturedType;
    @NonNull
    private final String capturedTypeDesc;
    @NonNull
    private final CaptureOfImplementations<M> captureOfImplementations;
    @NonNull
    private final Map<ClassIdentification, byte[]> transformedClasses;
    @NonNull
    private final Map<String, Boolean> superTypesSearched;
    @Nullable
    private final M typeMetadata;
    private boolean inactive;

    CaptureTransformer(@NonNull CapturedType capturedType,
            @NonNull CaptureOfImplementations<M> captureOfImplementations, boolean registerTransformedClasses,
            @Nullable M typeMetadata) {
        this.capturedType = capturedType;
        capturedTypeDesc = JavaType.getInternalName(capturedType.baseType);
        this.captureOfImplementations = captureOfImplementations;
        transformedClasses = registerTransformedClasses ? new HashMap<>(2)
                : Collections.<ClassIdentification, byte[]>emptyMap();
        superTypesSearched = new HashMap<>();
        this.typeMetadata = typeMetadata;
    }

    public void deactivate() {
        inactive = true;

        if (!transformedClasses.isEmpty()) {
            for (Map.Entry<ClassIdentification, byte[]> classNameAndOriginalBytecode : transformedClasses.entrySet()) {
                ClassIdentification classId = classNameAndOriginalBytecode.getKey();
                byte[] originalBytecode = classNameAndOriginalBytecode.getValue();

                Startup.redefineMethods(classId, originalBytecode);
            }

            transformedClasses.clear();
        }
    }

    @Nullable
    @Override
    public byte[] transform(@Nullable ClassLoader loader, @NonNull String classDesc,
            @Nullable Class<?> classBeingRedefined, @Nullable ProtectionDomain protectionDomain,
            @NonNull byte[] classfileBuffer) {
        if (classBeingRedefined != null || inactive || isNotToBeCaptured(protectionDomain, classDesc)) {
            return null;
        }

        if (isClassToBeCaptured(loader, classfileBuffer)) {
            String className = classDesc.replace('/', '.');
            ClassReader cr = new ClassReader(classfileBuffer);
            return modifyAndRegisterClass(loader, className, cr);
        }

        return null;
    }

    private boolean isClassToBeCaptured(@Nullable ClassLoader loader, @NonNull byte[] classfileBuffer) {
        ClassMetadataReader cmr = new ClassMetadataReader(classfileBuffer);
        String superName = cmr.getSuperClass();

        if (capturedTypeDesc.equals(superName)) {
            return true;
        }

        String[] interfaces = cmr.getInterfaces();

        if (interfaces != null && isClassWhichImplementsACapturingInterface(interfaces)) {
            return true;
        }

        return superName != null && searchSuperTypes(loader, superName, interfaces);
    }

    private boolean isClassWhichImplementsACapturingInterface(@NonNull String[] interfaces) {
        for (String implementedInterface : interfaces) {
            if (capturedTypeDesc.equals(implementedInterface)) {
                return true;
            }
        }

        return false;
    }

    private boolean searchSuperTypes(@Nullable ClassLoader loader, @NonNull String superName,
            @Nullable String[] interfaces) {
        if (!"java/lang/Object".equals(superName) && !superName.startsWith("mockit/")
                && searchSuperType(loader, superName)) {
            return true;
        }

        if (interfaces != null && interfaces.length > 0) {
            for (String itf : interfaces) {
                if (!itf.startsWith("java/") && !itf.startsWith("javax/") && searchSuperType(loader, itf)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean searchSuperType(@Nullable ClassLoader loader, @NonNull String superName) {
        Boolean extendsCapturedType = superTypesSearched.get(superName);

        if (extendsCapturedType == null) {
            byte[] classfileBytes = ClassFile.getClassFile(loader, superName);
            extendsCapturedType = isClassToBeCaptured(loader, classfileBytes);
            superTypesSearched.put(superName, extendsCapturedType);
        }

        return extendsCapturedType;
    }

    @NonNull
    private byte[] modifyAndRegisterClass(@Nullable ClassLoader loader, @NonNull String className,
            @NonNull ClassReader cr) {
        ClassVisitor modifier = captureOfImplementations.createModifier(loader, cr, capturedType.baseType,
                typeMetadata);
        cr.accept(modifier);

        ClassIdentification classId = new ClassIdentification(loader, className);
        byte[] originalBytecode = cr.getBytecode();

        if (transformedClasses == Collections.<ClassIdentification, byte[]>emptyMap()) {
            TestRun.mockFixture().addTransformedClass(classId, originalBytecode);
        } else {
            transformedClasses.put(classId, originalBytecode);
        }

        TestRun.mockFixture().registerMockedClass(capturedType.baseType);
        return modifier.toByteArray();
    }

    @Nullable
    public <C extends CaptureOfImplementations<?>> C getCaptureOfImplementationsIfApplicable(@NonNull Class<?> aType) {
        if (typeMetadata != null && capturedType.baseType.isAssignableFrom(aType)) {
            // noinspection unchecked
            return (C) captureOfImplementations;
        }

        return null;
    }

    public boolean areCapturedClasses(@NonNull Class<?> mockedClass1, @NonNull Class<?> mockedClass2) {
        Class<?> baseType = capturedType.baseType;
        return baseType.isAssignableFrom(mockedClass1) && baseType.isAssignableFrom(mockedClass2);
    }
}
