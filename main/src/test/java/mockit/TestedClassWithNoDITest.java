package mockit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * The Class TestedClassWithNoDITest.
 */
public final class TestedClassWithNoDITest {

    /**
     * The Class TestedClass.
     */
    public static final class TestedClass {

        /** The dependency. */
        private final Dependency dependency = new Dependency();

        /**
         * Do some operation.
         *
         * @return true, if successful
         */
        public boolean doSomeOperation() {
            return dependency.doSomething() > 0;
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

    /** The tested 1. */
    @Tested
    TestedClass tested1;

    /** The tested 2. */
    @Tested
    final TestedClass tested2 = new TestedClass();

    /** The tested 3. */
    @Tested
    TestedClass tested3;

    /** The tested 4. */
    @Tested
    NonPublicTestedClass tested4;

    /** The tested 5. */
    @Tested
    final TestedClass tested5 = null;

    /** The mock. */
    @Mocked
    Dependency mock;

    /** The tested. */
    TestedClass tested;

    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
        assertNotNull(mock);
        assertNull(tested);
        tested = new TestedClass();
        assertNull(tested3);
        tested3 = tested;
        assertNull(tested1);
        assertNotNull(tested2);
        assertNull(tested4);
        assertNull(tested5);
    }

    /**
     * Verify tested fields.
     */
    @Test
    public void verifyTestedFields() {
        assertNull(tested5);
        assertNotNull(tested4);
        assertNotNull(tested3);
        assertSame(tested, tested3);
        assertNotNull(tested2);
        assertNotNull(tested1);
    }

    /**
     * Exercise automatically instantiated tested object.
     */
    @Test
    public void exerciseAutomaticallyInstantiatedTestedObject() {
        new Expectations() {
            {
                mock.doSomething();
                result = 1;
            }
        };

        assertTrue(tested1.doSomeOperation());
    }

    /**
     * Exercise manually instantiated tested object.
     */
    @Test
    public void exerciseManuallyInstantiatedTestedObject() {
        new Expectations() {
            {
                mock.doSomething();
                result = 1;
            }
        };

        assertTrue(tested2.doSomeOperation());

        new FullVerifications() {
        };
    }

    /**
     * Exercise another manually instantiated tested object.
     */
    @Test
    public void exerciseAnotherManuallyInstantiatedTestedObject() {
        assertFalse(tested3.doSomeOperation());

        new Verifications() {
            {
                mock.doSomething();
                times = 1;
            }
        };
    }
}

class NonPublicTestedClass {
    @SuppressWarnings("RedundantNoArgConstructor")
    NonPublicTestedClass() {
    }
}
