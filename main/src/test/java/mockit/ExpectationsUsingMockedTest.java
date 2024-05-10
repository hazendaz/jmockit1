package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.BeanInfo;
import java.beans.Introspector;

import org.junit.jupiter.api.Test;

/**
 * The Class ExpectationsUsingMockedTest.
 */
final class ExpectationsUsingMockedTest {

    /**
     * The Interface Dependency.
     */
    public interface Dependency {
        /**
         * Do something.
         *
         * @param b
         *            the b
         *
         * @return the string
         */
        String doSomething(boolean b);
    }

    /**
     * The Class AbstractBase.
     */
    public abstract static class AbstractBase {

        /**
         * Adds the.
         *
         * @param i
         *            the i
         *
         * @return true, if successful
         */
        protected abstract boolean add(Integer i);

        /**
         * Do something.
         *
         * @return the int
         */
        final int doSomething() {
            return -1;
        }
    }

    /** The base. */
    @Mocked
    AbstractBase base;

    /**
     * Multiple mock parameters of same mocked type.
     *
     * @param dependency1
     *            the dependency 1
     * @param dependency2
     *            the dependency 2
     */
    @Test
    void multipleMockParametersOfSameMockedType(@Mocked final Dependency dependency1,
            @Mocked final Dependency dependency2) {
        new Expectations() {
            {
                dependency1.doSomething(true);
                result = "1";
                dependency2.doSomething(false);
                result = "2";
            }
        };

        assertEquals("1", dependency1.doSomething(true));
        assertNull(dependency1.doSomething(false));
        assertEquals("2", dependency2.doSomething(false));
        assertNull(dependency2.doSomething(true));
    }

    /**
     * Mock field for abstract class.
     */
    @Test
    void mockFieldForAbstractClass() {
        new Expectations() {
            {
                base.add(1);
                result = true;
            }
        };

        assertFalse(base.add(0));
        assertTrue(base.add(1));
        assertFalse(base.add(2));
    }

    static class ClassWithStaticInitializer {
        static boolean initialized = true;

        static int initialized() {
            return initialized ? 1 : -1;
        }
    }

    @Test
    public void stubOutStaticInitializersWhenSpecified(
            @Mocked(stubOutClassInitialization = true) ClassWithStaticInitializer unused) {
        assertEquals(0, ClassWithStaticInitializer.initialized());
        assertFalse(ClassWithStaticInitializer.initialized);
    }

    /**
     * The Class ClassWithStaticInitializer2.
     */
    static class ClassWithStaticInitializer2 {

        /** The initialized. */
        static boolean initialized = true;

        /**
         * Initialized.
         *
         * @return the int
         */
        static int initialized() {
            return initialized ? 1 : -1;
        }
    }

    /**
     * Do not stub out static initializers by default.
     *
     * @param unused
     *            the unused
     */
    @Test
    void doNotStubOutStaticInitializersByDefault(@Mocked ClassWithStaticInitializer2 unused) {
        assertEquals(0, ClassWithStaticInitializer2.initialized());
        assertTrue(ClassWithStaticInitializer2.initialized);
    }

    /**
     * The Class AnotherClassWithStaticInitializer.
     */
    static class AnotherClassWithStaticInitializer {

        /** The initialized. */
        static boolean initialized = true;

        /**
         * Initialized.
         *
         * @return the int
         */
        static int initialized() {
            return initialized ? 1 : -1;
        }
    }

    /**
     * Mock everything without stubbing static initializers.
     *
     * @param unused
     *            the unused
     */
    @Test
    void mockEverythingWithoutStubbingStaticInitializers(@Mocked AnotherClassWithStaticInitializer unused) {
        assertEquals(0, AnotherClassWithStaticInitializer.initialized());
        assertTrue(AnotherClassWithStaticInitializer.initialized);
    }

    static class AnotherClassWithStaticInitializer2 {
        static boolean initialized = true;

        static int initialized() {
            return initialized ? 1 : -1;
        }
    }

    @Test
    @SuppressWarnings("DefaultAnnotationParam")
    public void avoidStubbingStaticInitializersThroughSpecificAnnotationAttribute(
            @Mocked(stubOutClassInitialization = false) AnotherClassWithStaticInitializer2 unused) {
        assertEquals(0, AnotherClassWithStaticInitializer2.initialized());
        assertTrue(AnotherClassWithStaticInitializer2.initialized);
    }

    /**
     * The Class InnerClass.
     */
    static class InnerClass {
        /**
         * Gets the value.
         *
         * @return the value
         */
        int getValue() {
            return -1;
        }
    }

    /**
     * Mock inner class.
     *
     * @param innerMock
     *            the inner mock
     */
    @Test
    void mockInnerClass(@Mocked final InnerClass innerMock) {
        assertEquals(0, innerMock.getValue());

        new Expectations() {
            {
                innerMock.getValue();
                result = 123;
                times = 1;
            }
        };

        assertEquals(123, new InnerClass().getValue());
    }

    /**
     * The Class SubClass.
     */
    static final class SubClass extends AbstractBase {
        @Override
        protected boolean add(Integer i) {
            return false;
        }
    }

    /**
     * Record method from abstract base class and replay on subclass.
     */
    @Test
    void recordMethodFromAbstractBaseClassAndReplayOnSubclass() {
        new Expectations() {
            {
                base.doSomething();
                result = 1;
            }
        };

        assertEquals(1, base.doSomething());
        assertEquals(-1, new SubClass().doSomething());
    }

    /**
     * The Interface BusinessInterface.
     */
    public interface BusinessInterface {
    }

    /**
     * Gets the bean info from mocked interface.
     *
     * @param mock
     *            the mock
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void beanInfoFromMockedInterface(@Mocked BusinessInterface mock) throws Exception {
        Class<? extends BusinessInterface> mockClass = mock.getClass();

        BeanInfo info = Introspector.getBeanInfo(mockClass);

        assertNotNull(info);
    }

    /**
     * The Class GenericBase.
     *
     * @param <B>
     *            the generic type
     */
    static class GenericBase<B extends Runnable> {
        /**
         * Base.
         *
         * @return the b
         */
        public B base() {
            return null;
        }
    }

    /**
     * The Class GenericSubclass.
     *
     * @param <S>
     *            the generic type
     */
    public static final class GenericSubclass<S extends Runnable> extends GenericBase<S> {
        /* bridge method here */ }

    /**
     * Record expectation on base method having A synthetic bridge method in subclass.
     *
     * @param mock
     *            the mock
     */
    @Test
    void recordExpectationOnBaseMethodHavingASyntheticBridgeMethodInSubclass(@Mocked final GenericSubclass<?> mock) {
        new Expectations() {
            {
                mock.base();
                result = null;
            }
        };

        assertNull(mock.base());
    }
}
