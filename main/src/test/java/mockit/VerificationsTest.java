/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mockit.integration.junit5.JMockitExtension;
import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class VerificationsTest.
 */
@ExtendWith(JMockitExtension.class)
class VerificationsTest {

    /**
     * The Class Dependency.
     */
    private static class Dependency {

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
    void verifySimpleInvocations() {
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
    void verifyUnrecordedInvocationThatNeverHappens() {
        MissingInvocation e = assertThrows(MissingInvocation.class, () -> {
            mock.setSomething(123);
            mock.prepare();
            new Verifications() {
                {
                    mock.setSomething(45);
                }
            };
        });
        assertTrue(e.getMessage().contains("45"));
    }

    /**
     * Verify recorded invocation that never happens.
     */
    @Test
    void verifyRecordedInvocationThatNeverHappens() {
        new Expectations() {
            {
                mock.editABunchMoreStuff();
                // Prevent failure here, let the verification fail instead:
                minTimes = 0;
            }
        };

        mock.setSomething(123);
        mock.prepare();

        MissingInvocation e = assertThrows(MissingInvocation.class, () -> {
            new Verifications() {
                {
                    mock.editABunchMoreStuff();
                }
            };
        });
    }

    /**
     * Verify invocation that is allowed to happen once or more and happens once.
     */
    @Test
    void verifyInvocationThatIsAllowedToHappenOnceOrMoreAndHappensOnce() {
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
    void verifyUnrecordedInvocationThatShouldHappenButDoesNot() {
        MissingInvocation e = assertThrows(MissingInvocation.class, () -> {
            mock.setSomething(1);

            new Verifications() {
                {
                    mock.notifyBeforeSave();
                }
            };
        });
    }

    /**
     * Verify invocations with invocation count.
     */
    @Test
    void verifyInvocationsWithInvocationCount() {
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
    void verifyInvocationsWithInvocationCountLargerThanOccurred() {
        MissingInvocation e = assertThrows(MissingInvocation.class, () -> {
            mock.setSomethingElse("test");
            mock.setSomething(3);
            mock.save();

            new Verifications() {
                {
                    mock.setSomething(anyInt);
                    times = 3;
                }
            };
        });
        assertTrue(e.getMessage().contains("Missing 2 invocations"));
        assertTrue(e.getMessage().contains("any int"));
    }

    /**
     * Verify invocations with invocation count smaller than occurred.
     */
    @Test
    void verifyInvocationsWithInvocationCountSmallerThanOccurred() {
        UnexpectedInvocation e = assertThrows(UnexpectedInvocation.class, () -> {
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
        });
        assertTrue(e.getMessage().contains("1 unexpected invocation"));
        assertTrue(e.getMessage().contains("5"));
    }

    /**
     * Verify invocation that should not occur but did.
     */
    @Test
    void verifyInvocationThatShouldNotOccurButDid() {
        UnexpectedInvocation e = assertThrows(UnexpectedInvocation.class, () -> {
            mock.setSomething(5);
            mock.setSomething(123);

            new Verifications() {
                {
                    mock.setSomething(anyInt);
                    maxTimes = 0;
                }
            };
        });
        assertTrue(e.getMessage().contains("2 unexpected invocations"));
        assertTrue(e.getMessage().contains("123"));
    }

    /**
     * Verify with argument matcher.
     */
    @Test
    void verifyWithArgumentMatcher() {
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
    void verifyWithArgumentMatcherAndIndividualInvocationCounts() {
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
    void verifyWithCustomArgumentMatcherWithoutArgumentValue() {
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
    void verifyThroughCapturedArguments() {
        AssertionError e = assertThrows(AssertionError.class, () -> {
            mock.setSomethingElse("test");

            new Verifications() {
                {
                    String value;
                    mock.setSomethingElse(value = withCapture());
                    // noinspection ConstantConditions
                    assertEquals(0, value.length(), "not empty");
                }
            };
        });
        assertTrue(e.getMessage().contains("not empty"));
    }

    /**
     * Verify with custom argument matcher.
     */
    @Test
    void verifyWithCustomArgumentMatcher() {
        MissingInvocation e = assertThrows(MissingInvocation.class, () -> {
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
        });
        assertTrue(e.getMessage().contains("isEmpty(\"test\")"));
    }

    /**
     * Verify invocation that matches expectation recorded with any matcher but with argument value which did not occur.
     */
    @Test
    void verifyInvocationThatMatchesExpectationRecordedWithAnyMatcherButWithArgumentValueWhichDidNotOccur() {
        MissingInvocation e = assertThrows(MissingInvocation.class, () -> {
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
        });
        assertTrue(e.getMessage().contains("45"));
    }

    /**
     * Verity two invocations to method matched on specific instance with no argument matchers.
     *
     * @param dep
     *            the dep
     */
    @Test
    void verityTwoInvocationsToMethodMatchedOnSpecificInstanceWithNoArgumentMatchers(@Injectable final Dependency dep) {
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
