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

final class NestHostWriter extends AttributeWriter {
    @NonNegative
    private final int hostClassNameIndex;

    NestHostWriter(@NonNull ConstantPoolGeneration cp, @NonNull String hostClassName) {
        super(cp, "NestHost");
        hostClassNameIndex = cp.newClass(hostClassName);
    }

    @NonNegative
    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public void put(@NonNull ByteVector out) {
        super.put(out);
        out.putShort(hostClassNameIndex);
    }
}
