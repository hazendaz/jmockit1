package mockit.asm.classes;

import mockit.asm.constantPool.AttributeWriter;
import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.util.ByteVector;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Writes out the interfaces implemented or extended by the class or interface being written.
 */
final class InterfaceWriter extends AttributeWriter {
    @NonNull
    private final int[] interfaceItemIndices;

    InterfaceWriter(@NonNull ConstantPoolGeneration cp, @NonNull String[] interfaceNames) {
        super(cp);

        int n = interfaceNames.length;
        int[] cpItemIndices = new int[n];

        for (int i = 0; i < n; i++) {
            cpItemIndices[i] = cp.newClass(interfaceNames[i]);
        }

        interfaceItemIndices = cpItemIndices;
    }

    @Override
    public int getSize() {
        return 2 * interfaceItemIndices.length;
    }

    @Override
    public void put(@NonNull ByteVector out) {
        out.putShort(interfaceItemIndices.length);

        for (int interfaceItemIndex : interfaceItemIndices) {
            out.putShort(interfaceItemIndex);
        }
    }
}
