/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.methods;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import mockit.asm.constantPool.AttributeWriter;
import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.controlFlow.Label;
import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Writes the bytecode for the "LineNumberTable" method code attribute.
 */
final class LineNumberTableWriter extends AttributeWriter {
    /**
     * Number of entries in the <code>LineNumberTable</code> attribute.
     */
    @NonNegative
    private int lineNumberCount;

    /**
     * The <code>LineNumberTable</code> attribute.
     */
    @Nullable
    private ByteVector lineNumbers;

    LineNumberTableWriter(@NonNull ConstantPoolGeneration cp) {
        super(cp);
    }

    void addLineNumber(@NonNegative int line, @NonNull Label start) {
        if (lineNumbers == null) {
            setAttribute("LineNumberTable");
            lineNumbers = new ByteVector();
        }

        lineNumberCount++;
        lineNumbers.putShort(start.position);
        lineNumbers.putShort(line);
    }

    boolean hasLineNumbers() {
        return lineNumbers != null;
    }

    @NonNegative
    @Override
    public int getSize() {
        return lineNumbers == null ? 0 : 8 + lineNumbers.getLength();
    }

    @Override
    public void put(@NonNull ByteVector out) {
        if (lineNumbers != null) {
            put(out, 2 + lineNumbers.getLength());
            out.putShort(lineNumberCount);
            out.putByteVector(lineNumbers);
        }
    }
}
