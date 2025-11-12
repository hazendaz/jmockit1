/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.constantPool;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

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
