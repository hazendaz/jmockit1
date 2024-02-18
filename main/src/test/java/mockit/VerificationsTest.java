package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * The Class VerificationsTest.
 */
public final class VerificationsTest {

    /** The thrown. */
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    /**
     * The Class Dependency.
     */
    public static class Dependency {

        /**
         * Sets the something.
         *
         * @param value
         *            the new something
         */
        public void setSomething(@SuppressWarnings("unused") int value) {
        }

        /**
         * Sets the something else.
         *
         * @param value
         *            the new something else
         */
        public void setSomethingElse(@SuppressWarnings("unused") String value) {
        }

        /**
         * Edits the A bunch more stuff.
         */
        public void editABunchMoreStuff() {
        }

        /**
         * Notify before save.
         */
        public void notifyBeforeSave() {
        }

        /**
         * Prepare.
         */
        public void prepare() {
        }

        /**
         * Save.
         */
        public void save() {
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
        mock.setSomethingElse("anotherValue");
        mock.setSomething(45);
        mock.editABunchMoreStuff();
        mock.notifyBeforeSave();
        mock.save();
    }

    /**
     * Verify simple invocations.
     */
    @Test
    public void verifySimpleInvocations() {
        exerciseCodeUnderTest();

        new Verifications() {
            {
                mock.prepare();
                times = 1;
                mock.editABunchMoreStuff();
                mock.setSomething(45);
            }
        };
    }

    /**
     * Verify unrecorded invocation that never happens.
     */
    @Test
    public void verifyUnrecordedInvocationThatNeverHappens() {
        thrown.expect(MissingInvocation.class);
        thrown.expectMessage("45");

        mock.setSomething(123);
        mock.prepare();

        new Verifications() {
            {
                mock.setSomething(45);
            }
        };
    }

    /**
     * Verify recorded invocation that never happens.
     */
    @Test
    public void verifyRecordedInvocationThatNeverHappens() {
        thrown.expect(MissingInvocation.class);

        new Expectations() {
            {
                mock.editABunchMoreStuff();
            }
        };

        mock.setSomething(123);
        mock.prepare();

        new Verifications() {
            {
                mock.editABunchMoreStuff();
            }
        };
    }

    /**
     * Verify invocation that is allowed to happen once or more and happens once.
     */
    @Test
    public void verifyInvocationThatIsAllowedToHappenOnceOrMoreAndHappensOnce() {
        mock.prepare();
        mock.setSomething(123);
        mock.save();

        new Verifications() {
            {
                mock.setSomething(anyInt);
                mock.save();
            }
        };
    }

    /**
     * Verify unrecorded invocation that should happen but does not.
     */
    @Test
    public void verifyUnrecordedInvocationThatShouldHappenButDoesNot() {
        thrown.expect(MissingInvocation.class);

        mock.setSomething(1);

        new Verifications() {
            {
                mock.notifyBeforeSave();
            }
        };
    }

    /**
     * Verify invocations with invocation count.
     */
    @Test
    public void verifyInvocationsWithInvocationCount() {
        mock.setSomething(3);
        mock.save();
        mock.setSomethingElse("test");
        mock.save();

        new Verifications() {
            {
                mock.save();
                times = 2;
            }
        };
    }

    /**
     * Verify invocations with invocation count larger than occurred.
     */
    @Test
    public void verifyInvocationsWithInvocationCountLargerThanOccurred() {
        thrown.expect(MissingInvocation.class);
        thrown.expectMessage("Missing 2 invocations");
        thrown.expectMessage("any int");

        mock.setSomethingElse("test");
        mock.setSomething(3);
        mock.save();

        new Verifications() {
            {
                mock.setSomething(anyInt);
                times = 3;
            }
        };
    }

