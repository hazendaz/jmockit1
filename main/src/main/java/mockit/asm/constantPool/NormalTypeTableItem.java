package mockit.asm.constantPool;

import static mockit.asm.constantPool.TypeTableItem.SpecialType.NORMAL;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

final class NormalTypeTableItem extends TypeTableItem {
    NormalTypeTableItem() {
        type = NORMAL;
    }

    NormalTypeTableItem(@NonNegative int index, @NonNull NormalTypeTableItem item) {
        super(index, item);
    }

    /**
     * Sets the type of this normal type table item.
     *
     * @param type
     *            the internal name to be added to the type table.
     */
    void set(@NonNull String type) {
        typeDesc = type;
        setHashCode(type.hashCode());
    }

    @Override
    boolean isEqualTo(@NonNull Item item) {
        return ((TypeTableItem) item).typeDesc.equals(typeDesc);
    }
}
