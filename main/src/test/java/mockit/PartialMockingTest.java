package mockit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.xml.bind.annotation.XmlElement;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;

import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * The Class PartialMockingTest.
 */
@SuppressWarnings("deprecation")
public final class PartialMockingTest {

    /** The thrown. */
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    /**
     * The Class Collaborator.
     */
    @Deprecated
    static class Collaborator {

        /** The value. */
        @Deprecated
        protected int value;

        /**
         * Instantiates a new collaborator.
         */
        Collaborator() {
            value = -1;
        }

        /**
         * Instantiates a new collaborator.
         *
         * @param value
         *            the value
         */
        @Deprecated
        Collaborator(@Deprecated int value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        final int getValue() {
            return value;
        }

        /**
         * Sets the value.
         *
         * @param value
         *            the new value
         */
        void setValue(int value) {
            this.value = value;
        }

        /**
         * Simple operation.
         *
         * @param a
         *            the a
         * @param b
         *            the b
         * @param c
         *            the c
         *
         * @return true, if successful
         */
        @SuppressWarnings("unused")
        final boolean simpleOperation(int a, @XmlElement(name = "test") String b, Date c) {
            return true;
        }

        /**
         * Do something.
         *
         * @param b
         *            the b
         * @param s
         *            the s
         */
        static void doSomething(@SuppressWarnings("unused") boolean b, @SuppressWarnings("unused") String s) {
            throw new IllegalStateException();
        }

        /**
         * Method which calls another in the same class.
         *
         * @return true, if successful
         */
        @Ignore("test")
        boolean methodWhichCallsAnotherInTheSameClass() {
            return simpleOperation(1, "internal", null);
        }

        /**
         * Overridable method.
         *
         * @return the string
         */
        String overridableMethod() {
            return "base";
        }
    }

    /**
     * Attempt to partially mock A class.
     */
    @Test
    public void attemptToPartiallyMockAClass() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid Class");
        thrown.expectMessage("partial mocking");
        thrown.expectMessage("Collaborator");

        new Expectations(Collaborator.class) {
        };
    }

    /**
     * Dynamic mock fully verified verify all recorded expectations but not all of the replayed ones.
     */
    @Test
    public void dynamicMockFullyVerified_verifyAllRecordedExpectationsButNotAllOfTheReplayedOnes() {
        final Collaborator collaborator = new Collaborator(0);

        new Expectations(collaborator) {
            {
                collaborator.setValue(1);
            }
        };

        collaborator.setValue(1);
        collaborator.setValue(2);

        // Verifies all the *mocked* invocations that would be left unverified; ignores those not mocked:
        new FullVerifications() {
            // No need to verify "setValue(1)" since it was recorded and implicitly verified.
            // No need to verify "setValue(2)" since it was not recorded.
        };
    }

    /**
     * Dynamic mock fully verified in order verify all recorded expectations but not all of the replayed ones.
     */
    @Test
    public void dynamicMockFullyVerifiedInOrder_verifyAllRecordedExpectationsButNotAllOfTheReplayedOnes() {
        final Collaborator collaborator = new Collaborator(0);

        new Expectations(collaborator) {
            {
                collaborator.setValue(2);
                collaborator.setValue(3);
            }
        };

        collaborator.setValue(1);
        collaborator.setValue(2);
        collaborator.setValue(3);

        // Verifies all the *mocked* (recorded) invocations, ignoring those not mocked:
        new VerificationsInOrder() {
            {
                // No need to verify "setValue(1)" since it was not recorded.
                collaborator.setValue(2);
                collaborator.setValue(3);
            }
        };
        new FullVerifications() {
        };
    }

    /**
     * Expect two invocations on dynamic mock but replay once.
     */
    @Test
    public void expectTwoInvocationsOnDynamicMockButReplayOnce() {
        final Collaborator collaborator = new Collaborator();

        new Expectations(collaborator) {
            {
                collaborator.getValue();
                times = 2;
            }
        };

        assertEquals(0, collaborator.getValue());
        thrown.expect(MissingInvocation.class);
    }

