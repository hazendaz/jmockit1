package mockit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.jupiter.api.Test;

/**
 * The Class VerificationsInOrderTest.
 */
public final class VerificationsInOrderTest {

    /**
     * The Class Dependency.
     */
    @SuppressWarnings("unused")
    private static class Dependency {

        /**
         * Sets the something.
         *
         * @param value
         *            the new something
         */
        public void setSomething(int value) {
        }

        /**
         * Sets the something else.
         *
         * @param value
         *            the new something else
         */
        public void setSomethingElse(String value) {
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

        /**
         * Do something.
         *
         * @param h
         *            the h
         */
        void doSomething(ClassWithHashCode h) {
        }
    }

    /**
     * The Class ClassWithHashCode.
     */
    static final class ClassWithHashCode {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof ClassWithHashCode && this == obj;
        }

        @Override
        public int hashCode() {
            return 123;
        }
    }

    /** The mock. */
    @Mocked
    Dependency mock;

    /**
     * Exercise code under test.
     */
    private void exerciseCodeUnderTest() {
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

        new VerificationsInOrder() {
            {
                mock.prepare();
                mock.setSomething(45);
                mock.editABunchMoreStuff();
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

            new VerificationsInOrder() {
                {
                    mock.notifyBeforeSave();
                }
            };
        });
    }

    /**
     * Verify unrecorded invocation that should happen exactly once but does not.
     */
    @Test
    void verifyUnrecordedInvocationThatShouldHappenExactlyOnceButDoesNot() {
        MissingInvocation e = assertThrows(MissingInvocation.class, () -> {
            mock.setSomething(1);

            new VerificationsInOrder() {
                {
                    mock.setSomething(2);
                    times = 1;
                }
            };
        });
        assertTrue(e.getMessage().contains("2"));
    }

    /**
     * Verify recorded invocation that should happen but does not.
     */
    @Test
    void verifyRecordedInvocationThatShouldHappenButDoesNot() {
        new Expectations() {
            {
                mock.setSomething(1);
                mock.notifyBeforeSave();
                // Prevent failure here, let the verification fail instead:
                minTimes = 0;
            }
        };

        mock.setSomething(1);

        MissingInvocation e = assertThrows(MissingInvocation.class, () -> {
            new VerificationsInOrder() {
                {
                    mock.setSomething(1);
                    mock.notifyBeforeSave();
                }
            };
        });
    }

    /**
     * Verify all invocations with some of them recorded.
     */
    @Test
    void verifyAllInvocationsWithSomeOfThemRecorded() {
        new Expectations() {
            {
                mock.prepare();
                mock.editABunchMoreStuff();
            }
        };

        exerciseCodeUnderTest();

        new VerificationsInOrder() {
            {
                mock.prepare();
                minTimes = 1;
                mock.setSomethingElse(anyString);
                mock.setSomething(anyInt);
                minTimes = 1;
                maxTimes = 2;
                mock.editABunchMoreStuff();
                mock.notifyBeforeSave();
                maxTimes = 1;
                mock.save();
                times = 1;
            }
        };
    }

    /**
     * Verify invocations with exact invocation counts having recorded matching expectation with argument matcher.
     */
    @Test
    void verifyInvocationsWithExactInvocationCountsHavingRecordedMatchingExpectationWithArgumentMatcher() {
        new Expectations() {
            {
                mock.setSomething(anyInt);
            }
        };

        mock.setSomething(1);
        mock.setSomething(2);

        new VerificationsInOrder() {
            {
                mock.setSomething(1);
                times = 1;
                mock.setSomething(2);
                times = 1;
            }
        };
    }

    /**
     * Verify invocation that is allowed to happen any number of times and happens once.
     */
    @Test
    void verifyInvocationThatIsAllowedToHappenAnyNumberOfTimesAndHappensOnce() {
        mock.prepare();
        mock.setSomething(123);
        mock.save();

        new VerificationsInOrder() {
            {
                mock.prepare();
                mock.setSomething(anyInt);
                mock.save();
            }
        };
    }

    /**
     * Verify simple invocations when out of order.
     */
    @Test
    void verifySimpleInvocationsWhenOutOfOrder() {
        MissingInvocation e = assertThrows(MissingInvocation.class, () -> {
            mock.setSomething(123);
            mock.prepare();

            new VerificationsInOrder() {
                {
                    mock.prepare();
                    mock.setSomething(123);
                }
            };
        });
        assertTrue(e.getMessage().contains("123"));
    }

    /**
     * Verify repeating invocation.
     */
    @Test
    void verifyRepeatingInvocation() {
        mock.setSomething(123);
        mock.setSomething(123);

        new VerificationsInOrder() {
            {
                mock.setSomething(123);
                times = 2;
            }
        };
    }

    /**
     * Verify repeating invocation that occurs one time more than expected.
     */
    @Test
    void verifyRepeatingInvocationThatOccursOneTimeMoreThanExpected() {
        UnexpectedInvocation e = assertThrows(UnexpectedInvocation.class, () -> {
            mock.setSomething(123);
            mock.setSomething(123);

            new VerificationsInOrder() {
                {
                    mock.setSomething(123);
                    maxTimes = 1;
                }
            };
        });
    }

