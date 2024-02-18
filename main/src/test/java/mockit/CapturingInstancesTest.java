package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Observable;
import java.util.concurrent.Callable;

import org.junit.Test;

/**
 * The Class CapturingInstancesTest.
 */
public final class CapturingInstancesTest {

    /**
     * The Interface Service1.
     */
    public interface Service1 {
        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething();
    }

    /**
     * The Class Service1Impl.
     */
    static final class Service1Impl implements Service1 {
        @Override
        public int doSomething() {
            return 1;
        }
    }

    /**
     * The Class TestedUnit.
     */
    public static final class TestedUnit {

        /** The service 1. */
        private final Service1 service1 = new Service1Impl();

        /** The service 2. */
        private final Service1 service2 = new Service1() {
            @Override
            public int doSomething() {
                return 2;
            }
        };

        /** The observable. */
        Observable observable;

        /**
         * Business operation.
         *
         * @param b
         *            the b
         *
         * @return the int
         */
        public int businessOperation(final boolean b) {
            new Callable() {
                @Override
                public Object call() {
                    throw new IllegalStateException();
                }
            }.call();

            observable = new Observable() {
                {
                    if (b) {
                        throw new IllegalArgumentException();
                    }
                }
            };

            return service1.doSomething() + service2.doSomething();
        }
    }

    /** The service. */
    @Capturing
    Service1 service;

    /**
     * Capture service instances created by tested constructor.
     */
    @Test
    public void captureServiceInstancesCreatedByTestedConstructor() {
        TestedUnit unit = new TestedUnit();

        assertEquals(0, unit.service1.doSomething());
        assertEquals(0, unit.service2.doSomething());
    }

    /**
     * Capture all internally created instances.
     *
     * @param observable
     *            the observable
     * @param callable
     *            the callable
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void captureAllInternallyCreatedInstances(@Capturing Observable observable,
            @Capturing final Callable<?> callable) throws Exception {
        new Expectations() {
            {
                service.doSomething();
                returns(3, 4);
            }
        };

        TestedUnit unit = new TestedUnit();
        int result = unit.businessOperation(true);
        assertEquals(4, unit.service1.doSomething());
        assertEquals(4, unit.service2.doSomething());

        assertNotNull(unit.observable);
        assertEquals(7, result);

        new Verifications() {
            {
                callable.call();
            }
        };
    }

    /**
     * The Interface Service2.
     */
    public interface Service2 {
        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething();
    }

    /**
     * The Class Base.
     */
    static class Base {
        /**
         * Do something.
         *
         * @return true, if successful
         */
        boolean doSomething() {
            return false;
        }
    }

    /**
     * The Class Derived.
     */
    static final class Derived extends Base {
        /**
         * Do something else.
         *
         * @return the service 2
         */
        Service2 doSomethingElse() {
            return null;
        }
    }

    /**
     * Capture subclass and cascade from method exclusive to subclass.
     *
     * @param capturingMock
     *            the capturing mock
     */
    @Test
    public void captureSubclassAndCascadeFromMethodExclusiveToSubclass(@Capturing Base capturingMock) {
        Derived d = new Derived();
        Service2 service2 = d.doSomethingElse();

        // Classes mocked only because they implement/extend a capturing base type do not cascade from methods
        // that exist only in them.
        assertNull(service2);
    }
}
