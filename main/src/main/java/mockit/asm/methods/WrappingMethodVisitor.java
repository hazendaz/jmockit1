/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.methods;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import mockit.asm.annotations.AnnotationVisitor;
import mockit.asm.controlFlow.Label;
import mockit.asm.util.MethodHandle;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Same as {@link MethodVisitor}, except it always wraps a {@link MethodWriter}.
 */
public class WrappingMethodVisitor extends MethodVisitor {
    /**
     * The method writer to which this visitor must delegate method calls.
     */
    @NonNull
    protected final MethodWriter mw;

    /**
     * Initializes a new wrapping Method Visitor.
     *
     * @param mw
     *            the method visitor to which this visitor must delegate method calls
     */
    protected WrappingMethodVisitor(@NonNull MethodWriter mw) {
        this.mw = mw;
    }

    @Nullable
    @Override
    public AnnotationVisitor visitAnnotation(@NonNull String desc) {
        return mw.visitAnnotation(desc);
    }

    @Override
    public final AnnotationVisitor visitParameterAnnotation(@NonNegative int parameter, @NonNull String desc) {
        return mw.visitParameterAnnotation(parameter, desc);
    }

    @Override
    public void visitInsn(int opcode) {
        mw.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        mw.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, @NonNegative int varIndex) {
        mw.visitVarInsn(opcode, varIndex);
    }

    @Override
    public void visitTypeInsn(int opcode, @NonNull String typeDesc) {
        mw.visitTypeInsn(opcode, typeDesc);
    }

    @Override
    public void visitFieldInsn(int opcode, @NonNull String owner, @NonNull String name, @NonNull String desc) {
        mw.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, @NonNull String owner, @NonNull String name, @NonNull String desc,
            boolean itf) {
        mw.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public final void visitInvokeDynamicInsn(@NonNull String name, @NonNull String desc, @NonNull MethodHandle bsm,
            @NonNull Object... bsmArgs) {
        mw.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitJumpInsn(int opcode, @NonNull Label label) {
        mw.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(@NonNull Label label) {
        mw.visitLabel(label);
    }

    @Override
    public void visitLdcInsn(@NonNull Object cst) {
        mw.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(@NonNegative int varIndex, int increment) {
        mw.visitIincInsn(varIndex, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, @NonNull Label dflt, @NonNull Label... labels) {
        mw.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(@NonNull Label dflt, @NonNull int[] keys, @NonNull Label[] labels) {
        mw.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(@NonNull String desc, @NonNegative int dims) {
        mw.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitTryCatchBlock(@NonNull Label start, @NonNull Label end, @NonNull Label handler,
            @Nullable String type) {
        mw.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public void visitLocalVariable(@NonNull String name, @NonNull String desc, String signature, @NonNull Label start,
            @NonNull Label end, @NonNegative int index) {
        mw.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitLineNumber(@NonNegative int line, @NonNull Label start) {
        mw.visitLineNumber(line, start);
    }

    @Override
    public void visitMaxStack(@NonNegative int maxStack) {
        mw.visitMaxStack(maxStack);
    }
}
