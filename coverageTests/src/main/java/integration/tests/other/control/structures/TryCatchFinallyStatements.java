/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests.other.control.structures;

public final class TryCatchFinallyStatements {
    void tryCatch() {
        try {
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean tryCatchWhichThrowsAndCatchesException() {
        try {
            throw new RuntimeException("testing");
        } catch (RuntimeException e) {
            return true;
        }
    }

    int regularTryFinally(boolean b) {
        try {
            if (b) {
                return 1;
            }
            return 0;
        } finally {
            System.gc();
        }
    }

    // very different from javac with Eclipse compiler
    boolean finallyBlockWhichCannotCompleteNormally(boolean b) {
        while (b) {
            try {
                return true;
            } finally {
                break;
            }
        }
        return false;
    }

    int whileTrueWithTryFinallyInsideAnother() {
        int i = 0;
        while (true) {
            try {
                try {
                    i = 1;
                    // the first finally clause
                } finally {
                    i = 2;
                }
                i = 3;
                // this never completes, because of the continue
                return i;
                // the second finally clause
            } finally {
                if (i == 3) {
                    // this continue overrides the return statement
                    continue;
                }
            }
        }
    }

    void finallyBlockContainingIfWithBodyInSameLine() {
        boolean b = false;

        try {
            toString();
        } finally {
            if (b) {
                toString();
            }
        }
    }
}
