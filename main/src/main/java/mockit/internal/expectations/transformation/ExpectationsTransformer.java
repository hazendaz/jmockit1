/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.transformation;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

import javax.annotation.Nullable;

import mockit.asm.classes.ClassReader;
import mockit.asm.classes.ClassVisitor;
import mockit.asm.classes.ClassWriter;
import mockit.asm.classes.WrappingClassVisitor;
import mockit.asm.methods.MethodVisitor;
import mockit.asm.methods.MethodWriter;
import mockit.internal.util.ClassNaming;
import mockit.internal.util.VisitInterruptedException;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class ExpectationsTransformer implements ClassFileTransformer {
    private static final String BASE_CLASSES = "mockit/Expectations mockit/Verifications mockit/VerificationsInOrder mockit/FullVerifications";

    @Nullable
    @Override
    public byte[] transform(@Nullable ClassLoader loader, @NonNull String className,
            @Nullable Class<?> classBeingRedefined, @Nullable ProtectionDomain protectionDomain,
            @NonNull byte[] classfileBuffer) {
        if (classBeingRedefined == null && protectionDomain != null && className != null) {
            boolean anonymousClass = ClassNaming.isAnonymousClass(className);

            if (anonymousClass && !isJMockitClass(className) && !className.startsWith("org/junit/")) {
                ClassReader cr = new ClassReader(classfileBuffer);
                String superClassName = cr.getSuperName();

                if (!BASE_CLASSES.contains(superClassName)) {
                    return null;
                }

                return modifyInvocationsSubclass(cr, className);
            }
        }

        return null;
    }

    private static boolean isJMockitClass(@NonNull String classDesc) {
        return classDesc.startsWith("mockit/") && (classDesc.startsWith("mockit/internal/")
                || classDesc.startsWith("mockit/coverage/") || classDesc.startsWith("mockit/integration/"));
    }

    @Nullable
    private static byte[] modifyInvocationsSubclass(@NonNull ClassReader cr, @NonNull final String classDesc) {
        ClassWriter cw = new ClassWriter(cr);

        ClassVisitor modifier = new WrappingClassVisitor(cw) {
            @Override
            public MethodVisitor visitMethod(int access, @NonNull String name, @NonNull String desc,
                    @Nullable String signature, @Nullable String[] exceptions) {
                MethodWriter mw = cw.visitMethod(access, name, desc, signature, exceptions);

                if (!"<init>".equals(name)) {
                    return mw;
                }

                return new InvocationBlockModifier(mw, classDesc);
            }
        };

        try {
            cr.accept(modifier);
            return modifier.toByteArray();
        } catch (VisitInterruptedException ignore) {
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }
}
