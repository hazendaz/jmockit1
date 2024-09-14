package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * The Class FakeForGenericsTest.
 */
final class FakeForGenericsTest {

    /**
     * The Class Collaborator.
     */
    public static final class Collaborator {

        /**
         * Generic method.
         *
         * @param <N>
         *            the number type
         * @param n
         *            the n
         *
         * @return the n
         */
        public <N extends Number> N genericMethod(@SuppressWarnings("UnusedParameters") N n) {
            return null;
        }
    }

    /**
     * Fake generic method.
     */
    @Test
    void fakeGenericMethod() {
        new MockUp<Collaborator>() {
            @Mock
            <T extends Number> T genericMethod(T t) {
                return t;
            }

            // This also works (same erasure):
            // @Mock Number genericMethod(Number t) { return t; }
        };

        Integer n = new Collaborator().genericMethod(123);
        assertEquals(123, n.intValue());

        Long l = new Collaborator().genericMethod(45L);
        assertEquals(45L, l.longValue());

        Short s = new Collaborator().genericMethod((short) 6);
        assertEquals(6, s.shortValue());

        Double d = new Collaborator().genericMethod(0.5);
        assertEquals(0.5, d, 0);
    }

    /**
     * The Class GenericClass.
     *
     * @param <T1>
     *            the generic type
     * @param <T2>
     *            the generic type
     */
    @SuppressWarnings("UnusedParameters")
    public static final class GenericClass<T1, T2> {

        /**
         * A method.
         *
         * @param t
         *            the t
         */
        public void aMethod(T1 t) {
            throw new RuntimeException("t=" + t);
        }

        /**
         * Another method.
         *
         * @param t
         *            the t
         * @param i
         *            the i
         * @param p
         *            the p
         *
         * @return the int
         */
        public int anotherMethod(T1 t, int i, T2 p) {
            return 2 * i;
        }

        /**
         * Another method.
         *
         * @param t
         *            the t
         * @param i
         *            the i
         * @param p
         *            the p
         *
         * @return the int
         */
        public int anotherMethod(Integer t, int i, String p) {
            return -2 * i;
        }
    }

    /**
     * Fake generic class with unspecified type arguments.
     */
    @Test
    void fakeGenericClassWithUnspecifiedTypeArguments() {
        new MockUp<GenericClass<?, ?>>() {
            @Mock
            void aMethod(Object o) {
                StringBuilder s = (StringBuilder) o;
                s.setLength(0);
                s.append("fake");
                s.toString();
            }

            @Mock
            int anotherMethod(Object o, int i, Object list) {
                assertTrue(o instanceof StringBuilder);
                // noinspection unchecked
                assertEquals(0, ((Collection<String>) list).size());
                return -i;
            }
        };

        StringBuilder s = new StringBuilder("test");
        GenericClass<StringBuilder, List<String>> g = new GenericClass<>();

        g.aMethod(s);
        int r1 = g.anotherMethod(new StringBuilder("test"), 58, Collections.<String> emptyList());
        int r2 = g.anotherMethod(123, 65, "abc");

        assertEquals("fake", s.toString());
        assertEquals(-58, r1);
        assertEquals(-130, r2);
    }

    /**
     * Fake both generic and non generic methods in generic class.
     */
    @Test
    void fakeBothGenericAndNonGenericMethodsInGenericClass() {
        new MockUp<GenericClass<String, Boolean>>() {
            @Mock
            int anotherMethod(Integer t, int i, String p) {
                return 2;
            }

            @Mock
            int anotherMethod(String t, int i, Boolean p) {
                return 1;
            }
        };

        GenericClass<String, Boolean> o = new GenericClass<>();
        assertEquals(1, o.anotherMethod("generic", 1, true));
        assertEquals(2, o.anotherMethod(123, 2, "non generic"));
    }

    /**
     * The Class GenericBaseClass.
     *
     * @param <T>
     *            the generic type
     * @param <U>
     *            the generic type
     */
    static class GenericBaseClass<T, U> {
        /**
         * Find.
         *
         * @param id
         *            the id
         *
         * @return the u
         */
        public U find(@SuppressWarnings("UnusedParameters") T id) {
            return null;
        }
    }

