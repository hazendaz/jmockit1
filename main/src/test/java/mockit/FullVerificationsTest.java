package mockit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The Class FullVerificationsTest.
 */
final class FullVerificationsTest {

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
        public void setSomethingElse(@SuppressWarnings("unused") char value) {
        }

        /**
         * Edits the A bunch more stuff.
         *
         * @return true, if successful
         */
        public boolean editABunchMoreStuff() {
            return false;
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
        mock.setSomethingElse('a');
        mock.setSomething(45);
        mock.editABunchMoreStuff();
        mock.notifyBeforeSave();
        mock.save();
    }

    /**
     * Verify all invocations.
     */
    @Test
    void verifyAllInvocations() {
        exerciseCodeUnderTest();

        new FullVerifications() {
            {
                mock.prepare();
                minTimes = 1;
                mock.editABunchMoreStuff();
                mock.notifyBeforeSave();
                maxTimes = 1;
                mock.setSomething(anyInt);
                minTimes = 0;
                maxTimes = 2;
                mock.setSomethingElse(anyChar);
                mock.save();
                times = 1;
            }
        };
    }

    /**
     * Verify all invocations with some of them recorded.
     */
    @Test
    void verifyAllInvocationsWithSomeOfThemRecorded() {
        new Expectations() {
            {
                mock.editABunchMoreStuff();
                result = true;
                mock.setSomething(45);
            }
        };

        exerciseCodeUnderTest();

        new FullVerifications() {
            {
                mock.prepare();
                mock.setSomething(anyInt);
                mock.setSomethingElse(anyChar);
                mock.notifyBeforeSave();
                mock.save();
            }
        };
    }

    /**
     * Verify all invocations with those recorded as expected to occur verified implicitly.
     */
    @Test
    void verifyAllInvocationsWithThoseRecordedAsExpectedToOccurVerifiedImplicitly() {
        new Expectations() {
            {
                mock.setSomething(45);
                times = 1;
                mock.editABunchMoreStuff();
                result = true;
                minTimes = 1;
            }
        };

        exerciseCodeUnderTest();

        new FullVerifications() {
            {
                mock.prepare();
                mock.setSomething(123);
                mock.setSomethingElse(anyChar);
                mock.notifyBeforeSave();
                mock.save();
            }
        };
    }

    /**
     * Verify all invocations except those already verified in A previous verification block.
     */
    @Test
    void verifyAllInvocationsExceptThoseAlreadyVerifiedInAPreviousVerificationBlock() {
        exerciseCodeUnderTest();

        new Verifications() {
            {
                mock.setSomething(45);
                mock.editABunchMoreStuff();
            }
        };

        new FullVerifications() {
            {
                mock.prepare();
                mock.setSomething(123);
                mock.setSomethingElse(anyChar);
                mock.notifyBeforeSave();
                mock.save();
            }
        };
    }

    /**
     * Verify all invocations with one missing.
     */
    @Test
    void verifyAllInvocationsWithOneMissing() {
        Throwable exception = Assertions.assertThrows(UnexpectedInvocation.class, () -> {

            exerciseCodeUnderTest();

            new FullVerifications() {
                {
                    mock.prepare();
                    mock.notifyBeforeSave();
                    mock.setSomething(anyInt);
                    mock.setSomethingElse(anyChar);
                    mock.save();
                }
            };
        });
        assertTrue(exception.getMessage().contains("editABunchMoreStuff()"));
    }

    /**
     * Verify unrecorded invocation that was expected to not happen.
     */
    @Test
    void verifyUnrecordedInvocationThatWasExpectedToNotHappen() {
        mock.prepare();
        mock.setSomething(123);
        mock.setSomething(45);

        new FullVerifications() {
            {
                mock.prepare();
                mock.setSomething(anyInt);
                times = 2;
                mock.notifyBeforeSave();
                times = 0;
            }
        };
    }

    /**
     * Verify unrecorded invocation that should not happen but does.
     */
    @Test
    void verifyUnrecordedInvocationThatShouldNotHappenButDoes() {
        Throwable exception = Assertions.assertThrows(UnexpectedInvocation.class, () -> {

            mock.setSomething(1);
            mock.notifyBeforeSave();

            new FullVerifications() {
                {
                    mock.setSomething(1);
                    mock.notifyBeforeSave();
                    times = 0;
                }
            };
        });
        assertTrue(exception.getMessage().contains("1 unexpected invocation"));
    }

    /**
     * Verify invocation that is allowed to happen any number of times and happens once.
     */
    @Test
    void verifyInvocationThatIsAllowedToHappenAnyNumberOfTimesAndHappensOnce() {
        mock.prepare();
        mock.setSomething(123);
        mock.save();

        new FullVerifications() {
            {
                mock.prepare();
                mock.setSomething(anyInt);
                mock.save();
                minTimes = 0;
            }
        };
    }

    /**
     * Verify recorded invocation that is allowed to happen any no of times and does not happen.
     */
    @Test
    void verifyRecordedInvocationThatIsAllowedToHappenAnyNoOfTimesAndDoesNotHappen() {
        mock.prepare();
        mock.setSomething(123);

        new FullVerifications() {
            {
                mock.prepare();
                mock.setSomething(anyInt);
                mock.save();
                minTimes = 0;
            }
        };
    }

    /**
     * Verify unrecorded invocation that is allowed to happen any no of times and does not happen.
     */
    @Test
    void verifyUnrecordedInvocationThatIsAllowedToHappenAnyNoOfTimesAndDoesNotHappen() {
        mock.prepare();
        mock.setSomething(123);

        new FullVerifications() {
            {
                mock.prepare();
                mock.setSomething(anyInt);
                mock.save();
                minTimes = 0;
            }
        };
    }

