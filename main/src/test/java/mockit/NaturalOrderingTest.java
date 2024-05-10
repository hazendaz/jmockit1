package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

/**
 * The Class NaturalOrderingTest.
 */
final class NaturalOrderingTest {

    /**
     * Verify standard comparable behavior in mocked class.
     *
     * @param a
     *            the a
     * @param b
     *            the b
     */
    @Test
    void verifyStandardComparableBehaviorInMockedClass(@Mocked Date a, @Mocked Date b) {
        // noinspection EqualsWithItself
        assertEquals(0, a.compareTo(a));
        // noinspection EqualsWithItself
        assertEquals(0, b.compareTo(b));

        int aXb = a.compareTo(b);
        assertTrue(aXb != 0);

        int bXa = b.compareTo(a);
        assertTrue(bXa != 0);

        assertEquals(aXb, -bXa);
    }

    /**
     * The Class ComparableClass.
     */
    @SuppressWarnings("ComparableImplementedButEqualsNotOverridden")
    static final class ComparableClass implements Comparable<String> {

        /** The value. */
        final String value;

        /**
         * Instantiates a new comparable class.
         *
         * @param value
         *            the value
         */
        ComparableClass(String value) {
            this.value = value;
        }

        @Override
        public int compareTo(@Nonnull String s) {
            return value.compareTo(s);
        }
    }

    /**
     * Mock override of compare to method.
     *
     * @param a
     *            the a
     * @param b
     *            the b
     */
    @Test
    void mockOverrideOfCompareToMethod(@Mocked final ComparableClass a, @Mocked final ComparableClass b) {
        new Expectations() {
            {
                a.compareTo(null);
                result = 5;
                a.compareTo(anyString);
                result = 123;
            }
        };

        new Expectations() {
            {
                b.compareTo("test");
                result = -50;
            }
        };

        assertEquals(5, a.compareTo(null));
        assertEquals(123, a.compareTo("test"));
        assertEquals(-50, b.compareTo("test"));
    }

    /**
     * Mock override of compare to method in JRE class.
     *
     * @param a
     *            the a
     * @param b
     *            the b
     */
    @Test
    void mockOverrideOfCompareToMethodInJREClass(@Mocked final Date a, @Mocked final Date b) {
        new Expectations() {
            {
                a.compareTo(b);
                result = 5;
            }
        };

        assertEquals(5, a.compareTo(b));
        assertTrue(b.compareTo(a) != 0);

        new Verifications() {
            {
                a.compareTo((Date) any);
                b.compareTo(a);
            }
        };
    }
}