    /**
     * Fake generic method with fake method having parameter types matching type arguments.
     */
    @Test
    void fakeGenericMethodWithFakeMethodHavingParameterTypesMatchingTypeArguments() {
        new MockUp<GenericBaseClass<String, Integer>>() {
            @Mock
            Integer find(String id) {
                return id.hashCode();
            }
        };

        int i = new GenericBaseClass<String, Integer>().find("test");
        assertEquals("test".hashCode(), i);
    }

    /**
     * Cannot call generic method when some fake method expects different types.
     */
    @Test
    void cannotCallGenericMethodWhenSomeFakeMethodExpectsDifferentTypes() {
        new MockUp<GenericBaseClass<String, Integer>>() {
            @Mock
            Integer find(String id) {
                return 1;
            }
        };

        try {
            new GenericBaseClass<Integer, String>().find(1);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().startsWith("Failure to invoke method: "));
        }
    }

    /**
     * The Class NonGenericSuperclass.
     */
    static class NonGenericSuperclass extends GenericBaseClass<Integer, String> {
    }

    /**
     * The Class NonGenericSubclass.
     */
    static final class NonGenericSubclass extends NonGenericSuperclass {
    }

    /**
     * Fake generic method from instantiation of non generic subclass.
     */
    @Test
    void fakeGenericMethodFromInstantiationOfNonGenericSubclass() {
        new MockUp<NonGenericSubclass>() {
            @Mock
            String find(Integer id) {
                return "faked" + id;
            }
        };

        String s = new NonGenericSubclass().find(1);
        assertEquals("faked1", s);
    }

    /**
     * The Class GenericSuperclass.
     *
     * @param <I>
     *            the generic type
     */
    static class GenericSuperclass<I> extends GenericBaseClass<I, String> {
    }

    /**
     * The Class AnotherNonGenericSubclass.
     */
    static final class AnotherNonGenericSubclass extends GenericSuperclass<Integer> {
    }

    /**
     * Fake generic method from instantiation of non generic subclass which extends A generic intermediate superclass.
     */
    @Test
    void fakeGenericMethodFromInstantiationOfNonGenericSubclassWhichExtendsAGenericIntermediateSuperclass() {
        new MockUp<AnotherNonGenericSubclass>() {
            @Mock
            String find(Integer id) {
                return "faked" + id;
            }
        };

        String s = new AnotherNonGenericSubclass().find(1);
        assertEquals("faked1", s);
    }

    /**
     * The Class NonGenericClassWithGenericMethods.
     */
    @SuppressWarnings("UnusedParameters")
    public static class NonGenericClassWithGenericMethods {

        /**
         * Static method.
         *
         * @param <T>
         *            the generic type
         * @param cls
         *            the cls
         * @param s
         *            the s
         *
         * @return the t
         */
        public static <T> T staticMethod(Class<T> cls, String s) {
            throw new RuntimeException();
        }

        /**
         * Instance method.
         *
         * @param <C>
         *            the generic type
         * @param cls
         *            the cls
         * @param s
         *            the s
         */
        public <C> void instanceMethod(Class<C> cls, String s) {
            throw new RuntimeException();
        }

        /**
         * Instance method.
         *
         * @param <N>
         *            the number type
         * @param cls
         *            the cls
         */
        public final <N extends Number> void instanceMethod(Class<N> cls) {
            throw new RuntimeException();
        }
    }

    /**
     * Fake generic methods of non generic class.
     */
    @Test
    void fakeGenericMethodsOfNonGenericClass() {
        new MockUp<NonGenericClassWithGenericMethods>() {
            @Mock
            <T> T staticMethod(Class<T> cls, String s) {
                return null;
            }

            @Mock
            <C> void instanceMethod(Class<C> cls, String s) {
            }

            @Mock
            void instanceMethod(Class<?> cls) {
            }
        };

        new NonGenericClassWithGenericMethods().instanceMethod(Integer.class);
        NonGenericClassWithGenericMethods.staticMethod(Collaborator.class, "test1");
        new NonGenericClassWithGenericMethods().instanceMethod(Byte.class, "test2");
    }
}