    /**
     * Verify unrecorded invocation that should happen but does not.
     */
    @Test
    void verifyUnrecordedInvocationThatShouldHappenButDoesNot() {
        Assertions.assertThrows(MissingInvocation.class, () -> {
            mock.setSomething(1);

            new FullVerifications() {
                {
                    mock.notifyBeforeSave();
                }
            };
        });
    }

    /**
     * Verify recorded invocation that should happen but does not.
     */
    @Test
    void verifyRecordedInvocationThatShouldHappenButDoesNot() {
        Assertions.assertThrows(MissingInvocation.class, () -> {
            new Expectations() {
                {
                    mock.notifyBeforeSave();
                }
            };

            mock.setSomething(1);

            new FullVerifications() {
                {
                    mock.notifyBeforeSave();
                }
            };
        });
    }

    /**
     * Verify all invocations with extra verification.
     */
    @Test
    void verifyAllInvocationsWithExtraVerification() {
        Throwable exception = Assertions.assertThrows(MissingInvocation.class, () -> {
            mock.prepare();
            mock.setSomething(123);

            new FullVerifications() {
                {
                    mock.prepare();
                    mock.setSomething(123);
                    mock.notifyBeforeSave();
                }
            };
        });
        assertTrue(exception.getMessage().contains("notifyBeforeSave()"));
    }

    /**
     * Verify all invocations with invocation count one less than actual.
     */
    @Test
    void verifyAllInvocationsWithInvocationCountOneLessThanActual() {
        Throwable exception = Assertions.assertThrows(UnexpectedInvocation.class, () -> {
            mock.setSomething(123);
            mock.setSomething(45);

            new FullVerifications() {
                {
                    mock.setSomething(anyInt);
                    times = 1;
                }
            };
        });
        assertTrue(exception.getMessage().contains("45"));
    }

    /**
     * Verify all invocations with invocation count two less than actual.
     */
    @Test
    void verifyAllInvocationsWithInvocationCountTwoLessThanActual() {
        Throwable exception = Assertions.assertThrows(UnexpectedInvocation.class, () -> {
            mock.setSomething(123);
            mock.setSomething(45);
            mock.setSomething(1);

            new FullVerifications() {
                {
                    mock.setSomething(anyInt);
                    times = 1;
                }
            };
        });
        assertTrue(exception.getMessage().contains("1"));
    }

    /**
     * Verify all invocations with invocation count more than actual.
     */
    @Test
    void verifyAllInvocationsWithInvocationCountMoreThanActual() {
        Throwable exception = Assertions.assertThrows(MissingInvocation.class, () -> {
            mock.setSomethingElse('f');

            new FullVerifications() {
                {
                    mock.setSomethingElse(anyChar);
                    times = 3;
                }
            };
        });
        assertTrue(exception.getMessage().contains("any char"));
    }

    /**
     * Verify no invocations occurred on mocked dependency with one having occurred.
     */
    @Test
    void verifyNoInvocationsOccurredOnMockedDependencyWithOneHavingOccurred() {
        Assertions.assertThrows(UnexpectedInvocation.class, () -> {
            mock.editABunchMoreStuff();

            new FullVerifications() {
            };
        });
    }

    /**
     * Verify no invocations on mocked dependency beyond those recorded as expected.
     */
    @Test
    void verifyNoInvocationsOnMockedDependencyBeyondThoseRecordedAsExpected() {
        new Expectations() {
            {
                mock.prepare();
                times = 1;
            }
        };

        new Expectations() {
            {
                mock.setSomething(anyInt);
                minTimes = 1;
                mock.save();
                times = 1;
            }
        };

        mock.prepare();
        mock.setSomething(1);
        mock.setSomething(2);
        mock.save();

        new FullVerifications() {
        };
    }

    /**
     * Verify no invocations on mocked dependency beyond those recorded as expected with one having occurred.
     */
    @Test
    void verifyNoInvocationsOnMockedDependencyBeyondThoseRecordedAsExpectedWithOneHavingOccurred() {
        Throwable exception = Assertions.assertThrows(UnexpectedInvocation.class, () -> {

            new Expectations() {
                {
                    mock.prepare();
                    times = 1;
                    mock.save();
                    minTimes = 1;
                }
            };

            mock.prepare();
            mock.editABunchMoreStuff();
            mock.save();

            new FullVerifications() {
            };
        });
        assertTrue(exception.getMessage().contains("editABunchMoreStuff()"));
    }

    /**
     * Verify no unverified invocations when first invocation of method is but second one is not.
     */
    @Test
    void verifyNoUnverifiedInvocationsWhenFirstInvocationOfMethodIsButSecondOneIsNot() {
        Throwable exception = Assertions.assertThrows(UnexpectedInvocation.class, () -> {
            mock.prepare();
            mock.save();
            mock.prepare();
            mock.save(); // doesn't get verified

            new VerificationsInOrder() {
                {
                    mock.prepare();
                    times = 1;
                    mock.save();
                    times = 1;
                    mock.prepare();
                    times = 1;
                }
            };

            new FullVerifications() {
            };
        });
        assertTrue(exception.getMessage().contains("save()"));
    }

    /**
     * Verify no unverified invocations when second invocation of method is but first one is not.
     */
    @Test
    void verifyNoUnverifiedInvocationsWhenSecondInvocationOfMethodIsButFirstOneIsNot() {
        Throwable exception = Assertions.assertThrows(UnexpectedInvocation.class, () -> {
            mock.save(); // doesn't get verified
            mock.prepare();
            mock.save();

            new VerificationsInOrder() {
                {
                    mock.prepare();
                    mock.save();
                }
            };

            new FullVerifications() {
            };
        });
        assertTrue(exception.getMessage().contains("save()"));
    }
}
