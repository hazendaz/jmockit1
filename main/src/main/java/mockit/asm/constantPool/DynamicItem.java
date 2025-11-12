/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.constantPool;

import edu.umd.cs.findbugs.annotations.NonNull;

import mockit.asm.jvmConstants.ConstantPoolTypes;

import org.checkerframework.checker.index.qual.NonNegative;

public final class DynamicItem extends TypeOrMemberItem {
    @NonNegative
    int bsmIndex;

    public DynamicItem(@NonNegative int index) {
        super(index);
    }

    DynamicItem(@NonNegative int index, @NonNull DynamicItem item) {
        super(index, item);
        bsmIndex = item.bsmIndex;
    }

    /**
     * Sets the type, name, desc, and index of the constant or invoke dynamic item.
     *
     * @param type
     *            one of {@link ConstantPoolTypes#INVOKE_DYNAMIC} or {@link ConstantPoolTypes#DYNAMIC}, for invoke or
     *            constant dynamic, respectively
     * @param name
     *            the item name
     * @param desc
     *            the item type descriptor
     * @param index
     *            zero based index into the class attribute "BootstrapMethods".
     */
    public void set(int type, @NonNull String name, @NonNull String desc, @NonNegative int index) {
        super.type = type;
        bsmIndex = index;
        setValuesAndHashcode(name, desc, index);
    }

    @Override
    boolean isEqualTo(@NonNull Item item) {
        DynamicItem other = (DynamicItem) item;
        return other.bsmIndex == bsmIndex && isEqualTo(other);
    }
}
