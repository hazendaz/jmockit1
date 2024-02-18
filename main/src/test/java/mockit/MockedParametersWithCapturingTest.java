package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.CharBuffer;

import org.junit.Test;

/**
 * The Class MockedParametersWithCapturingTest.
 */
public final class MockedParametersWithCapturingTest {

    /**
     * The Interface Service.
     */
    public interface Service {

        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething();

        /**
         * Do something else.
         *
         * @param i
         *            the i
         */
        void doSomethingElse(int i);
    }

    /**
     * The Class ServiceImpl.
     */
    static final class ServiceImpl implements Service {

        /** The str. */
        final String str;

        /**
         * Instantiates a new service impl.
         *
         * @param str
         *            the str
         */
        ServiceImpl(String str) {
            this.str = str;
        }

        @Override
        public int doSomething() {
            return 1;
        }

        @Override
        public void doSomethingElse(int i) {
            throw new IllegalMonitorStateException();
        }
    }

    /**
     * The Class BaseClass.
     */
    static class BaseClass {

        /** The str. */
        final String str;

        /**
         * Instantiates a new base class.
         */
        BaseClass() {
            str = "";
        }

        /**
         * Instantiates a new base class.
         *
         * @param str
         *            the str
         */
        BaseClass(String str) {
            this.str = str;
        }

        /**
         * Gets the str.
         *
         * @return the str
         */
        String getStr() {
            return str;
        }

        /**
         * Do something.
         */
        void doSomething() {
            throw new IllegalStateException("Invalid state");
        }
    }

    /**
     * The Class DerivedClass.
     */
    static class DerivedClass extends BaseClass {

        /**
         * Instantiates a new derived class.
         */
        DerivedClass() {
        }

        /**
         * Instantiates a new derived class.
         *
         * @param str
         *            the str
         */
        DerivedClass(String str) {
            super(str);
        }

        @Override
        String getStr() {
            return super.getStr().toUpperCase();
        }
    }

    /**
     * Capture derived class.
     *
     * @param service
     *            the service
     */
    @Test
    public void captureDerivedClass(@Capturing BaseClass service) {
        assertNull(new DerivedClass("test").str);
        assertNull(new DerivedClass() {
        }.str);
    }

    /**
     * Capture implementations of different interfaces.
     *
     * @param mock1
     *            the mock 1
     * @param mock2
     *            the mock 2
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void captureImplementationsOfDifferentInterfaces(@Capturing Runnable mock1, @Capturing Readable mock2)
            throws Exception {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                throw new RuntimeException("run");
            }
        };
        runnable.run();

        Readable readable = new Readable() {
            @Override
            public int read(CharBuffer cb) {
                throw new RuntimeException("read");
            }
        };
        readable.read(CharBuffer.wrap("test"));
    }

    /**
     * Capture implementations of an interface.
     *
     * @param service
     *            the service
     */
    @Test
    public void captureImplementationsOfAnInterface(@Capturing final Service service) {
        Service impl1 = new ServiceImpl("test1");
        impl1.doSomethingElse(1);

        Service impl2 = new Service() {
            @Override
            public int doSomething() {
                return 2;
            }

            @Override
            public void doSomethingElse(int i) {
                throw new IllegalStateException("2");
            }
        };
        impl2.doSomethingElse(2);
    }

    /**
     * Capture subclasses of A base class.
     *
     * @param base
     *            the base
     */
    @Test
    public void captureSubclassesOfABaseClass(@Capturing final BaseClass base) {
        BaseClass impl1 = new DerivedClass("test1");
        impl1.doSomething();

        BaseClass impl2 = new BaseClass("test2") {
            @Override
            void doSomething() {
                throw new IllegalStateException("2");
            }
        };
        impl2.doSomething();

        final class DerivedClass2 extends DerivedClass {
            DerivedClass2() {
                super("DeRiVed");
            }

            @Override
            String getStr() {
                return super.getStr().toLowerCase();
            }
        }
        DerivedClass2 impl3 = new DerivedClass2();
        impl3.doSomething();
    }

    /**
     * The Interface IBase.
     */
    public interface IBase {
        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething();
    }

    /**
     * The Interface ISub.
     */
    public interface ISub extends IBase {
    }

    /**
     * Record call to base interface method on capture sub interface implementation.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void recordCallToBaseInterfaceMethodOnCaptureSubInterfaceImplementation(@Capturing final ISub mock) {
        new Expectations() {
            {
                mock.doSomething();
                result = 123;
            }
        };

        ISub impl = new ISub() {
            @Override
            public int doSomething() {
                return -1;
            }
        };
        int i = impl.doSomething();

        assertEquals(123, i);
    }
}
