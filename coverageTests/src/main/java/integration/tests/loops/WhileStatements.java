/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests.loops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhileStatements {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(WhileStatements.class);

    void whileBlockInSeparateLines() {
        int i = 0;

        while (i < 5) {
            i++;
        }
    }

    void whileBlockInSingleLine(int i) {
        // @formatter:off
        while (i < 2) { i++; }
        // @formatter:on
    }

    int whileWithContinue(int i) {
        while (i < 2) {
            if (i == 1) {
                i = 3;
                continue;
            }

            i++;
        }

        return i;
    }

    int whileWithBreak(int i) {
        while (i < 2) {
            if (i == 1) {
                break;
            }

            i++;
        }

        return i;
    }

    void nestedWhile(int i, int j) {
        while (j < 2) {
            while (i < j) {
                i++;
            }

            j++;
        }
    }

    void doWhileInSeparateLines() {
        int i = 0;

        do {
            i++;
        } while (i < 3);
    }

    void bothKindsOfWhileCombined(int i, int j) {
        while (true) {
            do {
                i++;
            }
            // @formatter:off
            while (i < j);
            // @formatter:on

            j++;

            if (j >= 2) {
                return;
            }
        }
    }

    void whileTrueEndingWithAnIf(int i) {
        while (true) {
            i++;

            if (i >= 2) {
                return;
            }
        }
    }

    void whileTrueStartingWithAnIf(int i) {
        while (true) {
            if (i >= 2) {
                return;
            }

            i++;
        }
    }

    void whileTrueWithoutExitCondition() {
        while (true) {
            doSomething();
        }
    }

    void whileTrueContainingTryFinally() {
        while (true) {
            try {
                doSomething();
            } finally {
                doNothing();
            }
        }
    }

    private static void doSomething() {
        throw new IllegalStateException();
    }

    private static void doNothing() {
    }

    int whileWithIfElse(int i) {
        while (i <= 2) {
            if (i % 2 == 0) {
                logger.info("even");
            } else {
                logger.info("odd");
            }

            i++;
        }

        return i;
    }
}
