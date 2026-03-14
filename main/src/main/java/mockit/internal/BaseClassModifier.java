/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal;

import static java.lang.reflect.Modifier.isNative;
import static java.lang.reflect.Modifier.isStatic;

import static mockit.asm.jvmConstants.Opcodes.AASTORE;
import static mockit.asm.jvmConstants.Opcodes.ACONST_NULL;
import static mockit.asm.jvmConstants.Opcodes.ALOAD;
import static mockit.asm.jvmConstants.Opcodes.ANEWARRAY;
import static mockit.asm.jvmConstants.Opcodes.DUP;
import static mockit.asm.jvmConstants.Opcodes.GETSTATIC;
import static mockit.asm.jvmConstants.Opcodes.ICONST_0;
import static mockit.asm.jvmConstants.Opcodes.ILOAD;
import static mockit.asm.jvmConstants.Opcodes.INVOKEINTERFACE;
import static mockit.asm.jvmConstants.Opcodes.INVOKESPECIAL;
import static mockit.asm.jvmConstants.Opcodes.INVOKESTATIC;
import static mockit.asm.jvmConstants.Opcodes.IRETURN;
import static mockit.asm.jvmConstants.Opcodes.NEW;
import static mockit.asm.jvmConstants.Opcodes.NEWARRAY;
import static mockit.asm.jvmConstants.Opcodes.RETURN;
import static mockit.asm.jvmConstants.Opcodes.SIPUSH;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import mockit.asm.annotations.AnnotationVisitor;
import mockit.asm.classes.ClassInfo;
import mockit.asm.classes.ClassReader;
import mockit.asm.classes.ClassWriter;
import mockit.asm.classes.WrappingClassVisitor;
import mockit.asm.controlFlow.Label;
import mockit.asm.jvmConstants.Access;
import mockit.asm.jvmConstants.ClassVersion;
import mockit.asm.methods.MethodVisitor;
import mockit.asm.methods.MethodWriter;
import mockit.asm.methods.WrappingMethodVisitor;
import mockit.asm.types.ArrayType;
import mockit.asm.types.JavaType;
import mockit.asm.types.ObjectType;
import mockit.asm.types.PrimitiveType;
import mockit.asm.types.ReferenceType;
import mockit.internal.expectations.ExecutionMode;
import mockit.internal.state.TestRun;
import mockit.internal.util.ClassLoad;
import mockit.internal.util.TypeConversionBytecode;

import org.checkerframework.checker.index.qual.NonNegative;

public class BaseClassModifier extends WrappingClassVisitor {
    private static final int METHOD_ACCESS_MASK = 0xFFFF - Access.ABSTRACT - Access.NATIVE;
    protected static final JavaType VOID_TYPE = ObjectType.create("java/lang/Void");

    @NonNull
    protected final MethodVisitor methodAnnotationsVisitor = new MethodVisitor() {
        @Override
        public AnnotationVisitor visitAnnotation(@NonNull String desc) {
            return mw.visitAnnotation(desc);
        }
    };

    protected MethodWriter mw;
    protected boolean useClassLoadingBridge;
    protected String superClassName;
    protected String classDesc;
    protected int methodAccess;
    protected String methodName;
    protected String methodDesc;

    protected BaseClassModifier(@NonNull ClassReader classReader) {
        super(new ClassWriter(classReader));
    }

    protected final void setUseClassLoadingBridge(@Nullable ClassLoader classLoader) {
        useClassLoadingBridge = ClassLoad.isClassLoaderWithNoDirectAccess(classLoader);
    }

    @Override
    public void visit(int version, int access, @NonNull String name, @NonNull ClassInfo additionalInfo) {
        int modifiedVersion = version;
        int originalVersion = version & 0xFFFF;

        if (originalVersion < ClassVersion.V5) {
            // LDC instructions (see MethodVisitor#visitLdcInsn) are more capable in JVMs with support for class files
            // of
            // version 49 (Java 5) or newer, so we "upgrade" it to avoid a VerifyError:
            modifiedVersion = ClassVersion.V5;
        }

        cw.visit(modifiedVersion, access, name, additionalInfo);
        superClassName = additionalInfo.superName;
        classDesc = name;
    }

    /**
     * Just creates a new MethodWriter which will write out the method bytecode when visited.
     * <p>
     * Removes any "abstract" or "native" modifiers for the modified version.
     */
    protected final void startModifiedMethodVersion(int access, @NonNull String name, @NonNull String desc,
            @Nullable String signature, @Nullable String[] exceptions) {
        mw = cw.visitMethod(access & METHOD_ACCESS_MASK, name, desc, signature, exceptions);
        methodAccess = access;
        methodName = name;
        methodDesc = desc;

        if (isNative(access)) {
            TestRun.mockFixture().addRedefinedClassWithNativeMethods(classDesc);
        }
    }

