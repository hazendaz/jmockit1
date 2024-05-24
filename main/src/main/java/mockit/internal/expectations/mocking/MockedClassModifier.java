/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.mocking;

import static java.lang.reflect.Modifier.ABSTRACT;
import static java.lang.reflect.Modifier.FINAL;
import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PROTECTED;
import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;
import static java.lang.reflect.Modifier.isNative;

import static mockit.asm.jvmConstants.Access.ENUM;
import static mockit.asm.jvmConstants.Access.SYNTHETIC;
import static mockit.asm.jvmConstants.Opcodes.ACONST_NULL;
import static mockit.asm.jvmConstants.Opcodes.DUP;
import static mockit.asm.jvmConstants.Opcodes.IF_ACMPEQ;
import static mockit.asm.jvmConstants.Opcodes.POP;
import static mockit.internal.expectations.MockingFilters.validateAsMockable;
import static mockit.internal.util.ObjectMethods.isMethodFromObject;
import static mockit.internal.util.Utilities.HOTSPOT_VM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.asm.classes.ClassInfo;
import mockit.asm.classes.ClassReader;
import mockit.asm.controlFlow.Label;
import mockit.asm.methods.MethodVisitor;
import mockit.asm.types.JavaType;
import mockit.internal.BaseClassModifier;
import mockit.internal.expectations.ExecutionMode;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

final class MockedClassModifier extends BaseClassModifier {
    private static final int METHOD_ACCESS_MASK = PRIVATE + SYNTHETIC + ABSTRACT;
    private static final int PUBLIC_OR_PROTECTED = PUBLIC + PROTECTED;
    private static final boolean NATIVE_UNSUPPORTED = !HOTSPOT_VM;

    private static final Map<String, String> FILTERS = new HashMap<>(4);
    static {
        FILTERS.put("java/lang/Object", "<init> clone getClass hashCode wait notify notifyAll ");
        FILTERS.put("java/io/File", "compareTo equals getName getPath hashCode toString ");
        FILTERS.put("java/util/logging/Logger", "<init> getName ");
        FILTERS.put("java/util/jar/JarEntry", "<init> ");
    }

    @Nullable
    private final MockedType mockedType;
    private String className;
    private String methodSignature;
    @Nullable
    private String baseClassNameForCapturedInstanceMethods;
    @NonNull
    private ExecutionMode executionMode;
    private boolean isProxy;
    @Nullable
    private String defaultFilters;
    @Nullable
    List<String> enumSubclasses;

    MockedClassModifier(@Nullable ClassLoader classLoader, @NonNull ClassReader classReader,
            @Nullable MockedType typeMetadata) {
        super(classReader);
        mockedType = typeMetadata;
        setUseClassLoadingBridge(classLoader);
        executionMode = typeMetadata != null && typeMetadata.injectable ? ExecutionMode.PerInstance
                : ExecutionMode.Regular;
    }

    void useDynamicMocking() {
        executionMode = ExecutionMode.Partial;
    }

    void setClassNameForCapturedInstanceMethods(@NonNull String internalClassName) {
        baseClassNameForCapturedInstanceMethods = internalClassName;
    }

    @Override
    public void visit(int version, int access, @NonNull String name, @NonNull ClassInfo additionalInfo) {
        validateMockingOfJREClass(name);

        super.visit(version, access, name, additionalInfo);
        isProxy = "java/lang/reflect/Proxy".equals(additionalInfo.superName);

        if (isProxy) {
            className = additionalInfo.interfaces[0];
        } else {
            className = name;
            defaultFilters = FILTERS.get(name);
        }

        if (baseClassNameForCapturedInstanceMethods != null) {
            className = baseClassNameForCapturedInstanceMethods;
        }
    }

    private void validateMockingOfJREClass(@NonNull String internalName) {
        if (internalName.startsWith("java/")) {
            validateAsMockable(internalName);

            if (executionMode == ExecutionMode.Regular && mockedType != null && isFullMockingDisallowed(internalName)) {
                String modifyingClassName = internalName.replace('/', '.');

                if (modifyingClassName.equals(mockedType.getClassType().getName())) {
                    throw new IllegalArgumentException("Class " + modifyingClassName
                            + " cannot be @Mocked fully; instead, use @Injectable or partial mocking");
                }
            }
        }
    }

    private static boolean isFullMockingDisallowed(@NonNull String classDesc) {
        return classDesc.startsWith("java/io/") && ("java/io/FileOutputStream".equals(classDesc)
                || "java/io/FileInputStream".equals(classDesc) || "java/io/FileWriter".equals(classDesc)
                || "java/io/PrintWriter java/io/Writer java/io/DataInputStream".contains(classDesc));
    }

    @Override
    public void visitInnerClass(@NonNull String name, @Nullable String outerName, @Nullable String innerName,
            int access) {
        cw.visitInnerClass(name, outerName, innerName, access);

        // The second condition is for classes compiled with Java 8 or older, which had a bug (as an anonymous class can
        // never be static).
        if (access == ENUM + FINAL || access == ENUM + STATIC) {
            if (enumSubclasses == null) {
                enumSubclasses = new ArrayList<>();
            }

            enumSubclasses.add(name);
        }
    }

