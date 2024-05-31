/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.startup;

import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;
import static java.lang.reflect.Modifier.isPublic;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.security.Provider;

import mockit.asm.classes.ClassReader;
import mockit.asm.classes.ClassVisitor;
import mockit.asm.classes.ClassWriter;
import mockit.asm.classes.WrappingClassVisitor;
import mockit.asm.jvmConstants.Access;
import mockit.internal.ClassLoadingBridge;
import mockit.internal.expectations.mocking.MockedBridge;
import mockit.internal.faking.FakeBridge;
import mockit.internal.faking.FakeMethodBridge;

final class ClassLoadingBridgeFields {
    private ClassLoadingBridgeFields() {
    }

    static void createSyntheticFieldsInJREClassToHoldClassLoadingBridges(@NonNull Instrumentation instrumentation) {
        FieldAdditionTransformer fieldAdditionTransformer = new FieldAdditionTransformer(instrumentation);
        instrumentation.addTransformer(fieldAdditionTransformer);

        // Loads some JRE classes expected to not be loaded yet.
        NegativeArraySizeException.class.getName();
        String hostClassName = fieldAdditionTransformer.hostClassName;

        if (hostClassName == null) {
            Provider.class.getName();
            hostClassName = fieldAdditionTransformer.hostClassName;
        }

        ClassLoadingBridge.hostJREClassName = hostClassName;
    }

    private static final class FieldAdditionTransformer implements ClassFileTransformer {
        private static final int FIELD_ACCESS = PUBLIC + STATIC + Access.SYNTHETIC;
        @NonNull
        private final Instrumentation instrumentation;
        String hostClassName;

        FieldAdditionTransformer(@NonNull Instrumentation instrumentation) {
            this.instrumentation = instrumentation;
        }

        @Nullable
        @Override
        public byte[] transform(@Nullable ClassLoader loader, @NonNull String className,
                @Nullable Class<?> classBeingRedefined, @Nullable ProtectionDomain protectionDomain,
                @NonNull byte[] classfileBuffer) {
            if (loader == null && hostClassName == null) { // adds the fields to the first public JRE class to be loaded
                ClassReader cr = new ClassReader(classfileBuffer);

                if (isPublic(cr.getAccess())) {
                    instrumentation.removeTransformer(this);
                    hostClassName = className;
                    return getModifiedJREClassWithAddedFields(cr);
                }
            }

            return null;
        }

        @NonNull
        private static byte[] getModifiedJREClassWithAddedFields(@NonNull ClassReader classReader) {
            ClassWriter cw = new ClassWriter(classReader);

            ClassVisitor cv = new WrappingClassVisitor(cw) {
                @Override
                public void visitEnd() {
                    addField(MockedBridge.MB);
                    addField(FakeBridge.MB);
                    addField(FakeMethodBridge.MB);
                }

                private void addField(@NonNull ClassLoadingBridge mb) {
                    cw.visitField(FIELD_ACCESS, mb.id, "Ljava/lang/reflect/InvocationHandler;", null, null);
                }
            };

            classReader.accept(cv);
            return cw.toByteArray();
        }
    }
}
