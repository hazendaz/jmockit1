package mockit.asm.classes;

import javax.annotation.Nonnull;

import mockit.asm.constantPool.AttributeWriter;
import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

final class NestHostWriter extends AttributeWriter {
    @NonNegative
    private final int hostClassNameIndex;

    NestHostWriter(@Nonnull ConstantPoolGeneration cp, @Nonnull String hostClassName) {
        super(cp, "NestHost");
        hostClassNameIndex = cp.newClass(hostClassName);
    }

    @NonNegative
    @Override
    public int getSize() {
        return 8;
    }

    @Override
    public void put(@Nonnull ByteVector out) {
        super.put(out);
        out.putShort(hostClassNameIndex);
    }
}
