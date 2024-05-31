/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.expectations.transformation;

import static mockit.asm.jvmConstants.Opcodes.ALOAD;
import static mockit.asm.jvmConstants.Opcodes.DCONST_0;
import static mockit.asm.jvmConstants.Opcodes.FCONST_0;
import static mockit.asm.jvmConstants.Opcodes.GETFIELD;
import static mockit.asm.jvmConstants.Opcodes.GETSTATIC;
import static mockit.asm.jvmConstants.Opcodes.ICONST_0;
import static mockit.asm.jvmConstants.Opcodes.INVOKESTATIC;
import static mockit.asm.jvmConstants.Opcodes.INVOKEVIRTUAL;
import static mockit.asm.jvmConstants.Opcodes.LCONST_0;
import static mockit.asm.jvmConstants.Opcodes.NEW;
import static mockit.asm.jvmConstants.Opcodes.NEWARRAY;
import static mockit.asm.jvmConstants.Opcodes.POP;
import static mockit.asm.jvmConstants.Opcodes.PUTFIELD;
import static mockit.asm.jvmConstants.Opcodes.PUTSTATIC;
import static mockit.asm.jvmConstants.Opcodes.RETURN;
import static mockit.internal.util.TypeConversionBytecode.isBoxing;
import static mockit.internal.util.TypeConversionBytecode.isUnboxing;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import mockit.asm.controlFlow.Label;
import mockit.asm.jvmConstants.JVMInstruction;
import mockit.asm.methods.MethodWriter;
import mockit.asm.methods.WrappingMethodVisitor;
import mockit.asm.types.JavaType;

import org.checkerframework.checker.index.qual.NonNegative;

public final class InvocationBlockModifier extends WrappingMethodVisitor {
    private static final String CLASS_DESC = "mockit/internal/expectations/ActiveInvocations";

    // Input data:
    @NonNull
    private final String blockOwner;

    // Keeps track of the current stack size (after each bytecode instruction) within the invocation block:
    @NonNegative
    private int stackSize;

    // Handle withCapture()/anyXyz/withXyz matchers, if any:
    @NonNull
    final ArgumentMatching argumentMatching;
    @NonNull
    final ArgumentCapturing argumentCapturing;
    private boolean justAfterWithCaptureInvocation;

    // Stores the index of the local variable holding a list passed in a withCapture(List) call, if any:
    @NonNegative
    private int lastLoadedVarIndex;

    InvocationBlockModifier(@NonNull MethodWriter mw, @NonNull String blockOwner) {
        super(mw);
        this.blockOwner = blockOwner;
        argumentMatching = new ArgumentMatching(this);
        argumentCapturing = new ArgumentCapturing(this);
    }

    void generateCallToActiveInvocationsMethod(@NonNull String name) {
        mw.visitMethodInsn(INVOKESTATIC, CLASS_DESC, name, "()V", false);
    }

    void generateCallToActiveInvocationsMethod(@NonNull String name, @NonNull String desc) {
        visitMethodInstruction(INVOKESTATIC, CLASS_DESC, name, desc, false);
    }

    @Override
    public void visitFieldInsn(@NonNegative int opcode, @NonNull String owner, @NonNull String name,
            @NonNull String desc) {
        boolean getField = opcode == GETFIELD;

        if ((getField || opcode == PUTFIELD) && blockOwner.equals(owner)) {
            if (name.indexOf('$') > 0) {
                // Nothing to do.
            } else if (getField && ArgumentMatching.isAnyField(name)) {
                argumentMatching.generateCodeToAddArgumentMatcherForAnyField(owner, name, desc);
                argumentMatching.addMatcher(stackSize);
                return;
            } else if (!getField && generateCodeThatReplacesAssignmentToSpecialField(name)) {
                visitInsn(POP);
                return;
            }
        }

        stackSize += stackSizeVariationForFieldAccess(opcode, desc);
        mw.visitFieldInsn(opcode, owner, name, desc);
    }

