package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Component;

import org.junit.jupiter.api.Test;

/**
 * The Class MisusedFakingAPITest.
 */
final class MisusedFakingAPITest {

    // Lightweight test-only class to be mocked.
    public static class SimpleComponent {
        public int getComponentCount() {
            return 0;
        }
    }

    /**
     * Fake same method twice with reentrant fakes from two different fake classes.
     */
    @Test
    void fakeSameMethodTwiceWithReentrantFakesFromTwoDifferentFakeClasses() {
        new MockUp<SimpleComponent>() {
            @Mock
            int getComponentCount(Invocation inv) {
                int i = inv.proceed();
                return i + 1;
            }
        };

        int i = new SimpleComponent().getComponentCount();
        assertEquals(1, i);

        new MockUp<SimpleComponent>() {
            @Mock
            int getComponentCount(Invocation inv) {
                int j = inv.proceed();
                return j + 2;
            }
        };

        // Should return 3, but returns 5. Chaining mock methods is not supported.
        int j = new SimpleComponent().getComponentCount();
        assertEquals(5, j);
    }

    /**
     * The Class SimpleComponentFake.
     */
    static final class SimpleComponentFake extends MockUp<SimpleComponent> {

        /** The component count. */
        final int componentCount;

        /**
         * Instantiates a new simple component fake.
         *
         * @param componentCount
         *            the component count
         */
        SimpleComponentFake(int componentCount) {
            this.componentCount = componentCount;
        }

        /**
         * Gets the component count.
         *
         * @param inv
         *            the inv
         *
         * @return the component count
         */
        @Mock
        int getComponentCount(Invocation inv) {
            return componentCount;
        }
    }

    /**
     * Apply the same fake for A class twice.
     */
    @Test
    void applyTheSameFakeForAClassTwice() {
        new SimpleComponentFake(1);
        new SimpleComponentFake(2); // second application overrides the previous one

        assertEquals(2, new SimpleComponent().getComponentCount());
    }

    /**
     * Fake A private method.
     */
    @Test
    void fakeAPrivateMethod() {
        // Changed to allow fake private constructors.
        new MockUp<Component>() {
            @Mock
            boolean checkCoalescing() {
                return false;
            }
        };
    }

    /**
     * Fake A private constructor.
     */
    @Test
    public void fakeAPrivateConstructor() {
        // Changed to allow fake private constructors.
        new MockUp<System>() {
            @Mock
            void $init() {
            }
        };
    }
}
