/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.modification;

import static mockit.asm.jvmConstants.Opcodes.ACONST_NULL;
import static mockit.asm.jvmConstants.Opcodes.DCONST_0;
import static mockit.asm.jvmConstants.Opcodes.DUP;
import static mockit.asm.jvmConstants.Opcodes.DUP2_X1;
import static mockit.asm.jvmConstants.Opcodes.DUP_X1;
import static mockit.asm.jvmConstants.Opcodes.DUP_X2;
import static mockit.asm.jvmConstants.Opcodes.FCONST_0;
import static mockit.asm.jvmConstants.Opcodes.GETFIELD;
import static mockit.asm.jvmConstants.Opcodes.GETSTATIC;
import static mockit.asm.jvmConstants.Opcodes.GOTO;
import static mockit.asm.jvmConstants.Opcodes.ICONST_0;
import static mockit.asm.jvmConstants.Opcodes.INVOKESPECIAL;
import static mockit.asm.jvmConstants.Opcodes.INVOKESTATIC;
import static mockit.asm.jvmConstants.Opcodes.INVOKEVIRTUAL;
import static mockit.asm.jvmConstants.Opcodes.IRETURN;
import static mockit.asm.jvmConstants.Opcodes.LCONST_0;
import static mockit.asm.jvmConstants.Opcodes.POP;
import static mockit.asm.jvmConstants.Opcodes.POP2;
import static mockit.asm.jvmConstants.Opcodes.PUTSTATIC;
import static mockit.asm.jvmConstants.Opcodes.RETURN;
import static mockit.asm.jvmConstants.Opcodes.SIPUSH;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import mockit.asm.annotations.AnnotationVisitor;
import mockit.asm.controlFlow.Label;
import mockit.asm.methods.MethodWriter;
import mockit.asm.methods.WrappingMethodVisitor;
import mockit.coverage.data.FileCoverageData;
import mockit.coverage.lines.PerFileLineCoverage;

import org.checkerframework.checker.index.qual.NonNegative;

final class MethodModifier extends WrappingMethodVisitor {
    private static final String DATA_RECORDING_CLASS = "mockit/coverage/TestRun";

    @NonNull
    private final String sourceFileName;
    @NonNull
    private final FileCoverageData fileData;
    @NonNull
    private final PerFileLineCoverage lineCoverageInfo;
    @NonNull
    private final CFGTracking cfgTracking;
    private boolean foundInterestingInstruction;
    @NonNegative
    int currentLine;

    MethodModifier(@NonNull MethodWriter mw, @NonNull String sourceFileName, @NonNull FileCoverageData fileData) {
        super(mw);
        this.sourceFileName = sourceFileName;
        this.fileData = fileData;
        lineCoverageInfo = fileData.getLineCoverageData();
        cfgTracking = new CFGTracking(lineCoverageInfo);
    }

    @Override
    public AnnotationVisitor visitAnnotation(@NonNull String desc) {
        boolean isTestMethod = desc.startsWith("Lorg/junit/") || desc.startsWith("Lorg/testng/");

        if (isTestMethod) {
            throw VisitInterruptedException.INSTANCE;
        }

        return mw.visitAnnotation(desc);
    }

    @Override
    public void visitLineNumber(@NonNegative int line, @NonNull Label start) {
        lineCoverageInfo.addLine(line);
        currentLine = line;
        cfgTracking.startNewLine();
        generateCallToRegisterLineExecution();
        mw.visitLineNumber(line, start);
    }

    private void generateCallToRegisterLineExecution() {
        mw.visitIntInsn(SIPUSH, fileData.index);
        pushCurrentLineOnTheStack();
        mw.visitMethodInsn(INVOKESTATIC, DATA_RECORDING_CLASS, "lineExecuted", "(II)V", false);
    }

    private void pushCurrentLineOnTheStack() {
        if (currentLine <= Short.MAX_VALUE) {
            mw.visitIntInsn(SIPUSH, currentLine);
        } else {
            mw.visitLdcInsn(currentLine);
        }
    }

