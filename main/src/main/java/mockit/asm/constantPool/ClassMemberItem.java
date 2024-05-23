package mockit.asm.constantPool;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ClassMemberItem extends TypeOrMemberItem {
    @NonNull
    String owner;

    public ClassMemberItem(@NonNegative int index) {
        super(index);
        owner = "";
    }

    ClassMemberItem(@NonNegative int index, @NonNull ClassMemberItem item) {
        super(index, item);
        owner = item.owner;
    }

    /**
     * Sets the values of this field/method item.
     */
    public void set(int type, @NonNull String owner, @NonNull String name, @NonNull String desc) {
        this.type = type;
        this.owner = owner;
        setValuesAndHashcode(name, desc, owner.hashCode());
    }

    @Override
    boolean isEqualTo(@NonNull Item item) {
        ClassMemberItem other = (ClassMemberItem) item;
        return other.owner.equals(owner) && isEqualTo(other);
    }
}