    /**
     * Expect one invocation on dynamic mock but replay twice.
     */
    @Test
    public void expectOneInvocationOnDynamicMockButReplayTwice() {
        final Collaborator collaborator = new Collaborator(1);

        new Expectations(collaborator) {
            {
                collaborator.getValue();
                times = 1;
            }
        };

        // Mocked:
        assertEquals(0, collaborator.getValue());

        // Still mocked because it's not strict:
        thrown.expect(UnexpectedInvocation.class);
        assertEquals(0, collaborator.getValue());
    }

    /**
     * Dynamically mock an instance.
     */
    @Test
    public void dynamicallyMockAnInstance() {
        final Collaborator collaborator = new Collaborator(2);

        new Expectations(collaborator) {
            {
                collaborator.simpleOperation(1, "", null);
                result = false;
                Collaborator.doSomething(anyBoolean, "test");
            }
        };

        // Mocked:
        assertFalse(collaborator.simpleOperation(1, "", null));
        Collaborator.doSomething(true, "test");

        // Not mocked:
        assertEquals(2, collaborator.getValue());
        assertEquals(45, new Collaborator(45).value);
        assertEquals(-1, new Collaborator().value);

        try {
            Collaborator.doSomething(false, null);
            fail();
        } catch (IllegalStateException ignore) {
        }

        new Verifications() {
            {
                collaborator.getValue();
                times = 1;
            }
        };
    }

    /**
     * Mock method in same class.
     */
    @Test
    public void mockMethodInSameClass() {
        final Collaborator collaborator = new Collaborator();

        new Expectations(collaborator) {
            {
                collaborator.simpleOperation(1, anyString, null);
                result = false;
            }
        };

        assertFalse(collaborator.methodWhichCallsAnotherInTheSameClass());
        assertTrue(collaborator.simpleOperation(2, "", null));
        assertFalse(collaborator.simpleOperation(1, "", null));
    }

    /**
     * The Class SubCollaborator.
     */
    static final class SubCollaborator extends Collaborator {

        /**
         * Instantiates a new sub collaborator.
         */
        @SuppressWarnings("unused")
        SubCollaborator() {
            this(1);
        }

        /**
         * Instantiates a new sub collaborator.
         *
         * @param value
         *            the value
         */
        SubCollaborator(int value) {
            super(value);
        }

        @Override
        String overridableMethod() {
            return super.overridableMethod() + " overridden";
        }

        /**
         * Format.
         *
         * @return the string
         */
        String format() {
            return String.valueOf(value);
        }

        /**
         * Cause failure.
         */
        static void causeFailure() {
            throw new RuntimeException();
        }
    }

