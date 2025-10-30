package integration.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ClassNotExercised.
 */
public final class ClassNotExercised {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(ClassNotExercised.class);

    /**
     * Do something.
     *
     * @param i
     *            the i
     * @param s
     *            the s
     *
     * @return true, if successful
     */
    public boolean doSomething(int i, String s) {
        if (i > 0) {
            logger.info(s);
        }

        return !s.isEmpty();
    }
}
