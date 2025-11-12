/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.constantPool;

import edu.umd.cs.findbugs.annotations.NonNull;

import mockit.asm.types.JavaType;

import org.checkerframework.checker.index.qual.NonNegative;

public class TypeOrMemberItem extends Item {
    @NonNull
    String name;
    @NonNull
    String desc;
    @NonNegative
    private int argSize;

    TypeOrMemberItem(@NonNegative int index) {
        super(index);
        name = desc = "";
    }

    TypeOrMemberItem(@NonNegative int index, @NonNull TypeOrMemberItem item) {
        super(index, item);
        name = item.name;
        desc = item.desc;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getDesc() {
        return desc;
    }

    /**
     * Sets the name and type descriptor of this item, and computes its hashcode.
     */
    final void setValuesAndHashcode(@NonNull String name, @NonNull String desc, @NonNegative int hashCodeMultiplier) {
        this.name = name;
        this.desc = desc;
        setHashCode(hashCodeMultiplier * name.hashCode() * desc.hashCode());
    }

    @Override
    boolean isEqualTo(@NonNull Item item) {
        return isEqualTo((TypeOrMemberItem) item);
    }

    final boolean isEqualTo(@NonNull TypeOrMemberItem item) {
        return item.name.equals(name) && item.desc.equals(desc);
    }

    /**
     * Recovers the stack size variation from this constant pool item, computing and storing it if needed. The
     * {@link #argSize} field stores the sizes of the arguments and of the return value corresponding to
     * <code>desc</code>.
     */
    @NonNegative
    public final int getArgSizeComputingIfNeeded(@NonNull String methodDesc) {
        int thisArgSize = argSize;

        if (thisArgSize == 0) {
            thisArgSize = JavaType.getArgumentsAndReturnSizes(methodDesc);
            argSize = thisArgSize;
        }

        return thisArgSize;
    }
}
