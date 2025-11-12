/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.transformation;

import static mockit.asm.jvmConstants.Opcodes.ACONST_NULL;
import static mockit.asm.jvmConstants.Opcodes.ALOAD;
import static mockit.asm.jvmConstants.Opcodes.SIPUSH;
import static mockit.internal.util.TypeConversionBytecode.generateCastOrUnboxing;
import static mockit.internal.util.TypeConversionBytecode.isPrimitiveWrapper;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import mockit.asm.methods.MethodWriter;
import mockit.asm.types.ArrayType;
import mockit.asm.types.JavaType;
import mockit.asm.types.ObjectType;
import mockit.asm.types.ReferenceType;

import org.checkerframework.checker.index.qual.NonNegative;

final class Capture {
    @NonNull
    private final InvocationBlockModifier invocationBlockModifier;
    @NonNull
    private final MethodWriter mw;
    @NonNegative
    private final int opcode;
    @NonNegative
    private final int varIndex;
    @Nullable
    private String typeToCapture;
    @NonNegative
    private int parameterIndex;
    @NonNegative
    private boolean parameterIndexFixed;

    Capture(@NonNull InvocationBlockModifier invocationBlockModifier, @NonNegative int opcode,
            @NonNegative int varIndex, @Nullable String typeToCapture, @NonNegative int parameterIndex) {
        this.invocationBlockModifier = invocationBlockModifier;
        mw = invocationBlockModifier.getMethodWriter();
        this.opcode = opcode;
        this.varIndex = varIndex;
        this.typeToCapture = typeToCapture;
        this.parameterIndex = parameterIndex;
    }

    Capture(@NonNull InvocationBlockModifier invocationBlockModifier, @NonNegative int varIndex,
            @NonNegative int parameterIndex) {
        this.invocationBlockModifier = invocationBlockModifier;
        mw = invocationBlockModifier.getMethodWriter();
        opcode = ALOAD;
        this.varIndex = varIndex;
        this.parameterIndex = parameterIndex;
    }

    /**
     * Generates bytecode that will be responsible for performing the following steps: 1. Get the argument value (an
     * Object) for the last matched invocation. 2. Cast to a reference type or unbox to a primitive type, as needed. 3.
     * Store the converted value in its local variable.
     */
    void generateCodeToStoreCapturedValue() {
        if (opcode != ALOAD) {
            mw.visitIntInsn(SIPUSH, parameterIndex);

            if (typeToCapture == null) {
                mw.visitInsn(ACONST_NULL);
            } else {
                mw.visitLdcInsn(typeToCapture);
            }

            invocationBlockModifier.generateCallToActiveInvocationsMethod("matchedArgument",
                    "(ILjava/lang/String;)Ljava/lang/Object;");

            JavaType argType = getArgumentType();
            generateCastOrUnboxing(mw, argType, opcode);

            mw.visitVarInsn(opcode, varIndex);
        }
    }

    @NonNull
    private JavaType getArgumentType() {
        if (typeToCapture == null) {
            return invocationBlockModifier.argumentMatching.getParameterType(parameterIndex);
        }

        if (typeToCapture.charAt(0) == '[') {
            return ArrayType.create(typeToCapture);
        }

        return ObjectType.create(typeToCapture);
    }

    boolean fixParameterIndex(@NonNegative int originalIndex, @NonNegative int newIndex) {
        if (!parameterIndexFixed && parameterIndex == originalIndex) {
            parameterIndex = newIndex;
            parameterIndexFixed = true;
            return true;
        }

        return false;
    }

    void generateCallToSetArgumentTypeIfNeeded() {
        if (opcode == ALOAD) {
            mw.visitIntInsn(SIPUSH, parameterIndex);
            mw.visitLdcInsn(varIndex);
            invocationBlockModifier.generateCallToActiveInvocationsMethod("setExpectedArgumentType", "(II)V");
        } else if (typeToCapture != null && !isTypeToCaptureSameAsParameterType(typeToCapture)) {
            mw.visitIntInsn(SIPUSH, parameterIndex);
            mw.visitLdcInsn(typeToCapture);
            invocationBlockModifier.generateCallToActiveInvocationsMethod("setExpectedArgumentType",
                    "(ILjava/lang/String;)V");
        }
    }

    private boolean isTypeToCaptureSameAsParameterType(@NonNull String typeDesc) {
        JavaType parameterType = invocationBlockModifier.argumentMatching.getParameterType(parameterIndex);

        if (parameterType instanceof ReferenceType) {
            String parameterTypeDesc = ((ReferenceType) parameterType).getInternalName();
            return typeDesc.equals(parameterTypeDesc);
        }

        return isPrimitiveWrapper(typeDesc);
    }
}
