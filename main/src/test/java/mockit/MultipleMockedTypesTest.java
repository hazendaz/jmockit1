/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class MultipleMockedTypesTest.
 */
@ExtendWith(JMockitExtension.class)
class MultipleMockedTypesTest {

    /**
     * The Class FirstDependency.
     */
    public static class FirstDependency {
        /**
         * Gets the value.
         *
         * @return the value
         */
        public int getValue() {
            return 1;
        }
    }

    /**
     * The Class SecondDependency.
     */
    public static class SecondDependency {

        /**
         * Gets the value.
         *
         * @return the value
         */
        public int getValue() {
            return 2;
        }

        /**
         * Gets the different value.
         *
         * @return the different value
         */
        public int getDifferentValue() {
            return 3;
        }
    }

    /**
     * The Class TestedUnit.
     */
    public static class TestedUnit {

        /**
         * Validate.
         *
         * @param first
         *            the first
         *
         * @return true, if successful
         */
        public boolean validate(FirstDependency first) {
            SecondDependency second = new SecondDependency();
            return first.getValue() + second.getValue() <= 0;
        }

        /**
         * Validate with different value.
         *
         * @param first
         *            the first
         *
         * @return true, if successful
         */
        public boolean validateWithDifferentValue(FirstDependency first) {
            SecondDependency second = new SecondDependency();
            return first.getValue() + second.getDifferentValue() <= 0;
        }

        /**
         * Validate.
         *
         * @param first
         *            the first
         * @param second
         *            the second
         *
         * @return true, if successful
         */
        public boolean validate(FirstDependency first, SecondDependency second) {
            return first.getValue() + second.getValue() <= 0;
        }

        /**
         * Do something with internally created implementations.
         */
        static void doSomethingWithInternallyCreatedImplementations() {
            new Observer() {
                @Override
                public void update(Observable o, Object arg) {
                    throw new IllegalStateException();
                }
            }.update(null, "event");

            new Callable<String>() {
                @Override
                public String call() {
                    return "tested";
                }
            }.call();
        }
    }

    /** The mock 1. */
    @Mocked
    FirstDependency mock1;

    /**
     * Invocations on methods of different classes with different signatures.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void invocationsOnMethodsOfDifferentClassesWithDifferentSignatures(@Mocked final SecondDependency mock2) {
        new Expectations() {
            {
                mock1.getValue();
                result = 15;
                mock2.getDifferentValue();
                result = -50;
            }
        };

        assertTrue(new TestedUnit().validateWithDifferentValue(mock1));
    }

    /**
     * Invocations on methods of different classes but same signature.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void invocationsOnMethodsOfDifferentClassesButSameSignature(@Mocked final SecondDependency mock2) {
        new Expectations() {
            {
                mock1.getValue();
                result = 15;
                mock2.getValue();
                result = -50;
            }
        };

        assertTrue(new TestedUnit().validate(mock1));

        new VerificationsInOrder() {
            {
                mock1.getValue();
                mock2.getValue();
            }
        };
    }

    /**
     * The Class SubDependencyThatInherits.
     */
    public static final class SubDependencyThatInherits extends SecondDependency {
    }

    /**
     * The Class SubDependencyThatOverrides.
     */
    public static final class SubDependencyThatOverrides extends SecondDependency {
        @Override
        public int getValue() {
            return 1;
        }
    }

    /**
     * Invocation on base type with replay on subtype that overrides the invoked method.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void invocationOnBaseTypeWithReplayOnSubtypeThatOverridesTheInvokedMethod(@Mocked final SecondDependency mock2) {
        new Expectations() {
            {
                mock1.getValue();
                result = 15;
            }
        };

        // The executed method will be the override, which is not mocked.
        assertFalse(new TestedUnit().validate(mock1, new SubDependencyThatOverrides()));

        new FullVerifications() {
            {
                mock2.getValue();
                times = 0;
            }
        };
    }

    /**
     * Invocation on base type with capturing of subtype that inherits the invoked method.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void invocationOnBaseTypeWithCapturingOfSubtypeThatInheritsTheInvokedMethod(
            @Capturing final SecondDependency mock2) {
        new Expectations() {
            {
                mock1.getValue();
                result = 15;
                mock2.getValue();
                result = -50;
            }
        };

        assertTrue(new TestedUnit().validate(mock1, new SubDependencyThatInherits()));
    }

    /**
     * Invocation on base type with capturing of subtype that overrides the invoked method.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void invocationOnBaseTypeWithCapturingOfSubtypeThatOverridesTheInvokedMethod(
            @Capturing final SecondDependency mock2) {
        new Expectations() {
            {
                mock1.getValue();
                result = 15;
                mock2.getValue();
                result = -50;
            }
        };

        assertTrue(new TestedUnit().validate(mock1, new SubDependencyThatOverrides()));

        new VerificationsInOrder() {
            {
                mock1.getValue();
                mock2.getValue();
            }
        };
    }

    /**
     * Invocations on captured implementations of interfaces.
     *
     * @param callable
     *            the callable
     * @param observer
     *            the observer
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void invocationsOnCapturedImplementationsOfInterfaces(@Capturing final Callable<String> callable,
            @Capturing final Observer observer) throws Exception {
        new Expectations() {
            {
                observer.update(null, any);
                times = 1;
            }
        };

        TestedUnit.doSomethingWithInternallyCreatedImplementations();

        new Verifications() {
            {
                callable.call();
            }
        };
    }
}
