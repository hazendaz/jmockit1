package mockit.asm.constantPool;

import static mockit.asm.jvmConstants.ConstantPoolTypes.NAME_TYPE;

import javax.annotation.Nonnull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class NameAndTypeItem extends TypeOrMemberItem {
    public NameAndTypeItem(@NonNegative int index) {
        super(index);
        type = NAME_TYPE;
    }

    NameAndTypeItem(@NonNegative int index, @Nonnull NameAndTypeItem item) {
        super(index, item);
    }

    /**
     * Sets the name and type descriptor of this item.
     */
    public void set(@Nonnull String name, @Nonnull String desc) {
        setValuesAndHashcode(name, desc, 1);
    }
}
