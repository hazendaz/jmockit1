package mockit.asm.constantPool;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.checkerframework.checker.index.qual.NonNegative;

public final class ModuleItem extends Item {
    @NonNull
    @SuppressWarnings("NullableProblems")
    String strVal;

    ModuleItem() {
        super(0);
        strVal = "";
    }

    public ModuleItem(@NonNegative int index, int type, @NonNull String strVal) {
        super(index);
        set(type, strVal);
    }

    ModuleItem(@NonNegative int index, @NonNull ModuleItem item) {
        super(index, item);
        strVal = item.strVal;
    }

    @NonNull
    public String getValue() {
        return strVal;
    }

    /**
     * Sets this module name value.
     */
    void set(int type, @NonNull String strVal) {
        this.type = type;
        this.strVal = strVal;
        setHashCode(strVal.hashCode());
    }

    @Override
    boolean isEqualTo(@NonNull Item item) {
        return ((ModuleItem) item).strVal.equals(strVal);
    }
}
