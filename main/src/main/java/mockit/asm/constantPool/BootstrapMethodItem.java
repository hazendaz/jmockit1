/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.constantPool;

import static mockit.asm.jvmConstants.ConstantPoolTypes.BSM;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class BootstrapMethodItem extends Item {
    @NonNegative
    final int position;

    /**
     * Initializes the new item with the given index, position and hash code.
     *
     * @param position
     *            position in byte in the class attribute "BootstrapMethods"
     * @param hashCode
     *            hashcode of the item, which is processed from the hashcode of the bootstrap method and the hashcode of
     *            all bootstrap arguments
     */
    public BootstrapMethodItem(@NonNegative int index, @NonNegative int position, int hashCode) {
        super(index);
        this.position = position;
        setHashCode(hashCode);
        type = BSM;
    }

    @Override
    boolean isEqualTo(@NonNull Item item) {
        return ((BootstrapMethodItem) item).position == position;
    }
}
