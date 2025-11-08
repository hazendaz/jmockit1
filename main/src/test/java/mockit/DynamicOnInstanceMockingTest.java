package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mockit.integration.junit5.JMockitExtension;
import mockit.internal.expectations.invocation.MissingInvocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class DynamicOnInstanceMockingTest.
 */
@ExtendWith(JMockitExtension.class)
class DynamicOnInstanceMockingTest {

    /**
     * The Class Collaborator.
     */
    static class Collaborator {

        /** The value. */
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
        Collaborator(int value) {
            this.value = value;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        int getValue() {
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
    }

    /**
     * The Class AnotherDependency.
     */
    static class AnotherDependency {

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return "";
        }
    }

    /**
     * Mocking one instance and matching invocations only on that instance.
     */
    @Test
    void mockingOneInstanceAndMatchingInvocationsOnlyOnThatInstance() {
        Collaborator collaborator1 = new Collaborator();
        Collaborator collaborator2 = new Collaborator();
        final Collaborator collaborator3 = new Collaborator();

        new Expectations(collaborator3) {
            {
                collaborator3.getValue();
                result = 3;
            }
        };

        assertEquals(-1, collaborator1.getValue());
        assertEquals(-1, collaborator2.getValue());
        assertEquals(3, collaborator3.getValue());
        assertEquals(2, new Collaborator(2).getValue());
    }

    /**
     * Mocking two instances and matching invocations on each one.
     */
    @Test
    void mockingTwoInstancesAndMatchingInvocationsOnEachOne() {
        final Collaborator collaborator1 = new Collaborator();
        Collaborator collaborator2 = new Collaborator();

        new Expectations(collaborator1, collaborator2) {
            {
                collaborator1.getValue();
                result = 1;
            }
        };

        collaborator2.setValue(2);
        assertEquals(2, collaborator2.getValue());
        assertEquals(1, collaborator1.getValue());
        assertEquals(3, new Collaborator(3).getValue());
    }

    /**
     * Mocking one instance but recording on another.
     */
    @Test
    void mockingOneInstanceButRecordingOnAnother() {
        Collaborator collaborator1 = new Collaborator();
        final Collaborator collaborator2 = new Collaborator();
        Collaborator collaborator3 = new Collaborator();

        new Expectations(collaborator1) {
            {
                // A misuse of the API:
                collaborator2.getValue();
                result = -2;
            }
        };

        collaborator1.setValue(1);
        collaborator2.setValue(2);
        collaborator3.setValue(3);
        assertEquals(1, collaborator1.getValue());
        assertEquals(-2, collaborator2.getValue());
        assertEquals(3, collaborator3.getValue());
    }

    /**
     * The Class Foo.
     */
    public static class Foo {

        /**
         * Builds the value.
         *
         * @param s
         *            the s
         *
         * @return the foo
         */
        Foo buildValue(@SuppressWarnings("unused") String s) {
            return this;
        }

        /**
         * Do it.
         *
         * @return true, if successful
         */
        boolean doIt() {
            return false;
        }

        /**
         * Do it again.
         *
         * @return true, if successful
         */
        boolean doItAgain() {
            return false;
        }

        /**
         * Gets the bar.
         *
         * @return the bar
         */
        AnotherDependency getBar() {
            return null;
        }
    }

    /**
     * The Class SubFoo.
     */
    public static class SubFoo extends Foo {
    }

    /**
     * Record duplicate invocation on two dynamic mocks of different types but shared base class.
     */
    @Test
    void recordDuplicateInvocationOnTwoDynamicMocksOfDifferentTypesButSharedBaseClass() {
        final Foo f1 = new Foo();
        final SubFoo f2 = new SubFoo();

        new Expectations(f1, f2) {
            {
                f1.doIt();
                result = true;
                f2.doIt();
                result = false;
            }
        };

        assertTrue(f1.doIt());
        assertFalse(f2.doIt());
        assertFalse(new Foo().doIt());
        assertFalse(new SubFoo().doIt());
    }

