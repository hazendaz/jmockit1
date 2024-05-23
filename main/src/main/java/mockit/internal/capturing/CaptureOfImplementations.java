/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.capturing;

import javax.annotation.Nullable;

import mockit.asm.classes.ClassReader;
import mockit.internal.BaseClassModifier;
import mockit.internal.ClassFile;
import mockit.internal.startup.Startup;
import mockit.internal.state.TestRun;

import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class CaptureOfImplementations<M> {
    protected CaptureOfImplementations() {
    }

    protected final void makeSureAllSubtypesAreModified(@NonNull Class<?> baseType, boolean registerCapturedClasses,
            @Nullable M typeMetadata) {
        CapturedType captureMetadata = new CapturedType(baseType);
        redefineClassesAlreadyLoaded(captureMetadata, baseType, typeMetadata);
        createCaptureTransformer(captureMetadata, registerCapturedClasses, typeMetadata);
    }

    private void redefineClassesAlreadyLoaded(@NonNull CapturedType captureMetadata, @NonNull Class<?> baseType,
            @Nullable M typeMetadata) {
        Class<?>[] classesLoaded = Startup.instrumentation().getAllLoadedClasses();

        for (Class<?> aClass : classesLoaded) {
            if (captureMetadata.isToBeCaptured(aClass)) {
                redefineClass(aClass, baseType, typeMetadata);
            }
        }
    }

    protected final void redefineClass(@NonNull Class<?> realClass, @NonNull Class<?> baseType,
            @Nullable M typeMetadata) {
        if (!TestRun.mockFixture().containsRedefinedClass(realClass)) {
            ClassReader classReader;

            try {
                classReader = ClassFile.createReaderOrGetFromCache(realClass);
            } catch (ClassFile.NotFoundException ignore) {
                return;
            }

            TestRun.ensureThatClassIsInitialized(realClass);

            BaseClassModifier modifier = createModifier(realClass.getClassLoader(), classReader, baseType,
                    typeMetadata);
            classReader.accept(modifier);

            if (modifier.wasModified()) {
                byte[] modifiedClass = modifier.toByteArray();
                redefineClass(realClass, modifiedClass);
            }
        }
    }

    @NonNull
    protected abstract BaseClassModifier createModifier(@Nullable ClassLoader cl, @NonNull ClassReader cr,
            @NonNull Class<?> baseType, @Nullable M typeMetadata);

    protected abstract void redefineClass(@NonNull Class<?> realClass, @NonNull byte[] modifiedClass);

    private void createCaptureTransformer(@NonNull CapturedType captureMetadata, boolean registerCapturedClasses,
            @Nullable M typeMetadata) {
        CaptureTransformer<M> transformer = new CaptureTransformer<>(captureMetadata, this, registerCapturedClasses,
                typeMetadata);
        Startup.instrumentation().addTransformer(transformer, true);
        TestRun.mockFixture().addCaptureTransformer(transformer);
    }
}