    @Override
    public void visitLabel(@NonNull Label label) {
        mw.visitLabel(label);
        cfgTracking.afterNewLabel(currentLine, label);
    }

    @Override
    public void visitJumpInsn(@NonNegative int opcode, @NonNull Label label) {
        Label jumpSource = mw.getCurrentBlock();
        assert jumpSource != null;

        mw.visitJumpInsn(opcode, label);

        if (opcode == GOTO) {
            cfgTracking.afterGoto();
        } else {
            cfgTracking.afterConditionalJump(this, jumpSource, label);
        }
    }

    private void generateCallToRegisterBranchTargetExecutionIfPending() {
        cfgTracking.generateCallToRegisterBranchTargetExecutionIfPending(this);
    }

    void generateCallToRegisterBranchTargetExecution(@NonNegative int branchIndex) {
        mw.visitIntInsn(SIPUSH, fileData.index);
        pushCurrentLineOnTheStack();
        mw.visitIntInsn(SIPUSH, branchIndex);
        mw.visitMethodInsn(INVOKESTATIC, DATA_RECORDING_CLASS, "branchExecuted", "(III)V", false);
    }

    @Override
    public void visitInsn(@NonNegative int opcode) {
        boolean isReturn = opcode >= IRETURN && opcode <= RETURN;

        if (!isReturn && !isDefaultReturnValue(opcode)) {
            foundInterestingInstruction = true;
        }

        if (isReturn && !foundInterestingInstruction && cfgTracking.hasOnlyOneLabelBeingVisited()) {
            lineCoverageInfo.getOrCreateLineData(currentLine).markAsUnreachable();
        } else {
            cfgTracking.beforeNoOperandInstruction(this, opcode);
        }

        mw.visitInsn(opcode);
    }

    private static boolean isDefaultReturnValue(@NonNegative int opcode) {
        return opcode == ACONST_NULL || opcode == ICONST_0 || opcode == LCONST_0 || opcode == FCONST_0
                || opcode == DCONST_0;
    }