    /**
     * Dynamically mock A sub collaborator instance.
     */
    @Test
    public void dynamicallyMockASubCollaboratorInstance() {
        final SubCollaborator collaborator = new SubCollaborator();

        new Expectations(collaborator) {
            {
                collaborator.getValue();
                result = 5;
                collaborator.format();
                result = "test";
                SubCollaborator.causeFailure();
            }
        };

        // Mocked:
        assertEquals(5, collaborator.getValue());
        SubCollaborator.causeFailure();
        assertEquals("test", collaborator.format());

        // Not mocked:
        assertTrue(collaborator.simpleOperation(0, null, null)); // not recorded
        assertEquals("1", new SubCollaborator().format()); // was recorded but on a different instance

        try {
            Collaborator.doSomething(true, null); // not recorded
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    /**
     * The Interface Dependency.
     */
    interface Dependency {

        /**
         * Do something.
         *
         * @return true, if successful
         */
        boolean doSomething();

        /**
         * Do something else.
         *
         * @param n
         *            the n
         *
         * @return the list
         */
        List<?> doSomethingElse(int n);
    }

    /**
     * Dynamically mock an anonymous class instance through the implemented interface.
     */
    @Test
    public void dynamicallyMockAnAnonymousClassInstanceThroughTheImplementedInterface() {
        final Collaborator collaborator = new Collaborator();

        final Dependency dependency = new Dependency() {
            @Override
            public boolean doSomething() {
                return false;
            }

            @Override
            public List<?> doSomethingElse(int n) {
                return null;
            }
        };

        new Expectations(collaborator, dependency) {
            {
                collaborator.getValue();
                result = 5;
                dependency.doSomething();
                result = true;
            }
        };

        // Mocked:
        assertEquals(5, collaborator.getValue());
        assertTrue(dependency.doSomething());

        // Not mocked:
        assertTrue(collaborator.simpleOperation(0, null, null));
        assertNull(dependency.doSomethingElse(3));

        new FullVerifications() {
            {
                dependency.doSomethingElse(anyInt);
                collaborator.simpleOperation(0, null, null);
            }
        };
    }

    /**
     * The Interface AnotherInterface.
     */
    public interface AnotherInterface {
    }

    /**
     * The Interface NonPublicInterface.
     */
    interface NonPublicInterface {
    }

    /**
     * Attempt to use dynamic mocking for invalid types.
     *
     * @param publicInterfaceMock
     *            the public interface mock
     * @param nonPublicInterfaceMock
     *            the non public interface mock
     */
    @Test
    public void attemptToUseDynamicMockingForInvalidTypes(@Mocked AnotherInterface publicInterfaceMock,
            @Injectable NonPublicInterface nonPublicInterfaceMock) {
        assertInvalidTypeForDynamicPartialMocking(new String[1]);
        assertInvalidTypeForDynamicPartialMocking(123);
        assertInvalidTypeForDynamicPartialMocking(true);
        assertInvalidTypeForDynamicPartialMocking(2.5);
        assertInvalidTypeForDynamicPartialMocking(publicInterfaceMock);
        assertInvalidTypeForDynamicPartialMocking(nonPublicInterfaceMock);
    }

    /**
     * Assert invalid type for dynamic partial mocking.
     *
     * @param object
     *            the object
     */
    void assertInvalidTypeForDynamicPartialMocking(Object object) {
        try {
            new Expectations(object) {
            };
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("partial mocking"));
        }
    }

    /**
     * Dynamic partial mocking with exact argument matching.
     */
    @Test
    public void dynamicPartialMockingWithExactArgumentMatching() {
        final Collaborator collaborator = new Collaborator();

        new Expectations(collaborator) {
            {
                collaborator.simpleOperation(1, "s", null);
                result = false;
            }
        };

        assertFalse(collaborator.simpleOperation(1, "s", null));
        assertTrue(collaborator.simpleOperation(2, "s", null));
        assertTrue(collaborator.simpleOperation(1, "S", null));
        assertTrue(collaborator.simpleOperation(1, "s", new Date()));
        assertTrue(collaborator.simpleOperation(1, null, new Date()));
        assertFalse(collaborator.simpleOperation(1, "s", null));

        new FullVerifications() {
            {
                collaborator.simpleOperation(anyInt, null, null);
            }
        };
    }

    /**
     * Dynamic partial mocking with flexible argument matching.
     */
    @Test
    public void dynamicPartialMockingWithFlexibleArgumentMatching() {
        final Collaborator mock = new Collaborator();

        new Expectations(mock) {
            {
                mock.simpleOperation(anyInt, withPrefix("s"), null);
                result = false;
            }
        };

        assertFalse(mock.simpleOperation(1, "sSs", null));
        assertTrue(mock.simpleOperation(2, " s", null));
        assertTrue(mock.simpleOperation(1, "S", null));
        assertFalse(mock.simpleOperation(-1, "s", new Date()));
        assertTrue(mock.simpleOperation(1, null, null));
        assertFalse(mock.simpleOperation(0, "string", null));

        Collaborator collaborator = new Collaborator();
        assertTrue(collaborator.simpleOperation(1, "sSs", null));
        assertTrue(collaborator.simpleOperation(-1, null, new Date()));
    }

    /**
     * Dynamic partial mocking with instance specific matching.
     */
    @Test
    public void dynamicPartialMockingWithInstanceSpecificMatching() {
        final Collaborator collaborator1 = new Collaborator();
        final Collaborator collaborator2 = new Collaborator(4);

        new Expectations(collaborator1, collaborator2) {
            {
                collaborator1.getValue();
                result = 3;
            }
        };

        assertEquals(3, collaborator1.getValue());
        assertEquals(4, collaborator2.getValue());

        new VerificationsInOrder() {
            {
                collaborator1.getValue();
                times = 1;
                collaborator2.getValue();
                times = 1;
            }
        };
    }

    /**
     * Dynamic partial mocking with instance specific matching on two instances of same class.
     */
    @Test
    public void dynamicPartialMockingWithInstanceSpecificMatchingOnTwoInstancesOfSameClass() {
        final Collaborator mock1 = new Collaborator();
        final Collaborator mock2 = new Collaborator();

        new Expectations(mock1, mock2) {
            {
                mock1.getValue();
                result = 1;
                mock2.getValue();
                result = 2;
            }
        };

        assertEquals(2, mock2.getValue());
        assertEquals(1, mock1.getValue());
    }

    /**
     * Method with no recorded expectation called twice during replay.
     */
    @Test
    public void methodWithNoRecordedExpectationCalledTwiceDuringReplay() {
        final Collaborator collaborator = new Collaborator(123);

        new Expectations(collaborator) {
        };

        assertEquals(123, collaborator.getValue());
        assertEquals(123, collaborator.getValue());

        new FullVerifications() {
            {
                collaborator.getValue();
                times = 2;
            }
        };
    }

    /**
     * The Class ClassWithNative.
     */
    static final class ClassWithNative {

        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething() {
            return nativeMethod();
        }

        /**
         * Native method.
         *
         * @return the int
         */
        private native int nativeMethod();
    }

    /**
     * Attempt to partially mock native method.
     */
    @Test
    public void attemptToPartiallyMockNativeMethod() {
        thrown.expect(UnsatisfiedLinkError.class);

        final ClassWithNative mock = new ClassWithNative();

        new Expectations(mock) {
            {
                // The native method is ignored when using dynamic mocking, so this actually tries to execute the real
                // method,
                // failing since there is no native implementation.
                mock.nativeMethod();
            }
        };
    }

    /**
     * Mock annotated constructor.
     *
     * @param mock
     *            the mock
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void mockAnnotatedConstructor(@Mocked Collaborator mock) throws Exception {
        Constructor<?> mockedConstructor = Collaborator.class.getDeclaredConstructor(int.class);

        assertTrue(mockedConstructor.isAnnotationPresent(Deprecated.class));
        assertTrue(mockedConstructor.getParameterAnnotations()[0][0] instanceof Deprecated);
    }

    /**
     * The Class TestedClass.
     */
    static final class TestedClass {

        /**
         * Instantiates a new tested class.
         */
        TestedClass() {
            this(true);
        }

        /**
         * Instantiates a new tested class.
         *
         * @param value
         *            the value
         */
        TestedClass(boolean value) {
            initialize(value);
        }

        /**
         * Initialize.
         *
         * @param flag
         *            the flag
         */
        private void initialize(@SuppressWarnings("unused") boolean flag) {
        }
    }

    /**
     * The Class BaseClass.
     */
    static class BaseClass {
        /**
         * Instantiates a new base class.
         *
         * @param o
         *            the o
         */
        @SuppressWarnings("unused")
        BaseClass(Object o) {
            assert o != null;
        }

        /**
         * Instantiates a new base class.
         */
        BaseClass() {
        }
    }

    /**
     * The Class SubClass.
     */
    static class SubClass extends BaseClass {
    }

    /**
     * The Class SubSubClass.
     */
    static class SubSubClass extends SubClass {
    }

    /**
     * Mock class indirectly extending base whose first constructor has more parameters than the second one.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void mockClassIndirectlyExtendingBaseWhoseFirstConstructorHasMoreParametersThanTheSecondOne(
            @Mocked SubSubClass mock) {
        new SubClass();
    }
}
