package mockit.asm.constantPool;

import static mockit.asm.constantPool.TypeTableItem.SpecialType.MERGED;

import javax.annotation.Nonnull;

import org.checkerframework.checker.index.qual.NonNegative;

final class MergedTypeTableItem extends TypeTableItem {
    private int type1;
    private int type2;
    @NonNegative
    int commonSuperTypeIndex;

    MergedTypeTableItem() {
        type = MERGED;
    }

    MergedTypeTableItem(@Nonnull MergedTypeTableItem item) {
        super(0, item);
        type1 = item.type1;
        type2 = item.type2;
        commonSuperTypeIndex = item.commonSuperTypeIndex;
    }

    /**
     * Sets the types of this merged type table item.
     *
     * @param type1
     *            index of an internal name in the type table.
     * @param type2
     *            index of an internal name in the type table.
     */
    void set(@NonNegative int type1, @NonNegative int type2) {
        this.type1 = type1;
        this.type2 = type2;
        setHashCode(type1 + type2);
    }

    @Override
    boolean isEqualTo(@Nonnull Item item) {
        MergedTypeTableItem other = (MergedTypeTableItem) item;
        return other.type1 == type1 && other.type2 == type2;
    }
}
