package mockit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.Callable;

import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.jupiter.api.Test;

/**
 * The Class RestrictedFullVerificationsTest.
 */
final class RestrictedFullVerificationsTest {

    /**
     * The Class Dependency.
     */
    @SuppressWarnings("UnusedReturnValue")
    static class Dependency {

        /**
         * Sets the something.
         *
         * @param value
         *            the new something
         */
        public void setSomething(@SuppressWarnings("unused") int value) {
        }

        /**
         * Edits the A bunch more stuff.
         *
         * @return the int
         */
        public int editABunchMoreStuff() {
            return 0;
        }

        /**
         * Prepare.
         *
         * @return true, if successful
         */
        public boolean prepare() {
            return false;
        }

        /**
         * Save.
         */
        public void save() {
        }

        /**
         * Static method.
         *
         * @param s
         *            the s
         */
        static void staticMethod(@SuppressWarnings("unused") String s) {
        }
    }

    /**
     * The Class SubDependency.
     */
    static final class SubDependency extends Dependency {
        /**
         * Gets the value.
         *
         * @return the value
         */
        int getValue() {
            return 5;
        }
    }

    /**
     * The Class AnotherDependency.
     */
    static final class AnotherDependency {

        /**
         * Do something.
         */
        void doSomething() {
        }

        /**
         * Do something else.
         *
         * @param i
         *            the i
         *
         * @return the string
         */
        String doSomethingElse(int i) {
            return String.valueOf(i);
        }

        /**
         * Static method.
         *
         * @return true, if successful
         */
        static boolean staticMethod() {
            return true;
        }
    }

    /** The mock. */
    @Mocked
    Dependency mock;

    /**
     * Exercise code under test.
     */
    void exerciseCodeUnderTest() {
        mock.prepare();
        mock.setSomething(123);
        mock.editABunchMoreStuff();
        mock.save();
    }

    /**
     * Verify all invocations to only one of two mocked types.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyAllInvocationsToOnlyOneOfTwoMockedTypes(@Mocked AnotherDependency mock2) {
        exerciseCodeUnderTest();
        mock2.doSomething();

        new FullVerifications(mock) {
            {
                mock.prepare();
                mock.setSomething(anyInt);
                minTimes = 1;
                maxTimes = 2;
                mock.editABunchMoreStuff();
                mock.save();
                times = 1;
            }
        };

        new FullVerifications(mock.getClass()) {
            {
                mock.prepare();
                mock.setSomething(anyInt);
                minTimes = 1;
                maxTimes = 2;
                mock.editABunchMoreStuff();
                mock.save();
                times = 1;
            }
        };
    }

    /**
     * Verify all invocations with some missing.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyAllInvocationsWithSomeMissing(@Mocked final AnotherDependency mock2) {
        assertThrows(UnexpectedInvocation.class, () -> {
            exerciseCodeUnderTest();
            mock2.doSomething();

            new FullVerifications(mock, mock2) {
                {
                    mock.prepare();
                    mock.setSomething(anyInt);
                    mock.save();
                    mock2.doSomething();
                }
            };
        });
    }

    /**
     * Verify only invocations to generic type.
     *
     * @param mock2
     *            the mock 2
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void verifyOnlyInvocationsToGenericType(@Mocked final Callable<Dependency> mock2) throws Exception {
        exerciseCodeUnderTest();
        mock2.call();

        new FullVerifications(mock2) {
            {
                mock2.call();
            }
        };
    }

    /**
     * Verify all invocations to inherited methods.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyAllInvocationsToInheritedMethods(@Mocked SubDependency mock2) {
        mock.prepare();
        mock2.getValue();

        new FullVerifications(mock) {
            {
                mock.prepare();
            }
        };
        new FullVerifications(Dependency.class) {
            {
                mock.prepare();
            }
        };
    }

    /**
     * Verify all invocations to inherited methods when not verified.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyAllInvocationsToInheritedMethods_whenNotVerified(@Mocked final SubDependency mock2) {
        assertThrows(UnexpectedInvocation.class, () -> {
            mock2.prepare();
            mock2.getValue();

            new FullVerifications(mock2) {
                {
                    mock2.getValue();
                }
            };
        });
    }

    /**
     * Verify all invocations to subclass methods.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyAllInvocationsToSubclassMethods(@Mocked final SubDependency mock2) {
        mock.prepare();
        mock2.getValue();

        new FullVerifications(mock2.getClass()) {
            {
                mock2.getValue();
            }
        };
    }

    /**
     * Verify all invocations to subclass methods when not verified.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyAllInvocationsToSubclassMethods_whenNotVerified(@Mocked SubDependency mock2) {
        assertThrows(UnexpectedInvocation.class, () -> {
            mock.prepare();
            mock2.getValue();

            new FullVerifications(mock2.getClass()) {
                {
                    mock.prepare();
                }
            };
        });
    }

    /**
     * Verify all invocations to methods of base class and of subclass.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyAllInvocationsToMethodsOfBaseClassAndOfSubclass(@Mocked final SubDependency mock2) {
        mock2.prepare();
        mock2.getValue();

        new FullVerifications(mock2) {
            {
                mock2.prepare();
                mock2.getValue();
            }
        };
    }

    /**
     * Verify all invocations to methods of base class and of subclass when inherited method not verified.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyAllInvocationsToMethodsOfBaseClassAndOfSubclass_whenInheritedMethodNotVerified(
            @Mocked final SubDependency mock2) {
        assertThrows(UnexpectedInvocation.class, () -> {
            mock2.prepare();
            mock2.getValue();

            new FullVerifications(mock2) {
                {
                    mock2.getValue();
                }
            };
        });
    }

    /**
     * Verify all invocations to methods of base class and of subclass when subclass method not verified.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyAllInvocationsToMethodsOfBaseClassAndOfSubclass_whenSubclassMethodNotVerified(
            @Mocked SubDependency mock2) {
        assertThrows(UnexpectedInvocation.class, () -> {
            mock.prepare();
            mock2.getValue();

            new FullVerifications(mock2) {
                {
                    mock.prepare();
                }
            };
        });
    }

    /**
     * Verify all invocations with replay on different instance.
     */
    @Test
    void verifyAllInvocationsWithReplayOnDifferentInstance() {
        new Dependency().save();

        new FullVerifications(mock) {
            {
                new Dependency();
                mock.save();
            }
        };
    }

