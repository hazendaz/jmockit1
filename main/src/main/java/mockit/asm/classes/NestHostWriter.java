package mockit.asm.classes;

import mockit.asm.constantPool.AttributeWriter;
import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

import edu.umd.cs.findbugs.annotations.NonNull;

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