    public final boolean wasModified() {
        return methodName != null;
    }

    protected final void generateDirectCallToHandler(@NonNull String className, int access, @NonNull String name,
            @NonNull String desc, @Nullable String genericSignature) {
        generateDirectCallToHandler(className, access, name, desc, genericSignature, ExecutionMode.Regular);
    }

    protected final void generateDirectCallToHandler(@NonNull String className, int access, @NonNull String name,
            @NonNull String desc, @Nullable String genericSignature, @NonNull ExecutionMode executionMode) {
        // First argument: the mock instance, if any.
        boolean isStatic = generateCodeToPassThisOrNullIfStaticMethod(access);

        // Second argument: method access flags.
        mw.visitLdcInsn(access);

        // Third argument: class name.
        mw.visitLdcInsn(className);

        // Fourth argument: method signature.
        mw.visitLdcInsn(name + desc);

        // Fifth argument: generic signature, or null if none.
        generateInstructionToLoadNullableString(genericSignature);

        // Sixth argument: indicate regular or special modes of execution.
        mw.visitLdcInsn(executionMode.ordinal());

        // Seventh argument: array with invocation arguments.
        JavaType[] argTypes = JavaType.getArgumentTypes(desc);
        int argCount = argTypes.length;

        if (argCount == 0) {
            mw.visitInsn(ACONST_NULL);
        } else {
            generateCodeToCreateArrayOfObject(argCount);
            generateCodeToFillArrayWithParameterValues(argTypes, 0, isStatic ? 0 : 1);
        }

        mw.visitMethodInsn(INVOKESTATIC, "mockit/internal/expectations/RecordAndReplayExecution", "recordOrReplay",
                "(Ljava/lang/Object;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;I[Ljava/lang/Object;)Ljava/lang/Object;",
                false);
    }

    private void generateInstructionToLoadNullableString(@Nullable String text) {
        if (text == null) {
            mw.visitInsn(ACONST_NULL);
        } else {
            mw.visitLdcInsn(text);
        }
    }

    protected final void generateReturnWithObjectAtTopOfTheStack(@NonNull String mockedMethodDesc) {
        JavaType returnType = JavaType.getReturnType(mockedMethodDesc);
        TypeConversionBytecode.generateCastFromObject(mw, returnType);
        mw.visitInsn(returnType.getOpcode(IRETURN));
    }

    protected final boolean generateCodeToPassThisOrNullIfStaticMethod() {
        return generateCodeToPassThisOrNullIfStaticMethod(methodAccess);
    }

    private boolean generateCodeToPassThisOrNullIfStaticMethod(int access) {
        boolean isStatic = isStatic(access);

        if (isStatic) {
            mw.visitInsn(ACONST_NULL);
        } else {
            mw.visitVarInsn(ALOAD, 0);
        }

        return isStatic;
    }

    protected final void generateCodeToCreateArrayOfObject(@NonNegative int arrayLength) {
        mw.visitIntInsn(SIPUSH, arrayLength);
        mw.visitTypeInsn(ANEWARRAY, "java/lang/Object");
    }

    protected final void generateCodeToFillArrayWithParameterValues(@NonNull JavaType[] parameterTypes,
            @NonNegative int initialArrayIndex, @NonNegative int initialParameterIndex) {
        int i = initialArrayIndex;
        int j = initialParameterIndex;

        for (JavaType parameterType : parameterTypes) {
            mw.visitInsn(DUP);
            mw.visitIntInsn(SIPUSH, i);
            i++;
            mw.visitVarInsn(parameterType.getOpcode(ILOAD), j);
            TypeConversionBytecode.generateCastToObject(mw, parameterType);
            mw.visitInsn(AASTORE);
            j += parameterType.getSize();
        }
    }

    protected final void generateCodeToObtainInstanceOfClassLoadingBridge(
            @NonNull ClassLoadingBridge classLoadingBridge) {
        String hostClassName = ClassLoadingBridge.getHostClassName();
        mw.visitFieldInsn(GETSTATIC, hostClassName, classLoadingBridge.id, "Ljava/lang/reflect/InvocationHandler;");
    }

    protected final void generateCodeToFillArrayElement(@NonNegative int arrayIndex, @Nullable Object value) {
        mw.visitInsn(DUP);
        mw.visitIntInsn(SIPUSH, arrayIndex);

        if (value == null) {
            mw.visitInsn(ACONST_NULL);
        } else if (value instanceof Integer) {
            mw.visitIntInsn(SIPUSH, (Integer) value);
            mw.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        } else {
            mw.visitLdcInsn(value);
        }

        mw.visitInsn(AASTORE);
    }

