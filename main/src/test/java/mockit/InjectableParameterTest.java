package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

/**
 * The Class InjectableParameterTest.
 */
final class InjectableParameterTest {

    /**
     * Injectable parameters of primitive types.
     *
     * @param i
     *            the i
     * @param l
     *            the l
     * @param s
     *            the s
     * @param b
     *            the b
     * @param f1
     *            the f 1
     * @param f2
     *            the f 2
     * @param c
     *            the c
     * @param f
     *            the f
     * @param d
     *            the d
     */
    @Test
    void injectableParametersOfPrimitiveTypes(@Injectable("123") int i, @Injectable("5") long l,
            @Injectable("-45 ") short s, @Injectable(" 127") byte b, @Injectable("true") boolean f1,
            @Injectable(" TRUE ") boolean f2, @Injectable("A") char c, @Injectable(" 1.23") float f,
            @Injectable("-1.23") double d) {
        assertEquals(123, i);
        assertEquals(5L, l);
        assertEquals(-45, s);
        assertEquals(127, b);
        assertTrue(f1);
        assertTrue(f2);
        assertEquals('A', c);
        assertEquals(1.23F, f, 0.0F);
        assertEquals(-1.23, d, 0.0);
    }

    /**
     * Injectable parameters of wrapper types.
     *
     * @param i
     *            the i
     * @param l
     *            the l
     * @param s
     *            the s
     * @param b
     *            the b
     * @param f1
     *            the f 1
     * @param f2
     *            the f 2
     * @param c
     *            the c
     * @param f
     *            the f
     * @param d
     *            the d
     */
    @Test
    void injectableParametersOfWrapperTypes(@Injectable("123") Integer i, @Injectable("5") Long l,
            @Injectable("-45 ") Short s, @Injectable(" 127") Byte b, @Injectable("true") Boolean f1,
            @Injectable(" TRUE ") Boolean f2, @Injectable("A") Character c, @Injectable(" 1.23") Float f,
            @Injectable("-1.23") Double d) {
        assertEquals(123, (int) i);
        assertEquals(5L, (long) l);
        assertEquals(-45, (short) s);
        assertEquals(127, (byte) b);
        assertTrue(f1);
        assertTrue(f2);
        assertEquals('A', (char) c);
        assertEquals(1.23F, f, 0.0F);
        assertEquals(-1.23, d, 0.0);
    }

    /**
     * Injectable parameters of other number subtypes.
     *
     * @param d
     *            the d
     * @param i
     *            the i
     * @param ai
     *            the ai
     * @param al
     *            the al
     */
    @Test
    void injectableParametersOfOtherNumberSubtypes(@Injectable(" 10.234") BigDecimal d,
            @Injectable("123 ") BigInteger i, @Injectable(" 4567 ") AtomicInteger ai,
            @Injectable(" 23434") AtomicLong al) {
        assertEquals(10.234, d.doubleValue(), 0.0);
        assertEquals(123, i.intValue());
        assertEquals(4567, ai.intValue());
        assertEquals(23434L, al.longValue());
    }

    /**
     * The Class TestedClass.
     */
    static class TestedClass {
        /** The n. */
        Number n;
        /** The text. */
        CharSequence text;
        /** The cmp. */
        Comparable<Float> cmp;
    }

    /**
     * Injectables having subtypes of tested fields.
     *
     * @param tested
     *            the tested
     * @param s
     *            the s
     * @param cmp
     *            the cmp
     * @param n
     *            the n
     */
    @Test
    void injectablesHavingSubtypesOfTestedFields(@Tested TestedClass tested, @Injectable("test") String s,
            @Injectable("1.2") Float cmp, @Injectable("123") Integer n) {
        assertEquals(123, tested.n.intValue());
        assertEquals("test", tested.text);
        assertEquals(1.2F, tested.cmp);
    }

    /**
     * The Class TestedClass2.
     */
    static class TestedClass2 {

        /** The n. */
        final Number n;

        /** The text. */
        final CharSequence text;

        /** The cmp. */
        final Comparable<Float> cmp;

        /**
         * Instantiates a new tested class 2.
         *
         * @param n
         *            the n
         * @param text
         *            the text
         * @param cmp
         *            the cmp
         */
        TestedClass2(Number n, CharSequence text, Comparable<Float> cmp) {
            this.n = n;
            this.text = text;
            this.cmp = cmp;
        }
    }

    /**
     * Injectables having subtypes of tested constructor parameters.
     *
     * @param tested
     *            the tested
     * @param cmp
     *            the cmp
     * @param s
     *            the s
     * @param n
     *            the n
     */
    @Test
    void injectablesHavingSubtypesOfTestedConstructorParameters(@Tested TestedClass2 tested,
            @Injectable("1.2") Float cmp, @Injectable("test") String s, @Injectable("123") Integer n) {
        assertEquals(123, tested.n.intValue());
        assertEquals("test", tested.text);
        assertEquals(1.2F, tested.cmp);
    }
}
