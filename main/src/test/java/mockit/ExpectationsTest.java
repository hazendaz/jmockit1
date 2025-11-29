/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import mockit.integration.junit5.ExpectedException;
import mockit.integration.junit5.JMockitExtension;
import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class ExpectationsTest.
 */
@ExtendWith(JMockitExtension.class)
@SuppressWarnings({ "unused", "ParameterHidesMemberVariable" })
class ExpectationsTest {

    /**
     * The Class Dependency.
     */
    @Deprecated
    public static class Dependency {

        /** The value. */
        @Deprecated
        int value;

        /**
         * Instantiates a new dependency.
         */
        @Deprecated
        public Dependency() {
            value = -1;
        }

        /**
         * Sets the something.
         *
         * @param value
         *            the new something
         */
        @Disabled("test")
        public void setSomething(@Deprecated int value) {
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
         * Do something.
         *
         * @param i
         *            the i
         * @param b
         *            the b
         *
         * @return the int
         */
        public int doSomething(Integer i, boolean b) {
            return i;
        }

        /**
         * Edits the A bunch more stuff.
         *
         * @return the int
         */
        public int editABunchMoreStuff() {
            return 1;
        }

        /**
         * Notify before save.
         *
         * @return true, if successful
         */
        public boolean notifyBeforeSave() {
            return true;
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
         * Static method.
         *
         * @param o
         *            the o
         * @param e
         *            the e
         *
         * @return the int
         */
        static int staticMethod(Object o, Exception e) {
            return -1;
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
     * Record simple invocations.
     */
    @Test
    void recordSimpleInvocations() {
        new Expectations() {
            {
                mock.prepare();
                mock.editABunchMoreStuff();
                mock.setSomething(45);
            }
        };

        exerciseCodeUnderTest();
    }

    /**
     * Record invocation that will not occur.
     */
    @Test
    void recordInvocationThatWillNotOccur() {
        new Expectations() {
            {
                mock.editABunchMoreStuff();
                result = 123;
                times = 0;
            }
        };

        mock.setSomething(123);
        mock.prepare();
    }

    /**
     * Expectations recorded on same method with same matchers but different arguments.
     */
    @Test
    void expectationsRecordedOnSameMethodWithSameMatchersButDifferentArguments() {
        new Expectations() {
            {
                mock.doSomething(1, anyBoolean);
                result = 1;
                mock.doSomething(2, anyBoolean);
                result = 2;
            }
        };

        assertEquals(1, mock.doSomething(1, true));
        assertEquals(2, mock.doSomething(2, false));
        assertEquals(0, mock.doSomething(3, false));
    }

    /**
     * Expectations recorded on same method with matcher in one and fixed argument in another.
     */
    @Test
    void expectationsRecordedOnSameMethodWithMatcherInOneAndFixedArgumentInAnother() {
        new Expectations() {
            {
                mock.doSomething(1, anyBoolean);
                result = 1;
                mock.doSomething(anyInt, anyBoolean);
                result = 2;
            }
        };

        assertEquals(1, mock.doSomething(1, true));
        assertEquals(2, mock.doSomething(null, false));
        assertEquals(2, mock.doSomething(2, true));
        assertEquals(1, mock.doSomething(1, false));
    }

    /**
     * Record invocation with exact expected number of invocations but fail to satisfy.
     */
    @Test
    @ExpectedException(MissingInvocation.class)
    void recordInvocationWithExactExpectedNumberOfInvocationsButFailToSatisfy() {
        new Expectations() {
            {
                mock.editABunchMoreStuff();
                times = 1;
            }
        };
    }

    /**
     * Record invocation with minimum expected number of invocations but fail to satisfy.
     */
    @Test
    @ExpectedException(MissingInvocation.class)
    void recordInvocationWithMinimumExpectedNumberOfInvocationsButFailToSatisfy() {
        new Expectations() {
            {
                mock.editABunchMoreStuff();
                minTimes = 2;
            }
        };
        mock.editABunchMoreStuff();
    }

    /**
     * Record invocation with maximum expected number of invocations but fail to satisfy.
     */
    @Test
    @ExpectedException(UnexpectedInvocation.class)
    void recordInvocationWithMaximumExpectedNumberOfInvocationsButFailToSatisfy() {
        new Expectations() {
            {
                mock.editABunchMoreStuff();
                maxTimes = 1;
            }
        };

        mock.editABunchMoreStuff();
        mock.editABunchMoreStuff();
    }

    /**
     * Record invocations with expected invocation counts.
     */
    @Test
    void recordInvocationsWithExpectedInvocationCounts() {
        new Expectations() {
            {
                mock.setSomethingElse(anyString);
                minTimes = 1;
                mock.save();
                times = 2;
            }
        };

        mock.setSomething(3);
        mock.save();
        mock.setSomethingElse("test");
        mock.save();
    }

    /**
     * Record invocations with min invocation count larger than will occur.
     */
    @Test
    @ExpectedException(MissingInvocation.class)
    void recordInvocationsWithMinInvocationCountLargerThanWillOccur() {
        new Expectations() {
            {
                mock.save();
                minTimes = 2;
            }
        };

        mock.save();
    }

    /**
     * Record with argument matcher and individual invocation counts.
     */
    @Test
    void recordWithArgumentMatcherAndIndividualInvocationCounts() {
        new Expectations() {
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

        exerciseCodeUnderTest();
    }

    /**
     * Record with max invocation count followed by return value.
     */
    @Test
    void recordWithMaxInvocationCountFollowedByReturnValue() {
        new Expectations() {
            {
                Dependency.staticMethod(any, null);
                maxTimes = 1;
                result = 1;
            }
        };

        assertEquals(1, Dependency.staticMethod(new Object(), new Exception()));
    }

    /**
     * Record with max invocation count followed by return value but replay one time beyond max.
     */
    @Test
    @ExpectedException(UnexpectedInvocation.class)
    void recordWithMaxInvocationCountFollowedByReturnValueButReplayOneTimeBeyondMax() {
        new Expectations() {
            {
                Dependency.staticMethod(any, null);
                maxTimes = 1;
                result = 1;
            }
        };

        Dependency.staticMethod(null, null);
        Dependency.staticMethod(null, null);
    }

    /**
     * Record with return value followed by expected invocation count.
     */
    @Test
    void recordWithReturnValueFollowedByExpectedInvocationCount() {
        new Expectations() {
            {
                Dependency.staticMethod(any, null);
                result = 1;
                times = 1;
            }
        };

        assertEquals(1, Dependency.staticMethod(null, null));
    }

    /**
     * Record with min invocation count followed by return value using delegate.
     */
    @Test
    void recordWithMinInvocationCountFollowedByReturnValueUsingDelegate() {
        new Expectations() {
            {
                Dependency.staticMethod(any, null);
                minTimes = 1;
                result = new Delegate<Object>() {
                    int staticMethod(Object o, Exception e) {
                        return 1;
                    }
                };
            }
        };

        assertEquals(1, Dependency.staticMethod(null, null));
    }

    /**
     * Mocked class with annotated elements.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void mockedClassWithAnnotatedElements() throws Exception {
        Class<?> mockedClass = mock.getClass();
        assertTrue(mockedClass.isAnnotationPresent(Deprecated.class));
        assertTrue(mockedClass.getDeclaredField("value").isAnnotationPresent(Deprecated.class));
        assertTrue(mockedClass.getDeclaredConstructor().isAnnotationPresent(Deprecated.class));

        Method mockedMethod = mockedClass.getDeclaredMethod("setSomething", int.class);
        Disabled disabled = mockedMethod.getAnnotation(Disabled.class);
        assertNotNull(disabled);
        assertEquals("test", disabled.value());
        assertTrue(mockedMethod.getParameterAnnotations()[0][0] instanceof Deprecated);
    }

    /**
     * The Class Collaborator.
     */
    static class Collaborator {

        /** The value. */
        private int value;

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

        /**
         * Provide some service.
         */
        void provideSomeService() {
        }

        /**
         * Do something.
         *
         * @param s
         *            the s
         *
         * @return the string
         */
        String doSomething(String s) {
            return s.toLowerCase();
        }

        /**
         * Do internal.
         *
         * @return the string
         */
        static String doInternal() {
            return "123";
        }
    }

    /**
     * Expect only one invocation but exercise others during replay.
     *
     * @param mock
     *            the mock
     */
    @Test
    @ExpectedException(UnexpectedInvocation.class)
    void expectOnlyOneInvocationButExerciseOthersDuringReplay(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
            }
        };

        mock.provideSomeService();
        mock.setValue(1);

        new FullVerifications() {
        };
    }

    /**
     * Expect nothing on mocked type but exercise it during replay.
     *
     * @param mock
     *            the mock
     */
    @Test
    @ExpectedException(UnexpectedInvocation.class)
    void expectNothingOnMockedTypeButExerciseItDuringReplay(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.setValue(anyInt);
                times = 0;
            }
        };

        mock.setValue(2);

        new FullVerifications() {
        };
    }

