/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * The Class FullVerificationsTest.
 */
public final class FullVerificationsTest {

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
    public void verifyAllInvocations() {
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
    public void verifyAllInvocationsWithSomeOfThemRecorded() {
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
    public void verifyAllInvocationsWithThoseRecordedAsExpectedToOccurVerifiedImplicitly() {
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
    public void verifyAllInvocationsExceptThoseAlreadyVerifiedInAPreviousVerificationBlock() {
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
    public void verifyAllInvocationsWithOneMissing() {
        thrown.expect(UnexpectedInvocation.class);
        thrown.expectMessage("editABunchMoreStuff()");

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
    }

    /**
     * Verify unrecorded invocation that was expected to not happen.
     */
    @Test
    public void verifyUnrecordedInvocationThatWasExpectedToNotHappen() {
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
    public void verifyUnrecordedInvocationThatShouldNotHappenButDoes() {
        thrown.expect(UnexpectedInvocation.class);
        thrown.expectMessage("1 unexpected invocation");

        mock.setSomething(1);
        mock.notifyBeforeSave();

        new FullVerifications() {
            {
                mock.setSomething(1);
                mock.notifyBeforeSave();
                times = 0;
            }
        };
    }

    /**
     * Verify invocation that is allowed to happen any number of times and happens once.
     */
    @Test
    public void verifyInvocationThatIsAllowedToHappenAnyNumberOfTimesAndHappensOnce() {
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
    public void verifyRecordedInvocationThatIsAllowedToHappenAnyNoOfTimesAndDoesNotHappen() {
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
    public void verifyUnrecordedInvocationThatIsAllowedToHappenAnyNoOfTimesAndDoesNotHappen() {
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
    public void verifyUnrecordedInvocationThatShouldHappenButDoesNot() {
        mock.setSomething(1);

        thrown.expect(MissingInvocation.class);

        new FullVerifications() {
            {
                mock.notifyBeforeSave();
            }
        };
    }

    /**
     * Verify recorded invocation that should happen but does not.
     */
    @Test
    public void verifyRecordedInvocationThatShouldHappenButDoesNot() {
        new Expectations() {
            {
                mock.notifyBeforeSave();
            }
        };
        thrown.expect(MissingInvocation.class);

        mock.setSomething(1);

        new FullVerifications() {
            {
                mock.notifyBeforeSave();
            }
        };
    }

    /**
     * Verify all invocations with extra verification.
     */
    @Test
    public void verifyAllInvocationsWithExtraVerification() {
        mock.prepare();
        mock.setSomething(123);

        thrown.expect(MissingInvocation.class);
        thrown.expectMessage("notifyBeforeSave()");

        new FullVerifications() {
            {
                mock.prepare();
                mock.setSomething(123);
                mock.notifyBeforeSave();
            }
        };
    }

    /**
     * Verify all invocations with invocation count one less than actual.
     */
    @Test
    public void verifyAllInvocationsWithInvocationCountOneLessThanActual() {
        mock.setSomething(123);
        mock.setSomething(45);

        thrown.expect(UnexpectedInvocation.class);
        thrown.expectMessage("45");

        new FullVerifications() {
            {
                mock.setSomething(anyInt);
                times = 1;
            }
        };
    }

    /**
     * Verify all invocations with invocation count two less than actual.
     */
    @Test
    public void verifyAllInvocationsWithInvocationCountTwoLessThanActual() {
        mock.setSomething(123);
        mock.setSomething(45);
        mock.setSomething(1);

        thrown.expect(UnexpectedInvocation.class);
        thrown.expectMessage("2 unexpected invocations");
        thrown.expectMessage("1");

        new FullVerifications() {
            {
                mock.setSomething(anyInt);
                times = 1;
            }
        };
    }

    /**
     * Verify all invocations with invocation count more than actual.
     */
    @Test
    public void verifyAllInvocationsWithInvocationCountMoreThanActual() {
        mock.setSomethingElse('f');

        thrown.expect(MissingInvocation.class);
        thrown.expectMessage("Missing 2 invocations");
        thrown.expectMessage("any char");

        new FullVerifications() {
            {
                mock.setSomethingElse(anyChar);
                times = 3;
            }
        };
    }

    /**
     * Verify no invocations occurred on mocked dependency with one having occurred.
     */
    @Test
    public void verifyNoInvocationsOccurredOnMockedDependencyWithOneHavingOccurred() {
        mock.editABunchMoreStuff();

        thrown.expect(UnexpectedInvocation.class);

        new FullVerifications() {
        };
    }

    /**
     * Verify no invocations on mocked dependency beyond those recorded as expected.
     */
    @Test
    public void verifyNoInvocationsOnMockedDependencyBeyondThoseRecordedAsExpected() {
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
    public void verifyNoInvocationsOnMockedDependencyBeyondThoseRecordedAsExpectedWithOneHavingOccurred() {
        thrown.expect(UnexpectedInvocation.class);
        thrown.expectMessage("editABunchMoreStuff()");

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
    }

    /**
     * Verify no unverified invocations when first invocation of method is but second one is not.
     */
    @Test
    public void verifyNoUnverifiedInvocationsWhenFirstInvocationOfMethodIsButSecondOneIsNot() {
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

        thrown.expect(UnexpectedInvocation.class);
        thrown.expectMessage("save()");

        new FullVerifications() {
        };
    }

    /**
     * Verify no unverified invocations when second invocation of method is but first one is not.
     */
    @Test
    public void verifyNoUnverifiedInvocationsWhenSecondInvocationOfMethodIsButFirstOneIsNot() {
        mock.save(); // doesn't get verified
        mock.prepare();
        mock.save();

        new VerificationsInOrder() {
            {
                mock.prepare();
                mock.save();
            }
        };

        thrown.expect(UnexpectedInvocation.class);
        thrown.expectMessage("save()");

        new FullVerifications() {
        };
    }
}
