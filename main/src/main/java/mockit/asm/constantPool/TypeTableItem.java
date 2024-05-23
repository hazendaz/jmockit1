package mockit.asm.constantPool;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

abstract class TypeTableItem extends Item {
    /**
     * Defines constants for {@link #NORMAL normal}, {@link #UNINIT uninitialized}, and {@link #MERGED merged} special
     * item types stored in the {@linkplain ConstantPoolGeneration#typeTable constant pool's type table}, instead of the
     * constant pool, in order to avoid clashes with normal constant pool items in the
     * {@linkplain ConstantPoolGeneration#items constant pool's hash table}.
     */
    interface SpecialType {
        int NORMAL = 30;
        int UNINIT = 31;
        int MERGED = 32;
    }

    @NonNull
    String typeDesc;

    TypeTableItem() {
        super(0);
        typeDesc = "";
    }

    TypeTableItem(@NonNegative int index, @NonNull TypeTableItem item) {
        super(index, item);
        typeDesc = item.typeDesc;
    }
}
