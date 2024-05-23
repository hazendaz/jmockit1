/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.classGeneration;

import static java.util.Arrays.asList;

import static mockit.asm.jvmConstants.Opcodes.ALOAD;
import static mockit.asm.jvmConstants.Opcodes.INVOKESPECIAL;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import mockit.asm.classes.ClassInfo;
import mockit.asm.classes.ClassReader;
import mockit.asm.fields.FieldVisitor;
import mockit.asm.jvmConstants.Access;
import mockit.asm.metadata.ClassMetadataReader;
import mockit.asm.metadata.ClassMetadataReader.MethodInfo;
import mockit.asm.methods.MethodVisitor;
import mockit.asm.types.JavaType;
import mockit.internal.BaseClassModifier;
import mockit.internal.ClassFile;
import mockit.internal.util.TypeDescriptor;

import org.checkerframework.checker.nullness.qual.NonNull;

public class BaseSubclassGenerator extends BaseClassModifier {
    private static final int CLASS_ACCESS_MASK = 0xFFFF - Access.ABSTRACT;
    private static final int CONSTRUCTOR_ACCESS_MASK = Access.PUBLIC + Access.PROTECTED;

    // Fixed initial state:
    @NonNull
    Class<?> baseClass;
    @NonNull
    private final String subclassName;
    @Nullable
    protected final MockedTypeInfo mockedTypeInfo;
    private final boolean copyConstructors;

    // Helper fields for mutable state:
    @NonNull
    private final List<String> implementedMethods;
    @Nullable
    private String superClassOfSuperClass;
    private Set<String> superInterfaces;

    protected BaseSubclassGenerator(@NonNull Class<?> baseClass, @NonNull ClassReader cr,
            @Nullable Type genericMockedType, @NonNull String subclassName, boolean copyConstructors) {
        super(cr);
        this.baseClass = baseClass;
        this.subclassName = subclassName.replace('.', '/');
        mockedTypeInfo = genericMockedType == null ? null : new MockedTypeInfo(genericMockedType);
        this.copyConstructors = copyConstructors;
        implementedMethods = new ArrayList<>();
    }

    @Override
    public void visit(int version, int access, @NonNull String name, @NonNull ClassInfo additionalInfo) {
        ClassInfo subClassInfo = new ClassInfo();
        subClassInfo.superName = name;
        subClassInfo.signature = mockedTypeInfo == null ? additionalInfo.signature
                : mockedTypeInfo.implementationSignature;
        int subclassAccess = access & CLASS_ACCESS_MASK | Access.FINAL;

        super.visit(version, subclassAccess, subclassName, subClassInfo);

        superClassOfSuperClass = additionalInfo.superName;
        superInterfaces = new HashSet<>();

        String[] interfaces = additionalInfo.interfaces;

        if (interfaces.length > 0) {
            superInterfaces.addAll(asList(interfaces));
        }
    }

    @Override
    public final void visitInnerClass(@NonNull String name, @Nullable String outerName, @Nullable String innerName,
            int access) {
    }

    @Override
    @Nullable
    public final FieldVisitor visitField(int access, @NonNull String name, @NonNull String desc,
            @Nullable String signature, @Nullable Object value) {
        return null;
    }

    @Override
    @Nullable
    public MethodVisitor visitMethod(int access, @NonNull String name, @NonNull String desc, @Nullable String signature,
            @Nullable String[] exceptions) {
        if (copyConstructors && "<init>".equals(name)) {
            if ((access & CONSTRUCTOR_ACCESS_MASK) != 0) {
                generateConstructorDelegatingToSuper(desc, signature, exceptions);
            }
        } else {
            // Inherits from super-class when non-abstract; otherwise, creates implementation for abstract method.
            generateImplementationIfAbstractMethod(superClassName, access, name, desc, signature, exceptions);
        }

        return null;
    }

    private void generateConstructorDelegatingToSuper(@NonNull String desc, @Nullable String signature,
            @Nullable String[] exceptions) {
        mw = cw.visitMethod(Access.PUBLIC, "<init>", desc, signature, exceptions);
        mw.visitVarInsn(ALOAD, 0);
        int varIndex = 1;

        for (JavaType paramType : JavaType.getArgumentTypes(desc)) {
            int loadOpcode = paramType.getLoadOpcode();
            mw.visitVarInsn(loadOpcode, varIndex);
            varIndex++;
        }

        mw.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", desc, false);
        generateEmptyImplementation();
    }

    private void generateImplementationIfAbstractMethod(String className, int access, @NonNull String name,
            @NonNull String desc, @Nullable String signature, @Nullable String[] exceptions) {
        if (!"<init>".equals(name)) {
            String methodNameAndDesc = name + desc;

            if (!implementedMethods.contains(methodNameAndDesc)) {
                if ((access & Access.ABSTRACT) != 0) {
                    generateMethodImplementation(className, access, name, desc, signature, exceptions);
                }

                implementedMethods.add(methodNameAndDesc);
            }
        }
    }

    protected void generateMethodImplementation(String className, int access, @NonNull String name,
            @NonNull String desc, @Nullable String signature, @Nullable String[] exceptions) {
    }

    @Override
    public void visitEnd() {
        generateImplementationsForInheritedAbstractMethods(superClassOfSuperClass);

        while (!superInterfaces.isEmpty()) {
            String superInterface = superInterfaces.iterator().next();
            generateImplementationsForAbstractMethods(superInterface, false);
            superInterfaces.remove(superInterface);
        }
    }

    private void generateImplementationsForInheritedAbstractMethods(@Nullable String superName) {
        if (superName != null) {
            generateImplementationsForAbstractMethods(superName, true);
        }
    }

    private void generateImplementationsForAbstractMethods(@NonNull String typeName, boolean abstractClass) {
        if (!"java/lang/Object".equals(typeName)) {
            byte[] typeBytecode = ClassFile.getClassFile(typeName);
            ClassMetadataReader cmr = new ClassMetadataReader(typeBytecode);
            String[] interfaces = cmr.getInterfaces();

            if (interfaces != null) {
                superInterfaces.addAll(asList(interfaces));
            }

            for (MethodInfo method : cmr.getMethods()) {
                if (abstractClass) {
                    generateImplementationIfAbstractMethod(typeName, method.accessFlags, method.name, method.desc, null,
                            null);
                } else if (method.isAbstract()) {
                    generateImplementationForInterfaceMethodIfMissing(typeName, method);
                }
            }

            if (abstractClass) {
                String superClass = cmr.getSuperClass();
                generateImplementationsForInheritedAbstractMethods(superClass);
            }
        }
    }

    private void generateImplementationForInterfaceMethodIfMissing(@NonNull String typeName,
            @NonNull MethodInfo method) {
        String name = method.name;
        String desc = method.desc;
        String methodNameAndDesc = name + desc;

        if (!implementedMethods.contains(methodNameAndDesc)) {
            if (!hasMethodImplementation(name, desc)) {
                generateMethodImplementation(typeName, method.accessFlags, name, desc, null, null);
            }

            implementedMethods.add(methodNameAndDesc);
        }
    }

    private boolean hasMethodImplementation(@NonNull String name, @NonNull String desc) {
        Class<?>[] paramTypes = TypeDescriptor.getParameterTypes(desc);

        try {
            Method method = baseClass.getMethod(name, paramTypes);
            return !method.getDeclaringClass().isInterface();
        } catch (NoSuchMethodException ignore) {
            return false;
        }
    }
}
