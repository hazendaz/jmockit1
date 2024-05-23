/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import static java.lang.reflect.Modifier.isStatic;

import static mockit.asm.jvmConstants.Access.FINAL;
import static mockit.asm.jvmConstants.Access.PUBLIC;
import static mockit.asm.jvmConstants.Access.isSynthetic;
import static mockit.asm.jvmConstants.Opcodes.ALOAD;
import static mockit.asm.jvmConstants.Opcodes.INVOKESPECIAL;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import mockit.asm.annotations.AnnotationVisitor;
import mockit.asm.classes.ClassInfo;
import mockit.asm.classes.ClassReader;
import mockit.asm.fields.FieldVisitor;
import mockit.asm.metadata.ClassMetadataReader;
import mockit.asm.metadata.ClassMetadataReader.Attribute;
import mockit.asm.metadata.ClassMetadataReader.MethodInfo;
import mockit.asm.methods.MethodVisitor;
import mockit.internal.BaseClassModifier;
import mockit.internal.ClassFile;
import mockit.internal.classGeneration.MockedTypeInfo;
import mockit.internal.reflection.GenericTypeReflection;
import mockit.internal.reflection.GenericTypeReflection.GenericSignature;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public final class InterfaceImplementationGenerator extends BaseClassModifier {
    private static final int CLASS_ACCESS = PUBLIC + FINAL;
    private static final EnumSet<Attribute> SIGNATURE = EnumSet.of(Attribute.Signature);

    @NonNull
    private final MockedTypeInfo mockedTypeInfo;
    @NonNull
    private final String implementationClassDesc;
    @NonNull
    private final List<String> implementedMethods;
    private String interfaceName;
    private String methodOwner;
    @Nullable
    private String[] initialSuperInterfaces;

    public InterfaceImplementationGenerator(@NonNull ClassReader cr, @NonNull Type mockedType,
            @NonNull String implementationClassName) {
        super(cr);
        mockedTypeInfo = new MockedTypeInfo(mockedType);
        implementationClassDesc = implementationClassName.replace('.', '/');
        implementedMethods = new ArrayList<>();
    }

    @Override
    public void visit(int version, int access, @NonNull String name, @NonNull ClassInfo additionalInfo) {
        interfaceName = name;
        methodOwner = name;
        initialSuperInterfaces = additionalInfo.interfaces;

        ClassInfo implementationClassInfo = new ClassInfo();
        String signature = additionalInfo.signature;
        implementationClassInfo.signature = signature == null ? null
                : signature + mockedTypeInfo.implementationSignature;
        implementationClassInfo.interfaces = new String[] { name };
        implementationClassInfo.superName = additionalInfo.superName;

        super.visit(version, CLASS_ACCESS, implementationClassDesc, implementationClassInfo);

        generateNoArgsConstructor();
    }

    private void generateNoArgsConstructor() {
        mw = cw.visitMethod(PUBLIC, "<init>", "()V", null, null);
        mw.visitVarInsn(ALOAD, 0);
        mw.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        generateEmptyImplementation();
    }

    @Override
    public AnnotationVisitor visitAnnotation(@NonNull String desc) {
        return null;
    }

    @Override
    public void visitInnerClass(@NonNull String name, String outerName, String innerName, int access) {
    }

    @Nullable
    @Override
    public FieldVisitor visitField(int access, @NonNull String name, @NonNull String desc, @Nullable String signature,
            @Nullable Object value) {
        return null;
    }

    @Nullable
    @Override
    public MethodVisitor visitMethod(int access, @NonNull String name, @NonNull String desc, @Nullable String signature,
            @Nullable String[] exceptions) {
        if (!isSynthetic(access)) {
            generateMethodImplementation(access, name, desc, signature, exceptions);
        }

        return null;
    }

    private void generateMethodImplementation(int access, @NonNull String name, @NonNull String desc,
            @Nullable String signature, @Nullable String[] exceptions) {
        if (!isStatic(access)) {
            String methodNameAndDesc = name + desc;

            if (!implementedMethods.contains(methodNameAndDesc)) {
                generateMethodBody(access, name, desc, signature, exceptions);
                implementedMethods.add(methodNameAndDesc);
            }
        }
    }

    private void generateMethodBody(int access, @NonNull String name, @NonNull String desc, @Nullable String signature,
            @Nullable String[] exceptions) {
        mw = cw.visitMethod(PUBLIC, name, desc, signature, exceptions);

        String className = null;

        if (signature != null) {
            String subInterfaceOverride = getSubInterfaceOverride(mockedTypeInfo.genericTypeMap, name, signature);

            if (subInterfaceOverride != null) {
                className = interfaceName;
                // noinspection AssignmentToMethodParameter
                desc = subInterfaceOverride.substring(name.length());
                // noinspection AssignmentToMethodParameter
                signature = null;
            }
        }

        if (className == null) {
            className = isOverrideOfMethodFromSuperInterface(name, desc) ? interfaceName : methodOwner;
        }

        generateDirectCallToHandler(className, access, name, desc, signature);
        generateReturnWithObjectAtTopOfTheStack(desc);
        mw.visitMaxStack(1);
    }

    @Nullable
    private String getSubInterfaceOverride(@NonNull GenericTypeReflection genericTypeMap, @NonNull String name,
            @NonNull String genericSignature) {
        if (!implementedMethods.isEmpty()) {
            GenericSignature parsedSignature = genericTypeMap.parseSignature(genericSignature);

            for (String implementedMethod : implementedMethods) {
                if (sameMethodName(implementedMethod, name) && parsedSignature.satisfiesSignature(implementedMethod)) {
                    return implementedMethod;
                }
            }
        }

        return null;
    }

    private static boolean sameMethodName(@NonNull String implementedMethod, @NonNull String name) {
        return implementedMethod.startsWith(name) && implementedMethod.charAt(name.length()) == '(';
    }

    private boolean isOverrideOfMethodFromSuperInterface(@NonNull String name, @NonNull String desc) {
        if (!implementedMethods.isEmpty()) {
            int p = desc.lastIndexOf(')');
            String descNoReturnType = desc.substring(0, p + 1);

            for (String implementedMethod : implementedMethods) {
                if (sameMethodName(implementedMethod, name) && implementedMethod.contains(descNoReturnType)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void visitEnd() {
        assert initialSuperInterfaces != null;

        for (String superInterface : initialSuperInterfaces) {
            generateImplementationsForInterfaceMethodsRecurringToSuperInterfaces(superInterface);
        }
    }

    private void generateImplementationsForInterfaceMethodsRecurringToSuperInterfaces(@NonNull String anInterface) {
        methodOwner = anInterface;

        byte[] interfaceBytecode = ClassFile.getClassFile(anInterface);
        ClassMetadataReader cmr = new ClassMetadataReader(interfaceBytecode, SIGNATURE);
        String[] superInterfaces = cmr.getInterfaces();

        for (MethodInfo method : cmr.getMethods()) {
            generateMethodImplementation(method.accessFlags, method.name, method.desc, method.signature, null);
        }

        if (superInterfaces != null) {
            for (String superInterface : superInterfaces) {
                generateImplementationsForInterfaceMethodsRecurringToSuperInterfaces(superInterface);
            }
        }
    }
}