    private boolean generateCodeThatReplacesAssignmentToSpecialField(@NonNull String fieldName) {
        if ("result".equals(fieldName)) {
            generateCallToActiveInvocationsMethod("addResult", "(Ljava/lang/Object;)V");
            return true;
        }

        if ("times".equals(fieldName) || "minTimes".equals(fieldName) || "maxTimes".equals(fieldName)) {
            generateCallToActiveInvocationsMethod(fieldName, "(I)V");
            return true;
        }

        return false;
    }

    private static int stackSizeVariationForFieldAccess(@NonNegative int opcode, @NonNull String fieldType) {
        char c = fieldType.charAt(0);
        boolean twoByteType = c == 'D' || c == 'J';

        switch (opcode) {
            case GETSTATIC:
                return twoByteType ? 2 : 1;
            case PUTSTATIC:
                return twoByteType ? -2 : -1;
            case GETFIELD:
                return twoByteType ? 1 : 0;
            case PUTFIELD:
                return twoByteType ? -3 : -2;
            default:
                throw new IllegalArgumentException("Invalid field access opcode: " + opcode);
        }
    }

    @Override
    public void visitMethodInsn(@NonNegative int opcode, @NonNull String owner, @NonNull String name,
            @NonNull String desc, boolean itf) {
        if (opcode == INVOKESTATIC && (isBoxing(owner, name, desc) || isAccessMethod(owner, name))) {
            // It's an invocation to a primitive boxing method or to a synthetic method for private access, just ignore
            // it.
            visitMethodInstruction(INVOKESTATIC, owner, name, desc, itf);
        } else if (isCallToArgumentMatcher(opcode, owner, name, desc)) {
            visitMethodInstruction(INVOKEVIRTUAL, owner, name, desc, itf);

            boolean withCaptureMethod = "withCapture".equals(name);

            if (argumentCapturing.registerMatcher(withCaptureMethod, desc, lastLoadedVarIndex)) {
                justAfterWithCaptureInvocation = withCaptureMethod;
                argumentMatching.addMatcher(stackSize);
            }
        } else if (isUnboxing(opcode, owner, desc)) {
            if (justAfterWithCaptureInvocation) {
                generateCodeToReplaceNullWithZeroOnTopOfStack(desc);
                justAfterWithCaptureInvocation = false;
            } else {
                visitMethodInstruction(opcode, owner, name, desc, itf);
            }
        } else {
            handleMockedOrNonMockedInvocation(opcode, owner, name, desc, itf);
        }
    }

    private boolean isAccessMethod(@NonNull String methodOwner, @NonNull String name) {
        return !methodOwner.equals(blockOwner) && name.startsWith("access$");
    }

