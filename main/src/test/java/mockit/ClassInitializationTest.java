package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.Before;
import org.junit.Test;

/**
 * The Class ClassInitializationTest.
 */
public final class ClassInitializationTest {

    /**
     * The Class ClassWithStaticInitializer.
     */
    static class ClassWithStaticInitializer {

        /** The Constant CONSTANT. */
        static final Object CONSTANT = "not a compile-time constant";

        /** The variable. */
        static String variable;
        static {
            variable = doSomething();
        }

        /**
         * Do something.
         *
         * @return the string
         */
        static String doSomething() {
            return "real value";
        }
    }

    /**
     * Mock class with static initializer.
     *
     * @param mocked
     *            the mocked
     */
    @Test
    public void mockClassWithStaticInitializer(@Mocked ClassWithStaticInitializer mocked) {
        // noinspection ConstantJUnitAssertArgument
        assertNotNull(ClassWithStaticInitializer.CONSTANT);
        assertNull(ClassWithStaticInitializer.doSomething());
        assertEquals("real value", ClassWithStaticInitializer.variable);
    }

    /**
     * The Class ClassWhichCallsStaticMethodFromInitializer.
     */
    static class ClassWhichCallsStaticMethodFromInitializer {
        static {
            String s = someValue();
            s.length();
        }

        /**
         * Some value.
         *
         * @return the string
         */
        static String someValue() {
            return "some value";
        }
    }

    /**
     * Mock uninitialized class.
     *
     * @param unused
     *            the unused
     */
    @Test
    public void mockUninitializedClass(@Mocked ClassWhichCallsStaticMethodFromInitializer unused) {
        assertNull(ClassWhichCallsStaticMethodFromInitializer.someValue());
    }

    /**
     * The Interface BaseType.
     */
    public interface BaseType {
        /**
         * Some value.
         *
         * @return the string
         */
        String someValue();
    }

    /**
     * The Class NestedImplementationClass.
     */
    static final class NestedImplementationClass implements BaseType {
        static {
            new NestedImplementationClass().someValue().length();
        }

        @Override
        public String someValue() {
            return "some value";
        }
    }

    /**
     * Load nested implementation class.
     */
    @Before
    public void loadNestedImplementationClass() {
        // Ensure the class gets loaded, but not initialized, before it gets mocked.
        // The HotSpot VM would (for some reason) already have loaded it, but the J9 VM would not.
        NestedImplementationClass.class.getName();
    }

    /**
     * Mock uninitialized implementation class.
     *
     * @param mockBase
     *            the mock base
     */
    @Test
    public void mockUninitializedImplementationClass(@Capturing BaseType mockBase) {
        BaseType obj = new NestedImplementationClass();

        assertNull(obj.someValue());
    }

    /**
     * The Class Dependency.
     */
    static class Dependency {
        /**
         * Creates the.
         *
         * @return the dependency
         */
        static Dependency create() {
            return null;
        }
    }

    /**
     * The Class Dependent.
     */
    static class Dependent {

        /** The Constant DEPENDENCY. */
        static final Dependency DEPENDENCY = Dependency.create();
        static {
            DEPENDENCY.toString();
        }
    }

    /**
     * The Class AnotherDependent.
     */
    static class AnotherDependent {

        /** The Constant DEPENDENCY. */
        static final Dependency DEPENDENCY = Dependency.create();
        static {
            DEPENDENCY.toString();
        }
    }

    /** The dependency. */
    @Mocked
    Dependency dependency;

    /** The dependent. */
    @Mocked
    Dependent dependent;

    /**
     * Mock another dependent class.
     *
     * @param anotherDependent
     *            the another dependent
     */
    @Test
    public void mockAnotherDependentClass(@Mocked AnotherDependent anotherDependent) {
        assertNotNull(Dependent.DEPENDENCY);
        assertNotNull(AnotherDependent.DEPENDENCY);
    }

    /**
     * The Interface BaseInterface.
     */
    public interface BaseInterface {
        /** The do not remove. */
        Object DO_NOT_REMOVE = "Testing";
    }

    /**
     * The Interface SubInterface.
     */
    public interface SubInterface extends BaseInterface {
    }

    /** The mock. */
    @Mocked
    SubInterface mock;

    /**
     * Verify class initializer for mocked base interface.
     */
    @Test
    public void verifyClassInitializerForMockedBaseInterface() {
        assertNotNull(mock);
        assertEquals("Testing", BaseInterface.DO_NOT_REMOVE);
    }

    /**
     * The Class ClassWhichCallsMethodOnItselfFromInitializer.
     */
    static final class ClassWhichCallsMethodOnItselfFromInitializer {

        /** The Constant value. */
        static final Integer value = value();

        /**
         * Value.
         *
         * @return the integer
         */
        static Integer value() {
            return null;
        }
    }

    /**
     * Mock class which calls method on itself from initializer.
     *
     * @param unused
     *            the unused
     */
    @Test
    public void mockClassWhichCallsMethodOnItselfFromInitializer(
            @Mocked ClassWhichCallsMethodOnItselfFromInitializer unused) {
        assertNotNull(ClassWhichCallsMethodOnItselfFromInitializer.value());
        assertNull(ClassWhichCallsMethodOnItselfFromInitializer.value);
    }

    /**
     * The Interface InterfaceWithStaticInitializer.
     */
    interface InterfaceWithStaticInitializer {
        /** The constant. */
        Object CONSTANT = "test";
    }

    /**
     * The Class AbstractImpl.
     */
    @SuppressWarnings({ "AbstractClassWithoutAbstractMethods", "StaticInheritance" })
    public abstract static class AbstractImpl implements InterfaceWithStaticInitializer {
    }

    /**
     * Mock abstract class implementing interface with static initializer.
     *
     * @param mock2
     *            the mock 2
     */
    @Test // failed on JDK 9+ only
    public void mockAbstractClassImplementingInterfaceWithStaticInitializer(@Mocked AbstractImpl mock2) {
        assertEquals("test", InterfaceWithStaticInitializer.CONSTANT);
    }
}
