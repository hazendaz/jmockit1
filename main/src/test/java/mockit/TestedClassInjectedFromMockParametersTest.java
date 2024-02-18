package mockit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Test;

/**
 * The Class TestedClassInjectedFromMockParametersTest.
 */
public final class TestedClassInjectedFromMockParametersTest {

    /**
     * The Enum AnEnum.
     */
    enum AnEnum {
        /** The Abc. */
        Abc,
        /** The Xyz. */
        Xyz
    }

    /**
     * The Class TestedClass.
     */
    public static final class TestedClass {

        /** The i. */
        private int i;

        /** The s. */
        private String s;

        /** The b. */
        private boolean b;

        /** The chars. */
        private char[] chars;

        /** The enum value. */
        AnEnum enumValue;

        /**
         * Instantiates a new tested class.
         *
         * @param b
         *            the b
         */
        public TestedClass(boolean b) {
            this.b = b;
        }

        /**
         * Instantiates a new tested class.
         *
         * @param i
         *            the i
         * @param s
         *            the s
         * @param b
         *            the b
         * @param chars
         *            the chars
         */
        public TestedClass(int i, String s, boolean b, char... chars) {
            this.i = i;
            this.s = s;
            this.b = b;
            this.chars = chars;
        }

        /**
         * Instantiates a new tested class.
         *
         * @param b1
         *            the b 1
         * @param b2
         *            the b 2
         * @param b3
         *            the b 3
         */
        public TestedClass(boolean b1, byte b2, boolean b3) {
            b = b1;
            chars = new char[] { (char) b2, b3 ? 'X' : 'x' };
        }

        /**
         * Instantiates a new tested class.
         *
         * @param first
         *            the first
         * @param second
         *            the second
         * @param third
         *            the third
         */
        public TestedClass(char first, char second, char third) {
            chars = new char[] { first, second, third };
        }
    }

    /** The tested. */
    @Tested
    TestedClass tested;

    /**
     * Attempt to instantiate tested class with no injectables.
     */
    @Test(expected = IllegalArgumentException.class)
    public void attemptToInstantiateTestedClassWithNoInjectables() {
    }

    /**
     * Instantiate tested object from mock parameters using one constructor.
     *
     * @param s
     *            the s
     * @param mock1
     *            the mock 1
     * @param mock2
     *            the mock 2
     * @param c1
     *            the c 1
     * @param c2
     *            the c 2
     */
    @Test
    public void instantiateTestedObjectFromMockParametersUsingOneConstructor(@Injectable("Text") String s,
            @Injectable("123") int mock1, @Injectable("true") boolean mock2, @Injectable("A") char c1,
            @Injectable("bB") char c2) {
        assertEquals("Text", s);
        assertEquals(s, tested.s);
        assertEquals(mock1, tested.i);
        assertEquals(mock2, tested.b);
        assertEquals(2, tested.chars.length);
        assertEquals(c1, tested.chars[0]);
        assertEquals(c2, tested.chars[1]);
        assertEquals('b', c2);
    }

    /**
     * Instantiate tested object from mock parameters using another constructor.
     *
     * @param b1
     *            the b 1
     * @param b3
     *            the b 3
     * @param b2
     *            the b 2
     */
    @Test
    public void instantiateTestedObjectFromMockParametersUsingAnotherConstructor(@Injectable("true") boolean b1,
            @Injectable("true") boolean b3, @Injectable("65") byte b2) {
        assertTrue(tested.b);
        assertEquals('A', tested.chars[0]);
        assertEquals('X', tested.chars[1]);
    }

    /**
     * Instantiate tested object using constructor with multiple parameters of the same type matched by name.
     *
     * @param second
     *            the second
     * @param third
     *            the third
     * @param first
     *            the first
     */
    @Test
    public void instantiateTestedObjectUsingConstructorWithMultipleParametersOfTheSameTypeMatchedByName(
            @Injectable("S") char second, @Injectable("T") char third, @Injectable("F") char first) {
        assertArrayEquals(new char[] { 'F', 'S', 'T' }, tested.chars);
    }

    /**
     * Sets the enum field in tested object from value provided in parameter.
     *
     * @param flag
     *            the flag
     * @param enumVal
     *            the enum val
     */
    @Test
    public void setEnumFieldInTestedObjectFromValueProvidedInParameter(@Injectable("false") boolean flag,
            @Injectable("Xyz") AnEnum enumVal) {
        assertSame(AnEnum.Xyz, tested.enumValue);
    }
}