    private void visitMethodInstruction(@NonNegative int opcode, @NonNull String owner, @NonNull String name,
            @NonNull String desc, boolean itf) {
        if (!"()V".equals(desc)) {
            int argAndRetSize = JavaType.getArgumentsAndReturnSizes(desc);
            int argSize = argAndRetSize >> 2;

            if (opcode == INVOKESTATIC) {
                argSize--;
            }

            stackSize -= argSize;

            int retSize = argAndRetSize & 0x03;
            stackSize += retSize;
        } else if (opcode != INVOKESTATIC) {
            stackSize--;
        }

        mw.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    private boolean isCallToArgumentMatcher(@NonNegative int opcode, @NonNull String owner, @NonNull String name,
            @NonNull String desc) {
        return opcode == INVOKEVIRTUAL && owner.equals(blockOwner)
                && ArgumentMatching.isCallToArgumentMatcher(name, desc);
    }

    private void generateCodeToReplaceNullWithZeroOnTopOfStack(@NonNull String unboxingMethodDesc) {
        visitInsn(POP);

        char primitiveTypeCode = unboxingMethodDesc.charAt(2);
        int zeroOpcode;

        switch (primitiveTypeCode) {
            case 'J':
                zeroOpcode = LCONST_0;
                break;
            case 'F':
                zeroOpcode = FCONST_0;
                break;
            case 'D':
                zeroOpcode = DCONST_0;
                break;
            default:
                zeroOpcode = ICONST_0;
        }

        visitInsn(zeroOpcode);
    }

    private void handleMockedOrNonMockedInvocation(@NonNegative int opcode, @NonNull String owner, @NonNull String name,
            @NonNull String desc, boolean itf) {
        if (argumentMatching.getMatcherCount() == 0) {
            visitMethodInstruction(opcode, owner, name, desc, itf);
        } else {
            boolean mockedInvocationUsingTheMatchers = argumentMatching.handleInvocationParameters(stackSize, desc);
            visitMethodInstruction(opcode, owner, name, desc, itf);
            handleArgumentCapturingIfNeeded(mockedInvocationUsingTheMatchers);
        }
    }

    private void handleArgumentCapturingIfNeeded(boolean mockedInvocationUsingTheMatchers) {
        if (mockedInvocationUsingTheMatchers) {
            argumentCapturing.generateCallsToCaptureMatchedArgumentsIfPending();
        }

        justAfterWithCaptureInvocation = false;
    }

    @Override
    public void visitLabel(@NonNull Label label) {
        mw.visitLabel(label);

        if (!label.isDebug()) {
            stackSize = 0;
        }
    }

    @Override
    public void visitTypeInsn(@NonNegative int opcode, @NonNull String typeDesc) {
        argumentCapturing.registerTypeToCaptureIfApplicable(opcode, typeDesc);

        if (opcode == NEW) {
            stackSize++;
        }

        mw.visitTypeInsn(opcode, typeDesc);
    }

    @Override
    public void visitIntInsn(@NonNegative int opcode, int operand) {
        if (opcode != NEWARRAY) {
            stackSize++;
        }

        mw.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(@NonNegative int opcode, @NonNegative int varIndex) {
        if (opcode == ALOAD) {
            lastLoadedVarIndex = varIndex;
        }

        argumentCapturing.registerAssignmentToCaptureVariableIfApplicable(opcode, varIndex);
        stackSize += JVMInstruction.SIZE[opcode];
        mw.visitVarInsn(opcode, varIndex);
    }

    @Override
    public void visitLdcInsn(@NonNull Object cst) {
        stackSize++;

        if (cst instanceof Long || cst instanceof Double) {
            stackSize++;
        }

        mw.visitLdcInsn(cst);
    }

    @Override
    public void visitJumpInsn(@NonNegative int opcode, @NonNull Label label) {
        stackSize += JVMInstruction.SIZE[opcode];
        mw.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, @NonNull Label dflt, @NonNull Label... labels) {
        stackSize--;
        mw.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(@NonNull Label dflt, @NonNull int[] keys, @NonNull Label[] labels) {
        stackSize--;
        mw.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(@NonNull String desc, @NonNegative int dims) {
        stackSize += 1 - dims;
        mw.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitInsn(@NonNegative int opcode) {
        if (opcode == RETURN) {
            generateCallToActiveInvocationsMethod("endInvocations");
        } else {
            stackSize += JVMInstruction.SIZE[opcode];
        }

        mw.visitInsn(opcode);
    }

    @Override
    public void visitLocalVariable(@NonNull String name, @NonNull String desc, @Nullable String signature,
            @NonNull Label start, @NonNull Label end, @NonNegative int index) {
        if (signature != null) {
            ArgumentCapturing.registerTypeToCaptureIntoListIfApplicable(index, signature);
        }

        // In classes instrumented with EMMA some local variable information can be lost, so we discard it entirely to
        // avoid a ClassFormatError.
        if (end.position > 0) {
            mw.visitLocalVariable(name, desc, signature, start, end, index);
        }
    }

    @NonNull
    MethodWriter getMethodWriter() {
        return mw;
    }
}