    @Nullable
    @Override
    public MethodVisitor visitMethod(final int access, @NonNull final String name, @NonNull final String desc,
            @Nullable final String signature, @Nullable String[] exceptions) {
        if ((access & METHOD_ACCESS_MASK) != 0) {
            return unmodifiedBytecode(access, name, desc, signature, exceptions);
        }

        methodSignature = signature;

        if ("<init>".equals(name)) {
            if (isConstructorNotAllowedByMockingFilters(name)) {
                return unmodifiedBytecode(access, name, desc, signature, exceptions);
            }
        } else {
            if (stubOutFinalizeMethod(access, name, desc)) {
                return null;
            }

            if ("<clinit>".equals(name)) {
                return stubOutClassInitializationIfApplicable(access);
            }

            if (isMethodNotToBeMocked(access, name, desc) || isMethodNotAllowedByMockingFilters(access, name)) {
                return unmodifiedBytecode(access, name, desc, signature, exceptions);
            }
        }

        startModifiedMethodVersion(access, name, desc, signature, exceptions);

        if (isNative(methodAccess)) {
            generateEmptyImplementation(methodDesc);
            return methodAnnotationsVisitor;
        }

        return copyOriginalImplementationWithInjectedInterceptionCode();
    }

    @NonNull
    private MethodVisitor unmodifiedBytecode(int access, @NonNull String name, @NonNull String desc,
            @Nullable String signature, @Nullable String[] exceptions) {
        return cw.visitMethod(access, name, desc, signature, exceptions);
    }

    private boolean isConstructorNotAllowedByMockingFilters(@NonNull String name) {
        return isProxy || executionMode != ExecutionMode.Regular || isUnmockableInvocation(name);
    }

    private boolean isUnmockableInvocation(@NonNull String name) {
        if (defaultFilters == null) {
            return false;
        }

        int i = defaultFilters.indexOf(name);
        return i > -1 && defaultFilters.charAt(i + name.length()) == ' ';
    }

    private boolean isMethodNotToBeMocked(int access, @NonNull String name, @NonNull String desc) {
        return isNative(access) && (NATIVE_UNSUPPORTED || (access & PUBLIC_OR_PROTECTED) == 0)
                || (isProxy || executionMode == ExecutionMode.Partial) && (isMethodFromObject(name, desc)
                        || "annotationType".equals(name) && "()Ljava/lang/Class;".equals(desc));
    }

    @Nullable
    private MethodVisitor stubOutClassInitializationIfApplicable(int access) {
        startModifiedMethodVersion(access, "<clinit>", "()V", null, null);

        if (mockedType != null && mockedType.isClassInitializationToBeStubbedOut()) {
            generateEmptyImplementation();
            return null;
        }

        return mw;
    }

    private boolean stubOutFinalizeMethod(int access, @NonNull String name, @NonNull String desc) {
        if ("finalize".equals(name) && "()V".equals(desc)) {
            startModifiedMethodVersion(access, name, desc, null, null);
            generateEmptyImplementation();
            return true;
        }

        return false;
    }

    private boolean isMethodNotAllowedByMockingFilters(int access, @NonNull String name) {
        return baseClassNameForCapturedInstanceMethods != null && (access & STATIC) != 0
                || executionMode.isMethodToBeIgnored(access) || isUnmockableInvocation(name);
    }

    @Override
    protected void generateInterceptionCode() {
        if (useClassLoadingBridge) {
            generateCallToHandlerThroughMockingBridge();
        } else {
            generateDirectCallToHandler(className, methodAccess, methodName, methodDesc, methodSignature,
                    executionMode);
        }

        generateDecisionBetweenReturningOrContinuingToRealImplementation();
    }

    private void generateCallToHandlerThroughMockingBridge() {
        generateCodeToObtainInstanceOfClassLoadingBridge(MockedBridge.MB);

        // First and second "invoke" arguments:
        boolean isStatic = generateCodeToPassThisOrNullIfStaticMethod();
        mw.visitInsn(ACONST_NULL);

        // Create array for call arguments (third "invoke" argument):
        JavaType[] argTypes = JavaType.getArgumentTypes(methodDesc);
        generateCodeToCreateArrayOfObject(6 + argTypes.length);

        int i = 0;
        generateCodeToFillArrayElement(i, methodAccess);
        i++;
        generateCodeToFillArrayElement(i, className);
        i++;
        generateCodeToFillArrayElement(i, methodName);
        i++;
        generateCodeToFillArrayElement(i, methodDesc);
        i++;
        generateCodeToFillArrayElement(i, methodSignature);
        i++;
        generateCodeToFillArrayElement(i, executionMode.ordinal());
        i++;

        generateCodeToFillArrayWithParameterValues(argTypes, i, isStatic ? 0 : 1);
        generateCallToInvocationHandler();
    }

    private void generateDecisionBetweenReturningOrContinuingToRealImplementation() {
        Label startOfRealImplementation = new Label();
        mw.visitInsn(DUP);
        mw.visitLdcInsn(VOID_TYPE);
        mw.visitJumpInsn(IF_ACMPEQ, startOfRealImplementation);
        generateReturnWithObjectAtTopOfTheStack(methodDesc);
        mw.visitLabel(startOfRealImplementation);
        mw.visitInsn(POP);
    }
}
