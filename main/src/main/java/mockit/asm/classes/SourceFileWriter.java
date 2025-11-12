/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.classes;

import edu.umd.cs.findbugs.annotations.NonNull;

import mockit.asm.constantPool.AttributeWriter;
import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Writes out into the constant pool the item index containing the name of the source file from which the class was
 * compiled.
 */
final class SourceFileWriter extends AttributeWriter {
    @NonNegative
    private final int sourceFileIndex;

    SourceFileWriter(@NonNull ConstantPoolGeneration cp, @NonNull String fileName) {
        super(cp, "SourceFile");
        sourceFileIndex = cp.newUTF8(fileName);
    }

    @NonNegative
    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public void put(@NonNull ByteVector out) {
        super.put(out);
        out.putShort(sourceFileIndex);
    }
}
