/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.constantPool;

import static mockit.asm.jvmConstants.ConstantPoolTypes.NAME_TYPE;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class NameAndTypeItem extends TypeOrMemberItem {
    public NameAndTypeItem(@NonNegative int index) {
        super(index);
        type = NAME_TYPE;
    }

    NameAndTypeItem(@NonNegative int index, @NonNull NameAndTypeItem item) {
        super(index, item);
    }

    /**
     * Sets the name and type descriptor of this item.
     */
    public void set(@NonNull String name, @NonNull String desc) {
        setValuesAndHashcode(name, desc, 1);
    }
}
