package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class TestedClassWithFieldDITest.
 */
public final class TestedClassWithFieldDITest {

    /** The tested global. */
    @Tested
    TestedClassInGlobalScopeTest.TestedClass testedGlobal;

    /**
     * Use local tested field having same type and name as global tested field in previous test class.
     */
    @Test
    public void useLocalTestedFieldHavingSameTypeAndNameAsGlobalTestedFieldInPreviousTestClass() {
        assertNull(testedGlobal.someValue);
    }

    /**
     * The Class UtilityClass.
     */
    static final class UtilityClass {

        /** The name. */
        String name;

        /** The id. */
        int id;

        /** The action. */
        Runnable action;

        /** The collaborator. */
        Collaborator collaborator;
    }

    /**
     * The Class TestedClass.
     */
    public static class TestedClass {

        /** The global action. */
        static Runnable globalAction;

        /** The i. */
        protected final int i;

        /** The dependency. */
        protected Dependency dependency;

        /**
         * Instantiates a new tested class.
         */
        public TestedClass() {
            i = -1;
        }

        /**
         * Instantiates a new tested class.
         *
         * @param i
         *            the i
         */
        public TestedClass(int i) {
            this.i = i;
        }

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

    /**
     * The Class Collaborator.
     */
    public static class Collaborator {
    }

    /** The util. */
    @Tested(availableDuringSetup = true)
    UtilityClass util;

    /** The util 2. */
    @Tested(availableDuringSetup = true, fullyInitialized = true)
    UtilityClass util2;

    /** The util name. */
    @Injectable("util")
    String utilName;

    /** The tested. */
    @Tested
    TestedClass tested;

    /** The dependency. */
    @Injectable
    Dependency dependency;

    /**
     * Sets the up.
     */
    @Before
    public void setUp() {
        assertUtilObjectsAreAvailable();
    }

    /**
     * Assert util objects are available.
     */
    void assertUtilObjectsAreAvailable() {
        assertNotNull(util);
        assertEquals("util", util.name);
        assertNull(util.collaborator);

        assertNotNull(util2);
        assertEquals("util", util2.name);
        assertNotNull(util2.collaborator);
    }

    /**
     * Tear down.
     */
    @After
    public void tearDown() {
        assertUtilObjectsAreAvailable();
    }

    /**
     * Exercise tested object with field injected by type.
     */
    @Test
    public void exerciseTestedObjectWithFieldInjectedByType() {
        assertEquals(-1, tested.i);
        assertSame(dependency, tested.dependency);

        new Expectations() {
            {
                dependency.doSomething();
                result = 23;
                times = 1;
            }
        };

        assertTrue(tested.doSomeOperation());
    }

    /**
     * Exercise tested object created through constructor and field injection.
     *
     * @param value
     *            the value
     */
    @Test
    public void exerciseTestedObjectCreatedThroughConstructorAndFieldInjection(@Injectable("123") int value) {
        assertEquals(0, util.id);
        assertEquals(123, tested.i);
        assertSame(dependency, tested.dependency);
    }

    /**
     * Ignore static fields when doing field injection.
     *
     * @param action
     *            the action
     */
    @Test
    public void ignoreStaticFieldsWhenDoingFieldInjection(@Injectable Runnable action) {
        assertNull(util.action);
        assertNull(TestedClass.globalAction);
    }

    /** The id. */
    @Tested("123")
    int id;

    /** The tested 2. */
    @Tested
    UtilityClass tested2;

    /**
     * Creates the tested object injecting it with value provided in previous tested field.
     */
    @Test
    public void createTestedObjectInjectingItWithValueProvidedInPreviousTestedField() {
        assertEquals(123, tested2.id);
    }
}
