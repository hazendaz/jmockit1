/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.asm.constantPool;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class PackageItem extends Item {
    @NonNull
    @SuppressWarnings("NullableProblems")
    String strVal;

    PackageItem() {
        super(0);
        strVal = "";
    }

    public PackageItem(@NonNegative int index, int type, @NonNull String strVal) {
        super(index);
        set(type, strVal);
    }

    PackageItem(@NonNegative int index, @NonNull PackageItem item) {
        super(index, item);
        strVal = item.strVal;
    }

    @NonNull
    public String getValue() {
        return strVal;
    }

    /**
     * Sets this package name value.
     */
    void set(int type, @NonNull String strVal) {
        this.type = type;
        this.strVal = strVal;
        setHashCode(strVal.hashCode());
    }

    @Override
    boolean isEqualTo(@NonNull Item item) {
        return ((PackageItem) item).strVal.equals(strVal);
    }
}