    /**
     * Verify invocations with invocation count smaller than occurred.
     */
    @Test
    public void verifyInvocationsWithInvocationCountSmallerThanOccurred() {
        thrown.expect(UnexpectedInvocation.class);
        thrown.expectMessage("1 unexpected invocation");
        thrown.expectMessage("5");

        mock.setSomethingElse("test");
        mock.setSomething(3);
        mock.save();
        mock.setSomething(5);

        new Verifications() {
            {
                mock.setSomething(anyInt);
                times = 1;
            }
        };
    }

    /**
     * Verify invocation that should not occur but did.
     */
    @Test
    public void verifyInvocationThatShouldNotOccurButDid() {
        thrown.expect(UnexpectedInvocation.class);
        thrown.expectMessage("2 unexpected invocations");
        thrown.expectMessage("123");

        mock.setSomething(5);
        mock.setSomething(123);

        new Verifications() {
            {
                mock.setSomething(anyInt);
                maxTimes = 0;
            }
        };
    }

    /**
     * Verify with argument matcher.
     */
    @Test
    public void verifyWithArgumentMatcher() {
        exerciseCodeUnderTest();

        new Verifications() {
            {
                mock.setSomething(anyInt);
            }
        };
    }

    /**
     * Verify with argument matcher and individual invocation counts.
     */
    @Test
    public void verifyWithArgumentMatcherAndIndividualInvocationCounts() {
        exerciseCodeUnderTest();

        new Verifications() {
            {
                mock.prepare();
                maxTimes = 1;
                mock.setSomething(anyInt);
                minTimes = 2;
                mock.editABunchMoreStuff();
                maxTimes = 5;
                mock.save();
                times = 1;
            }
        };
    }

    /**
     * Verify with custom argument matcher without argument value.
     */
    @Test
    public void verifyWithCustomArgumentMatcherWithoutArgumentValue() {
        mock.setSomethingElse("not empty");

        new Verifications() {
            {
                mock.setSomethingElse(with(new Delegate<String>() {
                    @Mock
                    boolean isNotEmpty(String s) {
                        return !s.isEmpty();
                    }
                }));
            }
        };
    }

    /**
     * Verify through captured arguments.
     */
    @Test
    public void verifyThroughCapturedArguments() {
        thrown.expect(AssertionError.class);
        thrown.expectMessage("not empty");

        mock.setSomethingElse("test");

        new Verifications() {
            {
                String value;
                mock.setSomethingElse(value = withCapture());
                // noinspection ConstantConditions
                assertEquals(0, value.length(), "not empty");
            }
        };
    }

    /**
     * Verify with custom argument matcher.
     */
    @Test
    public void verifyWithCustomArgumentMatcher() {
        thrown.expect(MissingInvocation.class);
        thrown.expectMessage("isEmpty(\"test\")");

        mock.setSomethingElse("test");

        new Verifications() {
            {
                mock.setSomethingElse(with(new Delegate<String>() {
                    @Mock
                    boolean isEmpty(String s) {
                        return s.isEmpty();
                    }
                }));
            }
        };
    }

    /**
     * Verify invocation that matches expectation recorded with any matcher but with argument value which did not occur.
     */
    @Test
    public void verifyInvocationThatMatchesExpectationRecordedWithAnyMatcherButWithArgumentValueWhichDidNotOccur() {
        thrown.expect(MissingInvocation.class);
        thrown.expectMessage("45");

        new Expectations() {
            {
                mock.setSomething(anyInt);
            }
        };

        mock.setSomething(123);

        new Verifications() {
            {
                mock.setSomething(45);
            }
        };
    }

    /**
     * Verity two invocations to method matched on specific instance with no argument matchers.
     *
     * @param dep
     *            the dep
     */
    @Test
    public void verityTwoInvocationsToMethodMatchedOnSpecificInstanceWithNoArgumentMatchers(
            @Injectable final Dependency dep) {
        dep.editABunchMoreStuff();
        dep.editABunchMoreStuff();

        new Verifications() {
            {
                dep.editABunchMoreStuff();
                times = 2;
            }
        };
    }
}
