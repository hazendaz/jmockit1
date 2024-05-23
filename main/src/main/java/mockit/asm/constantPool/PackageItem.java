package mockit.asm.constantPool;

import javax.annotation.Nonnull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class PackageItem extends Item {
    @Nonnull
    @SuppressWarnings("NullableProblems")
    String strVal;

    PackageItem() {
        super(0);
        strVal = "";
    }

    public PackageItem(@NonNegative int index, int type, @Nonnull String strVal) {
        super(index);
        set(type, strVal);
    }

    PackageItem(@NonNegative int index, @Nonnull PackageItem item) {
        super(index, item);
        strVal = item.strVal;
    }

    @Nonnull
    public String getValue() {
        return strVal;
    }

    /**
     * Sets this package name value.
     */
    void set(int type, @Nonnull String strVal) {
        this.type = type;
        this.strVal = strVal;
        setHashCode(strVal.hashCode());
    }

    @Override
    boolean isEqualTo(@Nonnull Item item) {
        return ((PackageItem) item).strVal.equals(strVal);
    }
}
