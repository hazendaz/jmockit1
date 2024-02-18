package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;

import mockit.MockingMultipleInterfacesTest.Dependency;

import org.junit.Test;

/**
 * The Class MockingMultipleInterfacesTest.
 *
 * @param <MultiMock>
 *            the generic type
 */
public final class MockingMultipleInterfacesTest<MultiMock extends Dependency & Runnable> {

    /**
     * The Interface Dependency.
     */
    interface Dependency {
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

    /** The multi mock. */
    @Mocked
    MultiMock multiMock;

    /**
     * Mock field with two interfaces.
     */
    @Test
    public void mockFieldWithTwoInterfaces() {
        new Expectations() {
            {
                multiMock.doSomething(false);
                result = "test";
            }
        };

        multiMock.run();
        assertEquals("test", multiMock.doSomething(false));

        new Verifications() {
            {
                multiMock.run();
            }
        };
    }

    /**
     * Mock parameter with two interfaces.
     *
     * @param <M>
     *            the generic type
     * @param mock
     *            the mock
     */
    @Test
    public <M extends Dependency & Serializable> void mockParameterWithTwoInterfaces(@Mocked final M mock) {
        new Expectations() {
            {
                mock.doSomething(true);
                result = "test";
            }
        };

        assertEquals("test", mock.doSomething(true));
    }

    /**
     * The Interface Base.
     */
    public interface Base {
        /**
         * Do something.
         */
        void doSomething();
    }

    /**
     * The Class Derived.
     */
    abstract static class Derived implements Base {
        /**
         * Instantiates a new derived.
         */
        protected Derived() {
        }
    }

    /**
     * The Class ToBeMocked.
     */
    public abstract static class ToBeMocked extends Derived {
    }

    /**
     * Mock abstract method inherited from interface implemented by super class.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void mockAbstractMethodInheritedFromInterfaceImplementedBySuperClass(@Mocked final ToBeMocked mock) {
        mock.doSomething();

        new Verifications() {
            {
                mock.doSomething();
                times = 1;
            }
        };
    }
}
