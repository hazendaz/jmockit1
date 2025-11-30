/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertThrows;

import mockit.integration.junit5.JMockitExtension;
import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * This class tests the invocation constraints for mocked methods using JMockit. It verifies that the @Mock annotation's
 * invocations, minInvocations, and maxInvocations parameters work correctly by expecting specific exceptions or
 * allowing calls within limits.
 */
@ExtendWith(JMockitExtension.class)
public final class InvocationConstraintsTest {

    /**
     * A simple class under test that depends on a Collaborator.
     */
    static class CodeUnderTest {
        private final Collaborator dependency = new Collaborator();

        void doSomething() {
            dependency.provideSomeService();
        }
    }

    /**
     * A collaborator class that provides a service, throwing an exception in the real implementation.
     */
    static class Collaborator {
        void provideSomeService() {
            throw new RuntimeException("Real provideSomeService() called");
        }
    }

    final CodeUnderTest codeUnderTest = new CodeUnderTest();

    /**
     * Helper method to verify missing invocations for MockUp with invocation constraints. This mimics the behavior of
     * JUnit 4's @Rule approach.
     */
    private static void verifyMockUpInvocations() {
        mockit.internal.state.TestRun.getFakeStates().verifyMissingInvocations();
    }

    // Tests for @Mock(invocations = N) with different call counts

    @Test
    public void testInvocationsWith0Calls() {
        assertThrows(MissingInvocation.class, () -> {
            new MockUp<Collaborator>() {
                @Mock(invocations = 1)
                void provideSomeService() {
                }
            };

            // Call 0 times
            verifyMockUpInvocations();
        });
    }

    @Test
    public void testInvocationsWith1Call() {
        new MockUp<Collaborator>() {
            @Mock(invocations = 1)
            void provideSomeService() {
            }
        };

        codeUnderTest.doSomething(); // Call 1 time
    }

    @Test
    public void testInvocationsWith2Calls() {
        new MockUp<Collaborator>() {
            @Mock(invocations = 1)
            void provideSomeService() {
            }
        };

        codeUnderTest.doSomething(); // Call 1 time - OK

        // Call 2nd time - UnexpectedInvocation will be thrown immediately
        assertThrows(UnexpectedInvocation.class, () -> {
            codeUnderTest.doSomething();
        });
    }

    // Tests for @Mock(minInvocations = N) with different call counts

    @Test
    public void testMinInvocationsWith0Calls() {
        assertThrows(MissingInvocation.class, () -> {
            new MockUp<Collaborator>() {
                @Mock(minInvocations = 1)
                void provideSomeService() {
                }
            };

            // Call 0 times
            verifyMockUpInvocations();
        });
    }

    @Test
    public void testMinInvocationsWith1Call() {
        new MockUp<Collaborator>() {
            @Mock(minInvocations = 1)
            void provideSomeService() {
            }
        };

        codeUnderTest.doSomething(); // Call 1 time
    }

    @Test
    public void testMinInvocationsWith2Calls() {
        new MockUp<Collaborator>() {
            @Mock(minInvocations = 1)
            void provideSomeService() {
            }
        };

        codeUnderTest.doSomething(); // Call 2 times
        codeUnderTest.doSomething();
    }

    // Tests for @Mock(maxInvocations = N) with different call counts

    @Test
    public void testMaxInvocationsWith0Calls() {
        new MockUp<Collaborator>() {
            @Mock(maxInvocations = 1)
            void provideSomeService() {
            }
        };

        // Call 0 times
    }

    @Test
    public void testMaxInvocationsWith1Call() {
        new MockUp<Collaborator>() {
            @Mock(maxInvocations = 1)
            void provideSomeService() {
            }
        };

        codeUnderTest.doSomething(); // Call 1 time
    }

    @Test
    public void testMaxInvocationsWith2Calls() {
        new MockUp<Collaborator>() {
            @Mock(maxInvocations = 1)
            void provideSomeService() {
            }
        };

        codeUnderTest.doSomething(); // Call 1 time - OK

        // Call 2nd time - UnexpectedInvocation will be thrown immediately
        assertThrows(UnexpectedInvocation.class, () -> {
            codeUnderTest.doSomething();
        });
    }
}
