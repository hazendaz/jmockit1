package mockit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;

/**
 * The Class TestedClassWithConstructorDI0Test.
 */
@SuppressWarnings({ "UnusedParameters", "ClassWithTooManyFields" })
public final class TestedClassWithConstructorDI0Test {

    /**
     * The Class TestedClassWithConstructorHavingPrimitiveParameter.
     */
    public static final class TestedClassWithConstructorHavingPrimitiveParameter {

        /**
         * Instantiates a new tested class with constructor having primitive parameter.
         *
         * @param i
         *            the i
         */
        public TestedClassWithConstructorHavingPrimitiveParameter(int i) {
            assertEquals(123, i);
        }
    }

    /**
     * The Class TestedClassWithConstructorHavingStringParameter.
     */
    public static final class TestedClassWithConstructorHavingStringParameter {

        /**
         * Instantiates a new tested class with constructor having string parameter.
         *
         * @param s
         *            the s
         */
        public TestedClassWithConstructorHavingStringParameter(String s) {
        }
    }

    /**
     * The Class TestedClassWithConstructorHavingArrayParameter.
     */
    public static final class TestedClassWithConstructorHavingArrayParameter {

        /**
         * Instantiates a new tested class with constructor having array parameter.
         *
         * @param arr
         *            the arr
         */
        public TestedClassWithConstructorHavingArrayParameter(String[] arr) {
            assertArrayEquals(new String[] { "abc", "Xyz" }, arr);
        }
    }

    /**
     * The Class TestedClassWithConstructorHavingLongParameters.
     */
    public static final class TestedClassWithConstructorHavingLongParameters {

        /**
         * Instantiates a new tested class with constructor having long parameters.
         *
         * @param l1
         *            the l 1
         * @param l2
         *            the l 2
         */
        @SuppressWarnings("unused")
        public TestedClassWithConstructorHavingLongParameters(long l1, long l2) {
            assertEquals(1, l1);
            assertEquals(2, l2);
        }

        /**
         * Instantiates a new tested class with constructor having long parameters.
         *
         * @param i
         *            the i
         * @param l1
         *            the l 1
         * @param l2
         *            the l 2
         */
        @SuppressWarnings("unused")
        TestedClassWithConstructorHavingLongParameters(int i, long l1, long l2) {
            throw new RuntimeException("Must not occur");
        }
    }

    /**
     * The Class TestedClassWithConstructorHavingDoubleParameters.
     */
    public static final class TestedClassWithConstructorHavingDoubleParameters {

        /**
         * Instantiates a new tested class with constructor having double parameters.
         *
         * @param d1
         *            the d 1
         * @param d2
         *            the d 2
         */
        TestedClassWithConstructorHavingDoubleParameters(double d1, double d2) {
            assertEquals(1.0, d1, 0);
            assertEquals(2.0, d2, 0);
        }
    }

    /**
     * The Class TestedClassWithConstructorHavingVarargsParameter.
     */
    public static final class TestedClassWithConstructorHavingVarargsParameter {

        /**
         * Instantiates a new tested class with constructor having varargs parameter.
         *
         * @param b
         *            the b
         * @param c
         *            the c
         * @param s
         *            the s
         * @param b2
         *            the b 2
         * @param flags
         *            the flags
         */
        public TestedClassWithConstructorHavingVarargsParameter(byte b, char c, String s, byte b2, boolean... flags) {
            assertEquals(56, b);
            assertEquals(57, b2);
            assertEquals('X', c);
            assertEquals("test", s);
            assertEquals(3, flags.length);
            assertTrue(flags[0]);
            assertFalse(flags[1]);
            assertTrue(flags[2]);
        }
    }

    /**
     * The Class TestedClassWithConstructorHavingDoubleSizeParameterFollowedByRegularParameters.
     */
    public static final class TestedClassWithConstructorHavingDoubleSizeParameterFollowedByRegularParameters {

        /**
         * Instantiates a new tested class with constructor having double size parameter followed by regular parameters.
         *
         * @param l1
         *            the l 1
         * @param c
         *            the c
         * @param s
         *            the s
         */
        public TestedClassWithConstructorHavingDoubleSizeParameterFollowedByRegularParameters(long l1, char c,
                String s) {
            assertEquals(1, l1);
            assertEquals('X', c);
            assertEquals("test", s);
        }
    }

    /** The tested 0. */
    @Tested
    TestedClassWithConstructorHavingPrimitiveParameter tested0;

    /** The tested 1. */
    @Tested
    TestedClassWithConstructorHavingStringParameter tested1;

    /** The tested 2. */
    @Tested
    TestedClassWithConstructorHavingArrayParameter tested2;

    /** The tested 3. */
    @Tested
    TestedClassWithConstructorHavingLongParameters tested3;

    /** The tested 4. */
    @Tested
    TestedClassWithConstructorHavingDoubleParameters tested4;

    /** The tested 5. */
    @Tested
    TestedClassWithConstructorHavingVarargsParameter tested5;

    /** The tested 6. */
    @Tested
    TestedClassWithConstructorHavingDoubleSizeParameterFollowedByRegularParameters tested6;

    /** The i. */
    @Injectable
    int i = 123;

    /** The unused. */
    @Injectable
    int unused;

    /** The l 1. */
    @Injectable
    long l1 = 1;

    /** The l 2. */
    @Injectable
    final long l2 = 2;

    /** The arr. */
    @Injectable
    String[] arr = { "abc", "Xyz" };

    /** The b. */
    @Injectable
    byte b = 56;

    /** The b 2. */
    @Injectable
    byte b2 = 57;

    /** The c. */
    @Injectable
    char c = 'X';

    /** The s. */
    @Injectable
    String s = "test";

    /** The d 1. */
    @Injectable
    double d1 = 1.0;

    /** The d 2. */
    @Injectable
    double d2 = 2.0;

    /** The first flag. */
    // For varargs parameter:
    @Injectable
    boolean firstFlag = true;

    /** The second flag. */
    @Injectable("false")
    boolean secondFlag;

    /** The third flag. */
    @Injectable
    boolean thirdFlag = true;

    /**
     * Verify instantiation of tested objects through constructors with non mocked parameters.
     */
    @Test
    public void verifyInstantiationOfTestedObjectsThroughConstructorsWithNonMockedParameters() {
        assertNotNull(tested0);
        assertNotNull(tested1);
        assertNotNull(tested2);
        assertNotNull(tested3);
        assertNotNull(tested4);
        assertNotNull(tested5);
        assertNotNull(tested6);
    }
}
