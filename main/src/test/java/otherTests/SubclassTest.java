package otherTests;

import static org.junit.jupiter.api.Assertions.assertFalse;

import mockit.Capturing;
import mockit.Expectations;
import mockit.Mocked;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * The Class SubclassTest.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class SubclassTest {

    /** The super class constructor called. */
    private static boolean superClassConstructorCalled;

    /** The sub class constructor called. */
    private static boolean subClassConstructorCalled;

    /**
     * The Class SuperClass.
     */
    public static class SuperClass {

        /** The name. */
        final String name;

        /**
         * Instantiates a new super class.
         *
         * @param x
         *            the x
         * @param name
         *            the name
         */
        public SuperClass(int x, String name) {
            this.name = name + x;
            superClassConstructorCalled = true;
        }
    }

    /**
     * The Class SubClass.
     */
    public static class SubClass extends SuperClass {

        /**
         * Instantiates a new sub class.
         *
         * @param name
         *            the name
         */
        public SubClass(String name) {
            super(name.length(), name);
            subClassConstructorCalled = true;
        }
    }

    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
        superClassConstructorCalled = false;
        subClassConstructorCalled = false;
    }

    /**
     * Capture subclass through classfile transformer.
     *
     * @param captured
     *            the captured
     */
    @Test
    public void captureSubclassThroughClassfileTransformer(@Capturing SuperClass captured) {
        new SubClass("capture");

        assertFalse(superClassConstructorCalled);
        assertFalse(subClassConstructorCalled);
    }

    /**
     * Capture subclass through redefinition of previously loaded classes.
     *
     * @param captured
     *            the captured
     */
    @Test
    public void captureSubclassThroughRedefinitionOfPreviouslyLoadedClasses(@Capturing SuperClass captured) {
        new SubClass("capture");

        assertFalse(superClassConstructorCalled);
        assertFalse(subClassConstructorCalled);
    }

    /**
     * Mock subclass using expectations with first super constructor.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void mockSubclassUsingExpectationsWithFirstSuperConstructor(@Mocked SubClass mock) {
        new Expectations() {
            {
                new SubClass("test");
            }
        };

        new SubClass("test");

        assertFalse(superClassConstructorCalled);
        assertFalse(subClassConstructorCalled);
    }
}
