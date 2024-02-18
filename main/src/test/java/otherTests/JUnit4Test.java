package otherTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import mockit.ClassWithObjectOverrides;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Invocation;
import mockit.Mock;
import mockit.Mocked;
import mockit.Verifications;

import org.junit.Test;

/**
 * The Class JUnit4Test.
 */
public final class JUnit4Test {

    /** The mock. */
    @Mocked
    ClassWithObjectOverrides mock;

    /**
     * Use mocked instance.
     */
    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @Test
    public void useMockedInstance() {
        new ClassWithObjectOverrides("test");
        assertFalse(mock.toString().isEmpty());
        mock.equals("123");
        // noinspection ObjectEqualsNull
        mock.equals(null);

        new Verifications() {
            {
                String s;
                mock.equals(s = withCapture());
                assertEquals("123", s);

                List<ClassWithObjectOverrides> objs = withCapture(new ClassWithObjectOverrides("test"));
                assertEquals(1, objs.size());

                mock.equals(withNull());
            }
        };
    }

    /**
     * The Class AnotherClass.
     */
    static class AnotherClass {

        /**
         * Do something.
         *
         * @param i
         *            the i
         * @param l
         *            the l
         * @param s
         *            the s
         * @param b
         *            the b
         * @param c
         *            the c
         * @param d
         *            the d
         * @param f
         *            the f
         * @param str
         *            the str
         */
        void doSomething(int i, long l, Short s, byte b, char c, double d, float f, String str) {
            System.out.println(i + l + s + b + d + f);
            System.out.println(c + str);
        }

        /**
         * Gets the modified value.
         *
         * @param s
         *            the s
         *
         * @return the modified value
         */
        String getModifiedValue(String s) {
            return s.toLowerCase();
        }
    }

    /**
     * Use argument matching fields.
     *
     * @param anotherMock
     *            the another mock
     */
    @Test
    public void useArgumentMatchingFields(@Injectable final AnotherClass anotherMock) {
        new Expectations() {
            {
                anotherMock.doSomething(anyInt, anyLong, anyShort, anyByte, anyChar, anyDouble, anyFloat, anyString);
            }
        };

        anotherMock.doSomething(1, 2, (short) 3, (byte) 4, 'c', 1.2, 2.5F, "");
    }

    /**
     * Use argument matching methods.
     *
     * @param anotherMock
     *            the another mock
     */
    @Test
    public void useArgumentMatchingMethods(@Injectable final AnotherClass anotherMock) {
        new Expectations() {
            {
                anotherMock.doSomething(withAny(0), withEqual(2L), withInstanceOf(short.class), withNotEqual((byte) 1),
                        withInstanceLike(' '), withEqual(1.2, 0), withEqual(2.5F, 0), withSameInstance("test"));

                anotherMock.getModifiedValue(withSubstring("abc"));
                result = "Abc";
                anotherMock.getModifiedValue(withPrefix("TX"));
                result = "abc";
                anotherMock.getModifiedValue(withSuffix("X"));
                result = "ABC";
                anotherMock.getModifiedValue(withMatch("\\d+"));
                result = "number";

                anotherMock.getModifiedValue("Delegate");
                result = new Delegate() {
                    @Mock
                    String delegate(Invocation inv, String s) {
                        assertNotNull(inv.getInvokedMember());
                        assertTrue(inv.getInvocationIndex() >= 0);
                        assertTrue(inv.getInvocationCount() >= 1);
                        assertEquals(1, inv.getInvokedArguments().length);
                        return inv.proceed();
                    }
                };
            }
        };

        anotherMock.doSomething(1, 2, (short) 3, (byte) 4, 'c', 1.2, 2.5F, "test");

        assertEquals("Abc", anotherMock.getModifiedValue("test abc xyz"));
        assertEquals("abc", anotherMock.getModifiedValue("TX test"));
        assertEquals("ABC", anotherMock.getModifiedValue("test X"));
        assertEquals("number", anotherMock.getModifiedValue("123"));
        assertEquals("delegate", anotherMock.getModifiedValue("Delegate"));

        new Verifications() {
            {
                List<String> values = new ArrayList<>();
                anotherMock.getModifiedValue(withCapture(values));
                assertEquals(5, values.size());
            }
        };
    }
}
