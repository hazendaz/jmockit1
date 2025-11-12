/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests;

/**
 * The Class UnreachableStatements.
 */
public final class UnreachableStatements {

    /**
     * Non branching method with unreachable lines.
     */
    void nonBranchingMethodWithUnreachableLines() {
        int a = 1;
        assert false;
        System.gc();
    }

    /**
     * Branching method with unreachable lines.
     *
     * @param a
     *            the a
     */
    void branchingMethodWithUnreachableLines(int a) {
        if (a > 0) {
            assert false;
            System.gc();
        }

        System.runFinalization();
    }
}
