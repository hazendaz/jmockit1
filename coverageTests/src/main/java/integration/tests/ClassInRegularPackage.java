package integration.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ClassInRegularPackage.
 */
public class ClassInRegularPackage {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(ClassInRegularPackage.class);

    /** The Constant CONSTANT. */
    public static final int CONSTANT = 123;

    /**
     * The Enum NestedEnum.
     */
    public enum NestedEnum {

        /** The First. */
        FIRST,

        /** The Second. */
        SECOND() {
            @Override
            public String toString() {
                return "2nd";
            }
        };

        static {
            logger.info("test");
        }
    }

    /**
     * Do something.
     *
     * @param value
     *            the value
     *
     * @return true, if successful
     */
    public boolean doSomething(NestedEnum value) {
        switch (value) {
            case FIRST:
                return true;

            case SECOND:
                value.toString();
                break;
        }

        return value.ordinal() == CONSTANT;
    }
}