    /**
     * Verify method invocation count on mocked and non mocked instances.
     */
    @Test
    void verifyMethodInvocationCountOnMockedAndNonMockedInstances() {
        final Foo foo1 = new Foo();
        final Foo foo2 = new Foo();

        new Expectations(foo1, foo2) {
            {
                foo1.doIt();
                result = true;
            }
        };

        assertTrue(foo1.doIt());
        assertFalse(foo2.doItAgain());
        assertFalse(foo2.doIt());
        final Foo foo3 = new Foo();
        assertFalse(foo1.doItAgain());
        assertFalse(foo3.doItAgain());
        assertFalse(foo3.doIt());
        assertFalse(foo3.doItAgain());

        new Verifications() {
            {
                assertFalse(foo2.doIt());
                times = 1;
                assertFalse(foo1.doItAgain());
                times = 1;
                assertFalse(foo3.doItAgain());
                times = 2;
            }
        };
    }

    /**
     * Creates the cascaded mock from partially mocked instance.
     */
    @Test
    void createCascadedMockFromPartiallyMockedInstance() {
        final Foo foo = new Foo();

        new Expectations(foo) {
            {
                foo.getBar().getName();
                result = "cascade";
            }
        };

        assertEquals("cascade", foo.getBar().getName());
    }

    /**
     * Use available mocked instance as cascade from partially mocked instance.
     *
     * @param bar
     *            the bar
     */
    @Test
    void useAvailableMockedInstanceAsCascadeFromPartiallyMockedInstance(@Mocked AnotherDependency bar) {
        final Foo foo = new Foo();

        new Expectations(foo) {
            {
                foo.getBar().getName();
                result = "cascade";
            }
        };

        AnotherDependency cascadedBar = foo.getBar();
        assertSame(bar, cascadedBar);
        assertEquals("cascade", cascadedBar.getName());
    }

    /**
     * The Class Bar.
     */
    static final class Bar extends AnotherDependency {
    }

    /**
     * Use available mocked subclass instance as cascade from partially mocked instance.
     *
     * @param bar
     *            the bar
     */
    @Test
    void useAvailableMockedSubclassInstanceAsCascadeFromPartiallyMockedInstance(@Mocked Bar bar) {
        final Foo foo = new Foo();

        new Expectations(foo) {
            {
                foo.getBar().getName();
                result = "cascade";
            }
        };

        AnotherDependency cascadedBar = foo.getBar();
        assertSame(bar, cascadedBar);
        assertEquals("cascade", cascadedBar.getName());
    }

    /**
     * Use itself as cascade from partially mocked instance.
     */
    @Test
    void useItselfAsCascadeFromPartiallyMockedInstance() {
        final Foo foo = new Foo();

        new Expectations(foo) {
            {
                foo.buildValue(anyString).doIt();
                result = true;
            }
        };

        Foo cascadedFoo = foo.buildValue("test");
        assertSame(foo, cascadedFoo);
        assertTrue(cascadedFoo.doIt());
    }

    /**
     * Verify single invocation to mocked instance with additional invocation to same method on another instance.
     */
    @Test
    void verifySingleInvocationToMockedInstanceWithAdditionalInvocationToSameMethodOnAnotherInstance() {
        final Collaborator mocked = new Collaborator();

        new Expectations(mocked) {
        };

        Collaborator notMocked = new Collaborator();
        assertEquals(-1, notMocked.getValue());
        assertEquals(-1, mocked.getValue());

        new Verifications() {
            {
                mocked.getValue();
                times = 1;
            }
        };
    }

    /**
     * Verify ordered invocations to dynamically mocked instance with another instance involved but missing an
     * invocation.
     */
    @Test
    void verifyOrderedInvocationsToDynamicallyMockedInstanceWithAnotherInstanceInvolvedButMissingAnInvocation() {
        assertThrows(MissingInvocation.class, () -> {
            final Collaborator mock = new Collaborator();

            new Expectations(mock) {
            };

            mock.setValue(1);
            new Collaborator().setValue(2);

            new VerificationsInOrder() {
                {
                    mock.setValue(1);
                    times = 1;
                    mock.setValue(2);
                    times = 1; // must be missing
                }
            };
        });
    }

    /**
     * Verify ordered invocations to dynamically mocked instance with another instance involved.
     */
    @Test
    void verifyOrderedInvocationsToDynamicallyMockedInstanceWithAnotherInstanceInvolved() {
        final Collaborator mock = new Collaborator();

        new Expectations(mock) {
            {
                mock.setValue(anyInt);
            }
        };

        mock.setValue(1);
        new Collaborator().setValue(2);

        new VerificationsInOrder() {
            {
                mock.setValue(1);
                times = 1;
                mock.setValue(2);
                times = 0;
            }
        };
    }
}
