package mockit.asm.classes;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import mockit.asm.constantPool.AttributeWriter;
import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

final class InnerClassesWriter extends AttributeWriter {
    @NonNull
    private final ByteVector innerClasses;
    @NonNegative
    private int innerClassesCount;

    InnerClassesWriter(@NonNull ConstantPoolGeneration cp) {
        super(cp, "InnerClasses");
        innerClasses = new ByteVector();
    }

    void add(@NonNull String name, @Nullable String outerName, @Nullable String innerName, int access) {
        innerClasses.putShort(cp.newClass(name));
        innerClasses.putShort(outerName == null ? 0 : cp.newClass(outerName));
        innerClasses.putShort(innerName == null ? 0 : cp.newUTF8(innerName));
        innerClasses.putShort(access);
        innerClassesCount++;
    }

    @NonNegative
    @Override
    public int getSize() {
        return 8 + innerClasses.getLength();
    }

    @Override
    public void put(@NonNull ByteVector out) {
        put(out, 2 + innerClasses.getLength());
        out.putShort(innerClassesCount);
        out.putByteVector(innerClasses);
    }
}
