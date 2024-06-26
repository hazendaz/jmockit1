package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The Class ClassInitializationTest.
 */
final class ClassInitializationTest {

    static final class ClassWhichFailsAtInitialization {
        static {
            // noinspection ConstantConditions
            if (true) {
                throw new AssertionError();
            }
        }

        static int value() {
            return 0;
        }
    }

    @Test
    public void usingExpectations(@Mocked(stubOutClassInitialization = true) ClassWhichFailsAtInitialization unused) {
        new Expectations() {
            {
                ClassWhichFailsAtInitialization.value();
                result = 1;
            }
        };

        assertEquals(1, ClassWhichFailsAtInitialization.value());
    }

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
    void mockClassWithStaticInitializerNotStubbedOut(@Mocked ClassWithStaticInitializer mocked) {
        // noinspection ConstantJUnitAssertArgument
        assertNotNull(ClassWithStaticInitializer.CONSTANT);
        assertNull(ClassWithStaticInitializer.doSomething());
        assertEquals("real value", ClassWithStaticInitializer.variable);
    }

    static class AnotherClassWithStaticInitializer {
        static final Object CONSTANT = "not a compile-time constant";
        static {
            doSomething();
        }

        static void doSomething() {
            throw new UnsupportedOperationException("must not execute");
        }

        int getValue() {
            return -1;
        }
    }

    @Test
    public void mockClassWithStaticInitializerStubbedOut(
            @Mocked(stubOutClassInitialization = true) AnotherClassWithStaticInitializer mockAnother) {
        // noinspection ConstantJUnitAssertArgument
        assertNull(AnotherClassWithStaticInitializer.CONSTANT);
        AnotherClassWithStaticInitializer.doSomething();
        assertEquals(0, mockAnother.getValue());
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
    void mockUninitializedClass(@Mocked ClassWhichCallsStaticMethodFromInitializer unused) {
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
    @BeforeEach
    void loadNestedImplementationClass() {
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
    void mockUninitializedImplementationClass(@Capturing BaseType mockBase) {
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
    void mockAnotherDependentClass(@Mocked AnotherDependent anotherDependent) {
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
    void verifyClassInitializerForMockedBaseInterface() {
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
    void mockClassWhichCallsMethodOnItselfFromInitializerWithoutStubbingOutTheInitializer(
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
    // failed on JDK 9+ only
    @Test
    void mockAbstractClassImplementingInterfaceWithStaticInitializer(@Mocked AbstractImpl mock2) {
        assertEquals("test", InterfaceWithStaticInitializer.CONSTANT);
    }
}