    /**
     * Verify all invocations with replay on same instance.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyAllInvocationsWithReplayOnSameInstance(@Mocked final Dependency mock2) {
        mock2.editABunchMoreStuff();

        new FullVerifications(mock2) {
            {
                mock2.editABunchMoreStuff();
            }
        };
    }

    /**
     * Verify all with replay on different instance when should be same.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyAllWithReplayOnDifferentInstanceWhenShouldBeSame(@Mocked Dependency mock2) {
        assertThrows(MissingInvocation.class, () -> {
            mock2.editABunchMoreStuff();

            new FullVerifications(mock2) {
                {
                    mock.editABunchMoreStuff();
                }
            };
        });
    }

    /**
     * Verify all with unverified replay on same instance.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyAllWithUnverifiedReplayOnSameInstance(@Mocked Dependency mock2) {
        assertThrows(UnexpectedInvocation.class, () -> {
            mock.editABunchMoreStuff();
            mock2.editABunchMoreStuff();

            new FullVerifications(mock2) {
                {
                    mock.editABunchMoreStuff();
                }
            };
        });
    }

    /**
     * Verify static invocation for specified mock instance.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyStaticInvocationForSpecifiedMockInstance(@Mocked final AnotherDependency mock2) {
        mock2.doSomething();
        AnotherDependency.staticMethod();
        mock2.doSomethingElse(1);
        mock.editABunchMoreStuff();
        mock2.doSomethingElse(2);

        new FullVerifications(mock2) {
            {
                mock2.doSomething();
                AnotherDependency.staticMethod();
                mock2.doSomethingElse(anyInt);
                mock2.doSomethingElse(anyInt);
            }
        };
    }

    /**
     * Unverified static invocation for specified mock instance.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void unverifiedStaticInvocationForSpecifiedMockInstance(@Mocked final AnotherDependency mock2) {
        assertThrows(UnexpectedInvocation.class, () -> {
            mock2.doSomething();
            AnotherDependency.staticMethod();

            new FullVerifications(mock2) {
                {
                    mock2.doSomething();
                }
            };
        });
    }

    /**
     * Unverified static invocation for specified subclass instance.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void unverifiedStaticInvocationForSpecifiedSubclassInstance(@Mocked final SubDependency mock2) {
        assertThrows(UnexpectedInvocation.class, () -> {
            mock2.getValue();
            Dependency.staticMethod("test");

            new FullVerifications(mock2) {
                {
                    mock2.getValue();
                }
            };
        });
    }

    /**
     * Verify no invocations occurred on one of two mocked dependencies.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyNoInvocationsOccurredOnOneOfTwoMockedDependencies(@Mocked AnotherDependency mock2) {
        mock2.doSomething();

        new FullVerifications(mock) {
        };
    }

    /**
     * Verify no invocations occurred on mocked dependency with one having occurred.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyNoInvocationsOccurredOnMockedDependencyWithOneHavingOccurred(@Mocked AnotherDependency mock2) {
        mock2.doSomething();
        mock.editABunchMoreStuff();

        try {
            new FullVerifications(mock) {
            };
            fail();
        } catch (UnexpectedInvocation e) {
            assertTrue(e.getMessage().contains("editABunchMoreStuff()"));
        }
    }

    /**
     * Verify no invocations on one of two mocked dependencies beyond those recorded as expected.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyNoInvocationsOnOneOfTwoMockedDependenciesBeyondThoseRecordedAsExpected(
            @Mocked final AnotherDependency mock2) {
        new Expectations() {
            {
                mock.setSomething(anyInt);
                mock2.doSomething();
                times = 1;
            }
        };

        mock.prepare();
        mock.setSomething(1);
        mock.setSomething(2);
        mock.save();
        mock2.doSomething();

        new FullVerifications(mock2) {
        };
    }
}