    @Override
    public void visitIntInsn(@NonNegative int opcode, int operand) {
        foundInterestingInstruction = true;
        generateCallToRegisterBranchTargetExecutionIfPending();
        mw.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(@NonNegative int opcode, @NonNegative int varIndex) {
        generateCallToRegisterBranchTargetExecutionIfPending();
        mw.visitVarInsn(opcode, varIndex);
    }

    @Override
    public void visitTypeInsn(@NonNegative int opcode, @NonNull String typeDesc) {
        generateCallToRegisterBranchTargetExecutionIfPending();
        mw.visitTypeInsn(opcode, typeDesc);
    }

    @Override
    public void visitFieldInsn(@NonNegative int opcode, @NonNull String owner, @NonNull String name,
            @NonNull String desc) {
        // TODO: need to also process field instructions inside accessor methods (STATIC + SYNTHETIC, "access$nnn")
        boolean getField = opcode == GETSTATIC || opcode == GETFIELD;
        boolean isStatic = opcode == PUTSTATIC || opcode == GETSTATIC;
        char fieldType = desc.charAt(0);
        boolean size2 = fieldType == 'J' || fieldType == 'D';
        String classAndFieldNames = null;
        boolean fieldHasData = false;

        if (!owner.startsWith("java/")) {
            classAndFieldNames = owner.substring(owner.lastIndexOf('/') + 1) + '.' + name;
            fieldHasData = fileData.dataCoverageInfo.isFieldWithCoverageData(classAndFieldNames);

            if (fieldHasData && !isStatic) {
                generateCodeToSaveInstanceReferenceOnTheStack(getField, size2);
            }
        }

        generateCallToRegisterBranchTargetExecutionIfPending();
        mw.visitFieldInsn(opcode, owner, name, desc);

        if (opcode == GETSTATIC && "$assertionsDisabled".equals(name)) {
            cfgTracking.registerAssertFoundInCurrentLine();
        }

        if (fieldHasData) {
            generateCallToRegisterFieldCoverage(getField, isStatic, size2, classAndFieldNames);
        }
    }

    private void generateCodeToSaveInstanceReferenceOnTheStack(boolean getField, boolean size2) {
        if (getField) {
            mw.visitInsn(DUP);
        } else {
            if (size2) {
                mw.visitInsn(DUP2_X1);
                mw.visitInsn(POP2);
                mw.visitInsn(DUP_X2);
                mw.visitInsn(DUP_X2);
            } else {
                mw.visitInsn(DUP_X1);
                mw.visitInsn(POP);
                mw.visitInsn(DUP_X1);
                mw.visitInsn(DUP_X1);
            }
            mw.visitInsn(POP);
        }
    }

    private void generateCallToRegisterFieldCoverage(boolean getField, boolean isStatic, boolean size2,
            @NonNull String classAndFieldNames) {
        if (!isStatic && getField) {
            if (size2) {
                mw.visitInsn(DUP2_X1);
                mw.visitInsn(POP2);
            } else {
                mw.visitInsn(DUP_X1);
                mw.visitInsn(POP);
            }
        }

        mw.visitLdcInsn(sourceFileName);
        mw.visitLdcInsn(classAndFieldNames);

        String methodToCall = getField ? "fieldRead" : "fieldAssigned";
        String methodDesc = isStatic ? "(Ljava/lang/String;Ljava/lang/String;)V"
                : "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;)V";

        mw.visitMethodInsn(INVOKESTATIC, DATA_RECORDING_CLASS, methodToCall, methodDesc, false);
    }

    @Override
    public void visitMethodInsn(@NonNegative int opcode, @NonNull String owner, @NonNull String name,
            @NonNull String desc, boolean itf) {
        // This is to ignore bytecode belonging to a static initialization block inserted in a regular line of code by
        // the Java
        // compiler when the class contains at least one "assert" statement.
        // Otherwise, that line of code would always appear as partially covered when running with assertions enabled.
        if (opcode == INVOKEVIRTUAL && "java/lang/Class".equals(owner) && "desiredAssertionStatus".equals(name)) {
            cfgTracking.registerAssertFoundInCurrentLine();
        }

        if (opcode != INVOKESPECIAL || !"()V".equals(desc)) {
            foundInterestingInstruction = true;
        }

        generateCallToRegisterBranchTargetExecutionIfPending();
        mw.visitMethodInsn(opcode, owner, name, desc, itf);
        cfgTracking.afterMethodInstruction(opcode, owner, name);
    }

    @Override
    public void visitLdcInsn(@NonNull Object cst) {
        foundInterestingInstruction = true;
        generateCallToRegisterBranchTargetExecutionIfPending();
        mw.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(@NonNegative int varIndex, int increment) {
        generateCallToRegisterBranchTargetExecutionIfPending();
        mw.visitIincInsn(varIndex, increment);
    }

    @Override
    public void visitTryCatchBlock(@NonNull Label start, @NonNull Label end, @NonNull Label handler,
            @Nullable String type) {
        generateCallToRegisterBranchTargetExecutionIfPending();
        mw.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitLookupSwitchInsn(@NonNull Label dflt, @NonNull int[] keys, @NonNull Label[] labels) {
        cfgTracking.beforeLookupSwitchInstruction();
        generateCallToRegisterBranchTargetExecutionIfPending();
        mw.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitTableSwitchInsn(@NonNegative int min, @NonNegative int max, @NonNull Label dflt,
            @NonNull Label... labels) {
        generateCallToRegisterBranchTargetExecutionIfPending();
        mw.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(@NonNull String desc, @NonNegative int dims) {
        generateCallToRegisterBranchTargetExecutionIfPending();
        mw.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitMaxStack(@NonNegative int maxStack) {
        if (maxStack > 1) {
            lineCoverageInfo.markLineAsReachable(currentLine);
        }

        mw.visitMaxStack(maxStack);
    }
}