    private void pushDefaultValueForType(@NonNull JavaType type) {
        if (type instanceof ArrayType) {
            generateCreationOfEmptyArray((ArrayType) type);
        } else {
            int constOpcode = type.getConstOpcode();

            if (constOpcode > 0) {
                mw.visitInsn(constOpcode);
            }
        }
    }

    private void generateCreationOfEmptyArray(@NonNull ArrayType arrayType) {
        int dimensions = arrayType.getDimensions();

        for (int dimension = 0; dimension < dimensions; dimension++) {
            mw.visitInsn(ICONST_0);
        }

        if (dimensions > 1) {
            mw.visitMultiANewArrayInsn(arrayType.getDescriptor(), dimensions);
            return;
        }

        JavaType elementType = arrayType.getElementType();

        if (elementType instanceof ReferenceType) {
            mw.visitTypeInsn(ANEWARRAY, ((ReferenceType) elementType).getInternalName());
        } else {
            int typeCode = PrimitiveType.getArrayElementType((PrimitiveType) elementType);
            mw.visitIntInsn(NEWARRAY, typeCode);
        }
    }

    protected final void generateCallToInvocationHandler() {
        mw.visitMethodInsn(INVOKEINTERFACE, "java/lang/reflect/InvocationHandler", "invoke",
                "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;", true);
    }

    protected final void generateEmptyImplementation(@NonNull String desc) {
        JavaType returnType = JavaType.getReturnType(desc);
        pushDefaultValueForType(returnType);
        mw.visitInsn(returnType.getOpcode(IRETURN));
        mw.visitMaxStack(1);
    }

    protected final void generateEmptyImplementation() {
        mw.visitInsn(RETURN);
        mw.visitMaxStack(1);
    }

    @NonNull
    protected final MethodVisitor copyOriginalImplementationWithInjectedInterceptionCode() {
        if ("<init>".equals(methodName)) {
            return new DynamicConstructorModifier();
        }

        generateInterceptionCode();
        return new DynamicModifier();
    }

    protected void generateInterceptionCode() {
    }

    /**
     * Called when a constructor with {@code this()} delegation is detected, allowing subclasses to emit an early fake
     * interception check before the original argument-creation bytecode runs. The default implementation simply emits
     * the supplied label so execution falls through to the original constructor body.
     *
     * @param originalCodeLabel
     *            the label that marks the start of the original (unmodified) constructor body; the overriding method
     *            must emit this label when the early-check path is not taken
     */
    protected void generateEarlyInterceptionCodeForThisDelegationConstructor(@NonNull Label originalCodeLabel) {
        mw.visitLabel(originalCodeLabel);
    }

    private class DynamicModifier extends WrappingMethodVisitor {
        DynamicModifier() {
            super(BaseClassModifier.this.mw);
        }

        @Override
        public final void visitLocalVariable(@NonNull String name, @NonNull String desc, @Nullable String signature,
                @NonNull Label start, @NonNull Label end, @NonNegative int index) {
            // For some reason, the start position for "this" gets displaced by bytecode inserted at the beginning,
            // in a method modified by the EMMA tool. If not treated, this causes a ClassFormatError.
            if (end.position > 0 && start.position > end.position) {
                start.position = end.position;
            }

            // Ignores any local variable with required information missing, to avoid a VerifyError/ClassFormatError.
            if (start.position > 0 && end.position > 0) {
                mw.visitLocalVariable(name, desc, signature, start, end, index);
            }
        }
    }

    private final class DynamicConstructorModifier extends DynamicModifier {
        private boolean pendingCallToConstructorOfSameClass;
        private boolean callToAnotherConstructorAlreadyCopied;

        /**
         * Buffers all instructions that appear before the first {@code this()}/{@code super()} call so we can decide
         * whether to insert an early fake check ahead of argument-creation code that may throw.
         */
        @NonNull
        private final List<Runnable> pendingInstructions = new ArrayList<>();

        @Override
        public void visitInsn(int opcode) {
            if (!callToAnotherConstructorAlreadyCopied) {
                pendingInstructions.add(() -> mw.visitInsn(opcode));
            } else {
                mw.visitInsn(opcode);
            }
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            if (!callToAnotherConstructorAlreadyCopied) {
                pendingInstructions.add(() -> mw.visitIntInsn(opcode, operand));
            } else {
                mw.visitIntInsn(opcode, operand);
            }
        }

        @Override
        public void visitVarInsn(int opcode, @NonNegative int varIndex) {
            if (!callToAnotherConstructorAlreadyCopied) {
                pendingInstructions.add(() -> mw.visitVarInsn(opcode, varIndex));
            } else {
                mw.visitVarInsn(opcode, varIndex);
            }
        }

