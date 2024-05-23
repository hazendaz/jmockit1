package mockit.asm.methods;

import javax.annotation.Nonnull;

import mockit.asm.constantPool.AttributeWriter;
import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

/**
 * Stores the exceptions that can be thrown by a method/constructor, and writes it to the "Exceptions" attribute. For
 * each thrown exception, stores the index of the constant pool item containing the internal name of the thrown
 * exception class.
 */
final class ExceptionsWriter extends AttributeWriter {
    @Nonnull
    private final int[] exceptionIndices;

    ExceptionsWriter(@Nonnull ConstantPoolGeneration cp, @Nonnull String[] exceptionTypeDescs) {
        super(cp, "Exceptions");
        int n = exceptionTypeDescs.length;
        exceptionIndices = new int[n];

        for (int i = 0; i < n; i++) {
            exceptionIndices[i] = cp.newClass(exceptionTypeDescs[i]);
        }
    }

    @NonNegative
    @Override
    public int getSize() {
        return 8 + 2 * exceptionIndices.length;
    }

    @Override
    public void put(@Nonnull ByteVector out) {
        int n = exceptionIndices.length;
        put(out, 2 + 2 * n);
        out.putShort(n);

        for (int exceptionIndex : exceptionIndices) {
            out.putShort(exceptionIndex);
        }
    }
}
