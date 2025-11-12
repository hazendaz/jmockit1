/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.constantPool;

import static mockit.asm.constantPool.TypeTableItem.SpecialType.NORMAL;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

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
