package integration.tests;

/**
 * The Class BooleanExpressions.
 */
public final class BooleanExpressions {

    /**
     * Eval 1.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     *
     * @return true, if successful
     */
    public boolean eval1(boolean x, boolean y, int z) {
        return x && (y || z > 0);
    }

    /**
     * Eval 2.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     *
     * @return true, if successful
     */
    public boolean eval2(boolean x, boolean y, int z) {
        return x && (y || z > 0);
    }

    /**
     * Eval 3.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     *
     * @return true, if successful
     */
    public boolean eval3(boolean x, boolean y, boolean z) {
        return x && (y || z); // LOAD 1 IFEQ L1, LOAD 2 IFNE L2, LOAD 3 IFEQ L1, [L2 1 GOTO L3], [L1 0 L3 RETURN]
    }

    /**
     * Eval 4.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     *
     * @return true, if successful
     */
    public boolean eval4(boolean x, boolean y, boolean z) {
        return x && (!y || z);
    }

    /**
     * Eval 5.
     *
     * @param a
     *            the a
     * @param b
     *            the b
     * @param c
     *            the c
     *
     * @return true, if successful
     */
    public boolean eval5(boolean a, boolean b, boolean c) {
        if (a) {
            return true;
        }
        if (b || c) {
            return false;
        }

        return !c;
    }

    /**
     * Checks if is same type ignoring auto boxing.
     *
     * @param firstType
     *            the first type
     * @param secondType
     *            the second type
     *
     * @return true, if is same type ignoring auto boxing
     */
    static boolean isSameTypeIgnoringAutoBoxing(Class<?> firstType, Class<?> secondType) {
        return firstType == secondType || firstType.isPrimitive() && isWrapperOfPrimitiveType(firstType, secondType)
                || secondType.isPrimitive() && isWrapperOfPrimitiveType(secondType, firstType);
    }

    /**
     * Checks if is wrapper of primitive type.
     *
     * @param primitiveType
     *            the primitive type
     * @param otherType
     *            the other type
     *
     * @return true, if is wrapper of primitive type
     */
    static boolean isWrapperOfPrimitiveType(Class<?> primitiveType, Class<?> otherType) {
        return primitiveType == int.class && otherType == Integer.class
                || primitiveType == long.class && otherType == Long.class
                || primitiveType == double.class && otherType == Double.class
                || primitiveType == float.class && otherType == Float.class
                || primitiveType == boolean.class && otherType == Boolean.class;
    }

    /**
     * Simply returns input.
     *
     * @param b
     *            the b
     *
     * @return true, if successful
     */
    public boolean simplyReturnsInput(boolean b) {
        return b;
    }

    /**
     * Returns negated input.
     *
     * @param b
     *            the b
     *
     * @return true, if successful
     */
    public boolean returnsNegatedInput(boolean b) {
        return !b; // LOAD 1 IFNE L1, 1 GOTO L2, L1 0 L2 RETURN
    }

    /**
     * Returns trivial result from input after if else.
     *
     * @param b
     *            the b
     * @param i
     *            the i
     *
     * @return true, if successful
     */
    public boolean returnsTrivialResultFromInputAfterIfElse(boolean b, int i) {
        String s;

        if (b) {
            s = "one";
        } else {
            s = "two";
        }

        return i != 0; // LOAD 2 IFEQ L1, 1 GOTO L2, L1 0 L2 RETURN
    }

    /**
     * Returns result previously computed from input.
     *
     * @param b
     *            the b
     * @param i
     *            the i
     *
     * @return true, if successful
     */
    public boolean returnsResultPreviouslyComputedFromInput(boolean b, int i) {
        String s = b ? "a" : "b";
        boolean res;

        if (i != 0) {
            res = true;
        } else {
            res = false;
            System.out.checkError();
        }

        return res;
    }

    /**
     * Method with too many conditions for path analysis.
     *
     * @param i
     *            the i
     * @param j
     *            the j
     * @param b
     *            the b
     *
     * @return true, if successful
     */
    public boolean methodWithTooManyConditionsForPathAnalysis(int i, int j, boolean b) {
        if (i > 0 && j < 5 || (b ? i > 1 : j > 5) || (i <= 3 || j >= 4) && b) {
            return i + j == 3 == b;
        }
        if (i < 0 || j < 0) {
            return i < j;
        }

        return b;
    }

    /**
     * Returns negated input from local variable.
     *
     * @param b
     *            the b
     *
     * @return true, if successful
     */
    public boolean returnsNegatedInputFromLocalVariable(boolean b) {
        return !b; // LOAD 1 IFNE L1, [1 GOTO L2], [L1 0], L2 STORE 2, L3 LOAD 2 RETURN
    }
}