    /**
     * Verify repeating invocation using matcher.
     */
    @Test
    void verifyRepeatingInvocationUsingMatcher() {
        mock.setSomething(123);
        mock.setSomething(45);

        new VerificationsInOrder() {
            {
                mock.setSomething(anyInt);
                times = 2;
            }
        };
    }

    /**
     * Verify invocation not expected to occur but which does.
     */
    @Test
    void verifyInvocationNotExpectedToOccurButWhichDoes() {
        UnexpectedInvocation e = assertThrows(UnexpectedInvocation.class, () -> {
            mock.prepare();
            mock.setSomething(123);

            new VerificationsInOrder() {
                {
                    mock.prepare();
                    mock.setSomething(anyInt);
                    maxTimes = 0;
                }
            };
        });
        assertTrue(e.getMessage().contains("123"));
    }

    /**
     * Verify with argument matcher.
     */
    @Test
    void verifyWithArgumentMatcher() {
        exerciseCodeUnderTest();

        new VerificationsInOrder() {
            {
                mock.prepare();
                mock.setSomething(anyInt);
            }
        };
    }

    /**
     * Verify with individual invocation counts for non consecutive invocations.
     */
    @Test
    void verifyWithIndividualInvocationCountsForNonConsecutiveInvocations() {
        exerciseCodeUnderTest();

        new VerificationsInOrder() {
            {
                mock.prepare();
                times = 1;
                mock.setSomething(anyInt);
                times = 2;
            }
        };
    }

    /**
     * Verify using invocation count constraint and argument matcher on object with mocked hash code.
     *
     * @param wh
     *            the wh
     */
    @Test
    void verifyUsingInvocationCountConstraintAndArgumentMatcherOnObjectWithMockedHashCode(
            @Mocked ClassWithHashCode wh) {
        mock.doSomething(null);
        mock.doSomething(wh);

        new VerificationsInOrder() {
            {
                mock.doSomething((ClassWithHashCode) withNull());
                times = 1;
                mock.doSomething((ClassWithHashCode) withNotNull());
            }
        };
    }

    /**
     * Verify with argument matchers when out of order.
     */
    @Test
    void verifyWithArgumentMatchersWhenOutOfOrder() {
        MissingInvocation e = assertThrows(MissingInvocation.class, () -> {
            mock.setSomething(123);
            mock.setSomethingElse("anotherValue");
            mock.setSomething(45);

            new VerificationsInOrder() {
                {
                    mock.setSomething(anyInt);
                    mock.setSomething(anyInt);
                    mock.setSomethingElse(anyString);
                }
            };
        });
        assertTrue(e.getMessage().contains("any String"));
    }

    /**
     * Verify with argument matcher and individual invocation count when out of order.
     */
    @Test
    void verifyWithArgumentMatcherAndIndividualInvocationCountWhenOutOfOrder() {
        MissingInvocation e = assertThrows(MissingInvocation.class, () -> {
            mock.setSomething(123);
            mock.prepare();
            mock.setSomething(45);

            new VerificationsInOrder() {
                {
                    mock.prepare();
                    mock.setSomething(anyInt);
                    times = 2;
                }
            };
        });
        assertTrue(e.getMessage().contains("Missing 1 invocation") && e.getMessage().contains("any int"));
    }

    /**
     * Verify two independent sequences of invocations which occur separately.
     */
    @Test
    void verifyTwoIndependentSequencesOfInvocationsWhichOccurSeparately() {
        // First sequence:
        mock.setSomething(1);
        mock.setSomething(2);

        // Second sequence:
        mock.setSomething(10);
        mock.setSomething(20);

        // Verifies first sequence:
        new VerificationsInOrder() {
            {
                mock.setSomething(1);
                mock.setSomething(2);
            }
        };

        // Verifies second sequence:
        new VerificationsInOrder() {
            {
                mock.setSomething(10);
                mock.setSomething(20);
            }
        };
    }

    /**
     * Verify two independent sequences of invocations which are mixed together.
     */
    @Test
    void verifyTwoIndependentSequencesOfInvocationsWhichAreMixedTogether() {
        mock.setSomething(1); // first sequence
        mock.setSomething(10); // second sequence
        mock.setSomething(2); // first sequence
        mock.setSomething(20); // second sequence

        // Verifies second sequence:
        new VerificationsInOrder() {
            {
                mock.setSomething(10);
                mock.setSomething(20);
            }
        };

        // Verifies first sequence:
        new VerificationsInOrder() {
            {
                mock.setSomething(1);
                mock.setSomething(2);
            }
        };
    }

    /**
     * Verify second sequence of invocations with times constraint after verifying last invocation of first sequence.
     */
    @Test
    void verifySecondSequenceOfInvocationsWithTimesConstraintAfterVerifyingLastInvocationOfFirstSequence() {
        mock.setSomething(1); // first sequence
        mock.setSomething(3); // second sequence
        mock.setSomething(4); // second sequence
        mock.setSomething(2); // first sequence

        new VerificationsInOrder() {
            {
                mock.setSomething(1);
                mock.setSomething(2);
            }
        };

        new VerificationsInOrder() {
            {
                mock.setSomething(3);
                mock.setSomething(4);
                times = 1;
            }
        };
    }
}