    /**
     * Replay with unexpected static method invocation.
     *
     * @param mock
     *            the mock
     */
    @Test
    @ExpectedException(MissingInvocation.class)
    void replayWithUnexpectedStaticMethodInvocation(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getValue();
            }
        };

        Collaborator.doInternal();

        Assertions.assertThrows(UnexpectedInvocation.class, () -> {
            new FullVerifications() {
            };
        });
    }

    /**
     * Failure from unexpected invocation in another thread.
     *
     * @param mock
     *            the mock
     *
     * @throws Exception
     *             the exception
     */
    @Test
    @ExpectedException(UnexpectedInvocation.class)
    void failureFromUnexpectedInvocationInAnotherThread(@Mocked final Collaborator mock) throws Exception {
        Thread t = new Thread() {
            @Override
            public void run() {
                mock.provideSomeService();
            }
        };

        new Expectations() {
            {
                mock.getValue();
            }
        };

        mock.getValue();
        t.start();
        t.join();

        new FullVerifications() {
        };
    }

    /**
     * Recording expectation on method with one argument but replaying with another should produce useful error message.
     *
     * @param mock
     *            the mock
     */
    @Test
    @ExpectedException(UnexpectedInvocation.class)
    void recordingExpectationOnMethodWithOneArgumentButReplayingWithAnotherShouldProduceUsefulErrorMessage(
            @Mocked final Collaborator mock) {
        final String expected = "expected";
        new Expectations() {
            {
                mock.doSomething(expected);
            }
        };

        mock.doSomething(expected);

        String another = "another";
        mock.doSomething(another);

        new FullVerifications() {
        };
    }
}
