package integration.tests;

/**
 * The Class IfElseStatements.
 */
@SuppressWarnings("ControlFlowStatementWithoutBraces")
public final class IfElseStatements {

    /**
     * Simple if.
     *
     * @param b
     *            the b
     */
    void simpleIf(boolean b) {
        if (b) {
            System.gc();
            System.runFinalization();
        }
    }

    /**
     * If and else.
     *
     * @param b
     *            the b
     */
    void ifAndElse(boolean b) {
        if (b) {
            System.gc();
        } else {
            System.runFinalization();
        }
    }

    /**
     * Single line if.
     *
     * @param b
     *            the b
     */
    void singleLineIf(boolean b) {
        // @formatter:off
        if (b) { System.gc(); }
        // @formatter:on
    }

    /**
     * Single line if and else.
     *
     * @param b
     *            the b
     */
    void singleLineIfAndElse(boolean b) {
        // @formatter:off
        if (b) { System.gc(); } else { System.runFinalization(); }
        // @formatter:on
    }

    /**
     * Method with four different paths and simple lines.
     *
     * @param b
     *            the b
     * @param i
     *            the i
     */
    void methodWithFourDifferentPathsAndSimpleLines(boolean b, int i) {
        if (b) {
            System.gc();
        } else {
            System.runFinalization();
        }

        if (i > 0) {
            System.gc();
        }
    }

    /**
     * Method with four different paths and segmented lines.
     *
     * @param b
     *            the b
     * @param i
     *            the i
     */
    void methodWithFourDifferentPathsAndSegmentedLines(boolean b, int i) {
        if (b) {
            System.gc();
        } else {
            System.runFinalization();
        }

        if (i > 0) {
            System.gc();
        } else {
            System.runFinalization();
        }
    }

    /**
     * If else with complex boolean condition.
     *
     * @param a
     *            the a
     * @param b
     *            the b
     *
     * @return true, if successful
     */
    boolean ifElseWithComplexBooleanCondition(boolean a, boolean b) {
        // noinspection RedundantIfStatement
        if (a || b) {
            return true;
        }
        return false;
    }

    /**
     * Return input.
     *
     * @param x
     *            the x
     * @param a
     *            the a
     * @param b
     *            the b
     * @param c
     *            the c
     *
     * @return the int
     */
    // Must return the same value of x as it was called with. Some paths will fail that requirement.
    @SuppressWarnings({ "AssignmentToMethodParameter" })
    int returnInput(int x, boolean a, boolean b, boolean c) {
        if (a) {
            x++;
        }

        if (b) {
            x--;
        }

        if (c) {
            // noinspection SillyAssignment
            x = x;
        }

        return x;
    }

    /**
     * Nested if.
     *
     * @param a
     *            the a
     * @param b
     *            the b
     *
     * @return the int
     */
    int nestedIf(boolean a, boolean b) {
        int i = 1;

        if (a && b) {
            i = 2;
        }

        return i;
    }

    /**
     * If else with nested if.
     *
     * @param a
     *            the a
     * @param b
     *            the b
     *
     * @return the int
     */
    int ifElseWithNestedIf(boolean a, boolean b) {
        int i = 1;

        if (!a) {
            return 3;
        }
        if (b) {
            i = 2;
        }

        return i;
    }

    /**
     * Nested if else.
     *
     * @param a
     *            the a
     * @param b
     *            the b
     *
     * @return the int
     */
    int nestedIfElse(boolean a, boolean b) {
        int i = 1;

        if (a) {
            if (b) {
                i = 2;
            } else {
                i = 3;
            }
        } else if (b) {
            i = 4;
        }

        return i;
    }

    /**
     * Block comment with method signature: infeasiblePaths(boolean a).
     *
     * @param a
     *            the a
     */
    void infeasiblePaths(boolean a) {
        if (a) {
            System.gc();
        }

        if (a) {
            System.runFinalization();
        }
    }

    /**
     * Another single line if and else.
     *
     * @param b
     *            the b
     *
     * @return the int
     */
    int anotherSingleLineIfAndElse(boolean b) {
        // @formatter:off
        int r; if (b) { r = 1; } else { r = 2; } return r;
        // @formatter:on
    }

    /**
     * Yet another single line if and else.
     *
     * @param b
     *            the b
     *
     * @return the int
     */
    int yetAnotherSingleLineIfAndElse(boolean b) {
        // @formatter:off
        if (b) { return 1; } return 2;
        // @formatter:on
    }

    /**
     * If with boolean and operator.
     *
     * @param b1
     *            the b 1
     * @param b2
     *            the b 2
     */
    void ifWithBooleanAndOperator(boolean b1, boolean b2) {
        if (b1 && b2) {
            System.gc();
        }
    }

    /**
     * If with boolean or operator.
     *
     * @param b1
     *            the b 1
     * @param b2
     *            the b 2
     */
    void ifWithBooleanOrOperator(boolean b1, boolean b2) {
        if (b1 || b2) {
            System.gc();
        }
    }

    /**
     * Another if with boolean and operator.
     *
     * @param b1
     *            the b 1
     * @param b2
     *            the b 2
     */
    void anotherIfWithBooleanAndOperator(boolean b1, boolean b2) {
        if (b1 && b2) {
            System.gc();
        }
    }

    /**
     * If spanning multiple lines.
     *
     * @param b
     *            the b
     * @param i
     *            the i
     */
    void ifSpanningMultipleLines(boolean b, int i) {
        // @formatter:off
        if (
           b ||
           i > 0
        ) {
            System.gc();
        }
        // @formatter:on
    }

    /**
     * Method to be called from custom runner test.
     *
     * @param s
     *            the s
     *
     * @return the class loader
     */
    ClassLoader methodToBeCalledFromCustomRunnerTest(String s) {
        instanceField = s;

        if (s.isEmpty()) {
            return null;
        }

        return getClass().getClassLoader();
    }

    /** The instance field. */
    String instanceField;
}
