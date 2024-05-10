package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * The Class TestedClassWithConstructorDI2Test.
 */
// TODO JWL 2/18/2024 Test not allowed on jdk21
@Disabled
final class TestedClassWithConstructorDI2Test {

    /**
     * The Class TestedClass.
     */
    public static final class TestedClass implements Servlet {

        /** The counter. */
        static int counter;

        /** The config. */
        private ServletConfig config;

        /** The dependency 1. */
        private final Dependency dependency1;

        /** The dependency 2. */
        private final Dependency dependency2;

        /** The dependency 3. */
        private final Dependency dependency3;

        /**
         * Instantiates a new tested class.
         *
         * @param dependency1
         *            the dependency 1
         * @param r
         *            the r
         * @param dependency2
         *            the dependency 2
         * @param dependency3
         *            the dependency 3
         */
        public TestedClass(Dependency dependency1, Runnable r, Dependency dependency2, Dependency dependency3) {
            this.dependency1 = dependency1;
            this.dependency2 = dependency2;
            this.dependency3 = dependency3;
            r.run();

            int i = dependency1.doSomething();
            assert i == 123;

            try {
                InetAddress localHost = InetAddress.getLocalHost();
                assert localHost.getHostName() == null;
            } catch (UnknownHostException e) {
                throw new IllegalStateException("InetAddress should be mocked", e);
            }
        }

        /**
         * Do some operation.
         *
         * @return the int
         */
        public int doSomeOperation() {
            return dependency1.doSomething() + dependency2.doSomething();
        }

        @Override
        public ServletConfig getServletConfig() {
            return config;
        }

        @Override
        public void service(ServletRequest req, ServletResponse res) {
        }

        @Override
        public String getServletInfo() {
            return null;
        }

        @Override
        public void init(ServletConfig cfg) {
            config = cfg;
            counter++;

            int i = dependency1.doSomething();
            assert i == 123;

            checkInetAddressMocking();
        }

        /**
         * Check inet address mocking.
         */
        private void checkInetAddressMocking() {
            try {
                InetAddress inetAddress = InetAddress.getByName("testHost");
                assert inetAddress.getHostName() == null;
            } catch (UnknownHostException ignore) {
                counter = -1;
            }
        }

        @Override
        public void destroy() {
            counter++;
            checkInetAddressMocking();
        }
    }

    /**
     * The Class Dependency.
     */
    static class Dependency {
        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething() {
            return -1;
        }
    }

    /** The tested. */
    @Tested
    TestedClass tested;

    /** The task. */
    @Injectable
    Runnable task;

    /** The dependency 1. */
    @Injectable
    Dependency dependency1;

    /** The dependency 2. */
    @Injectable
    Dependency dependency2;

    /** The config. */
    @Injectable
    ServletConfig config;

    /** The test host. */
    @Mocked
    InetAddress testHost;

    /**
     * Reset counter.
     */
    @BeforeEach
    void resetCounter() {
        TestedClass.counter = 0;
        new Expectations() {
            {
                dependency1.doSomething();
                result = 123;
            }
        };
    }

    /**
     * Exercise tested object with dependencies of same type injected through constructor.
     *
     * @param dependency3
     *            the dependency 3
     */
    @Test
    void exerciseTestedObjectWithDependenciesOfSameTypeInjectedThroughConstructor(@Injectable Dependency dependency3) {
        assertTestedObjectWasInitialized();
        assertSame(dependency3, tested.dependency3);

        new Expectations() {
            {
                dependency1.doSomething();
                result = 23;
                dependency2.doSomething();
                result = 5;
            }
        };

        assertEquals(28, tested.doSomeOperation());
    }

    /**
     * Exercise tested object with extra injectable parameter.
     *
     * @param dependency3
     *            the dependency 3
     * @param mock4
     *            the mock 4
     */
    @Test
    void exerciseTestedObjectWithExtraInjectableParameter(@Injectable Dependency dependency3,
            @Injectable Dependency mock4) {
        assertTestedObjectWasInitialized();
        assertSame(dependency1, tested.dependency1);
        assertSame(dependency2, tested.dependency2);
        assertSame(dependency3, tested.dependency3);
    }

    /**
     * Assert tested object was initialized.
     */
    void assertTestedObjectWasInitialized() {
        assertSame(config, tested.getServletConfig());
        assertEquals(1, TestedClass.counter);
    }

    /**
     * Verify tested object after every test.
     */
    @AfterEach
    void verifyTestedObjectAfterEveryTest() {
        assertEquals(2, TestedClass.counter);
    }
}