        @Override
        public void visitTypeInsn(int opcode, @NonNull String typeDesc) {
            if (!callToAnotherConstructorAlreadyCopied) {
                pendingInstructions.add(() -> mw.visitTypeInsn(opcode, typeDesc));

                if (opcode == NEW && typeDesc.equals(classDesc)) {
                    pendingCallToConstructorOfSameClass = true;
                }
            } else {
                mw.visitTypeInsn(opcode, typeDesc);
            }
        }

        @Override
        public void visitFieldInsn(int opcode, @NonNull String owner, @NonNull String name, @NonNull String desc) {
            if (!callToAnotherConstructorAlreadyCopied) {
                pendingInstructions.add(() -> mw.visitFieldInsn(opcode, owner, name, desc));
            } else {
                mw.visitFieldInsn(opcode, owner, name, desc);
            }
        }

        @Override
        public void visitLdcInsn(@NonNull Object cst) {
            if (!callToAnotherConstructorAlreadyCopied) {
                pendingInstructions.add(() -> mw.visitLdcInsn(cst));
            } else {
                mw.visitLdcInsn(cst);
            }
        }

        @Override
        public void visitJumpInsn(int opcode, @NonNull Label label) {
            if (!callToAnotherConstructorAlreadyCopied) {
                pendingInstructions.add(() -> mw.visitJumpInsn(opcode, label));
            } else {
                mw.visitJumpInsn(opcode, label);
            }
        }

        @Override
        public void visitLabel(@NonNull Label label) {
            if (!callToAnotherConstructorAlreadyCopied) {
                pendingInstructions.add(() -> mw.visitLabel(label));
            } else {
                mw.visitLabel(label);
            }
        }

        @Override
        public void visitIincInsn(@NonNegative int varIndex, int increment) {
            if (!callToAnotherConstructorAlreadyCopied) {
                pendingInstructions.add(() -> mw.visitIincInsn(varIndex, increment));
            } else {
                mw.visitIincInsn(varIndex, increment);
            }
        }

        @Override
        public void visitTryCatchBlock(@NonNull Label start, @NonNull Label end, @NonNull Label handler,
                @Nullable String type) {
            if (!callToAnotherConstructorAlreadyCopied) {
                pendingInstructions.add(() -> mw.visitTryCatchBlock(start, end, handler, type));
            } else {
                mw.visitTryCatchBlock(start, end, handler, type);
            }
        }

        @Override
        public void visitLineNumber(@NonNegative int line, @NonNull Label start) {
            if (!callToAnotherConstructorAlreadyCopied) {
                pendingInstructions.add(() -> mw.visitLineNumber(line, start));
            } else {
                mw.visitLineNumber(line, start);
            }
        }

        @Override
        public void visitMethodInsn(int opcode, @NonNull String owner, @NonNull String name, @NonNull String desc,
                boolean itf) {
            if (!callToAnotherConstructorAlreadyCopied) {
                if (pendingCallToConstructorOfSameClass) {
                    if (opcode == INVOKESPECIAL && "<init>".equals(name) && owner.equals(classDesc)) {
                        pendingCallToConstructorOfSameClass = false;
                    }
                    // Buffer this instruction; it belongs to a 'new SameClass(...)' expression
                    pendingInstructions.add(() -> mw.visitMethodInsn(opcode, owner, name, desc, itf));
                } else if (opcode == INVOKESPECIAL && "<init>".equals(name)
                        && (owner.equals(superClassName) || owner.equals(classDesc))) {
                    // This is the first this()/super() call on 'this'
                    callToAnotherConstructorAlreadyCopied = true;

                    if (owner.equals(classDesc)) {
                        // this() delegation: insert early fake check before the buffered argument-creation code
                        Label originalCodeLabel = new Label();
                        generateEarlyInterceptionCodeForThisDelegationConstructor(originalCodeLabel);
                        // Flush the buffered instructions (original argument-creation code)
                        flushPendingInstructions();
                        // Emit the this() call
                        mw.visitMethodInsn(opcode, owner, name, desc, itf);
                        // Emit post-this() interception (for Invocation.proceed() support)
                        generateInterceptionCode();
                    } else {
                        // super() delegation: flush buffer and use existing post-super() interception
                        flushPendingInstructions();
                        mw.visitMethodInsn(opcode, owner, name, desc, itf);
                        generateInterceptionCode();
                    }
                } else {
                    // Some other method call before the init call - buffer it
                    pendingInstructions.add(() -> mw.visitMethodInsn(opcode, owner, name, desc, itf));
                }
            } else {
                // After the first init call has been processed - no more buffering needed
                mw.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }

        private void flushPendingInstructions() {
            for (Runnable r : pendingInstructions) {
                r.run();
            }
            pendingInstructions.clear();
        }
    }
}
