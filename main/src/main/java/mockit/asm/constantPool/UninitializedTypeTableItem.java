package mockit.asm.constantPool;

import static mockit.asm.constantPool.TypeTableItem.SpecialType.UNINIT;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class UninitializedTypeTableItem extends TypeTableItem {
    @NonNegative
    int offset;

    UninitializedTypeTableItem() {
        type = UNINIT;
    }

    UninitializedTypeTableItem(@NonNegative int index, @NonNull UninitializedTypeTableItem item) {
        super(index, item);
        offset = item.offset;
    }

    @NonNegative
    public int getOffset() {
        return offset;
    }

    /**
     * Sets the type and bytecode offset of this uninitialized type table item.
     *
     * @param type
     *            the internal name to be added to the type table.
     * @param offset
     *            the bytecode offset of the NEW instruction that created the UNINITIALIZED type value.
     */
    void set(@NonNull String type, @NonNegative int offset) {
        typeDesc = type;
        this.offset = offset;
        setHashCode(type.hashCode() + offset);
    }

    @Override
    boolean isEqualTo(@NonNull Item item) {
        UninitializedTypeTableItem other = (UninitializedTypeTableItem) item;
        return other.offset == offset && other.typeDesc.equals(typeDesc);
    }
}
