package mockit.asm.methods;

import mockit.asm.constantPool.AttributeWriter;
import mockit.asm.constantPool.ConstantPoolGeneration;
import mockit.asm.controlFlow.Label;
import mockit.asm.util.ByteVector;

import org.checkerframework.checker.index.qual.NonNegative;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Writes the bytecode for the "LocalVariableTable" and "LocalVariableTypeTable" method code attributes.
 */
final class LocalVariableTableWriter extends AttributeWriter {
    /**
     * Number of entries in the LocalVariableTable attribute.
     */
    @NonNegative
    private int localVarCount;

    /**
     * The LocalVariableTable attribute.
     */
    @Nullable
    private ByteVector localVarTable;

    @NonNegative
    private int localVarTypeAttributeIndex;

    /**
     * Number of entries in the LocalVariableTypeTable attribute.
     */
    @NonNegative
    private int localVarTypeCount;

    /**
     * The LocalVariableTypeTable attribute.
     */
    @Nullable
    private ByteVector localVarTypeTable;

    LocalVariableTableWriter(@NonNull ConstantPoolGeneration cp) {
        super(cp);
    }

    @NonNegative
    int addLocalVariable(@NonNull String name, @NonNull String desc, @Nullable String signature, @NonNull Label start,
            @NonNull Label end, @NonNegative int index) {
        if (signature != null) {
            if (localVarTypeTable == null) {
                localVarTypeAttributeIndex = cp.newUTF8("LocalVariableTypeTable");
                localVarTypeTable = new ByteVector();
            }

            addAttribute(localVarTypeTable, name, signature, start, end, index);
            localVarTypeCount++;
        }

        if (localVarTable == null) {
            setAttribute("LocalVariableTable");
            localVarTable = new ByteVector();
        }

        addAttribute(localVarTable, name, desc, start, end, index);
        localVarCount++;

        char c = desc.charAt(0);
        return index + (c == 'J' || c == 'D' ? 2 : 1);
    }

    private void addAttribute(@NonNull ByteVector attribute, @NonNull String name, @NonNull String desc,
            @NonNull Label start, @NonNull Label end, @NonNegative int index) {
        attribute.putShort(start.position).putShort(end.position - start.position).putShort(cp.newUTF8(name))
                .putShort(cp.newUTF8(desc)).putShort(index);
    }

    @NonNegative
    @Override
    public int getSize() {
        return getSize(localVarTable) + getSize(localVarTypeTable);
    }

    @NonNegative
    private static int getSize(@Nullable ByteVector attribute) {
        return attribute == null ? 0 : 8 + attribute.getLength();
    }

    @NonNegative
    int getAttributeCount() {
        return (localVarTable == null ? 0 : 1) + (localVarTypeTable == null ? 0 : 1);
    }

    @Override
    public void put(@NonNull ByteVector out) {
        put(out, localVarTable, localVarCount);
        attributeIndex = localVarTypeAttributeIndex;
        put(out, localVarTypeTable, localVarTypeCount);
    }

    private void put(@NonNull ByteVector out, @Nullable ByteVector attribute, @NonNegative int numEntries) {
        if (attribute != null) {
            put(out, 2 + attribute.getLength());
            out.putShort(numEntries);
            out.putByteVector(attribute);
        }
    }
}
