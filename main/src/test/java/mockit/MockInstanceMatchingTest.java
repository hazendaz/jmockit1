/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import java.util.concurrent.AbstractExecutorService;

import javax.sql.DataSource;

import mockit.integration.junit5.ExpectedException;
import mockit.integration.junit5.JMockitExtension;
import mockit.internal.expectations.invocation.MissingInvocation;
import mockit.internal.expectations.invocation.UnexpectedInvocation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.w3c.dom.Attr;

/**
 * The Class MockInstanceMatchingTest.
 */
@ExtendWith(JMockitExtension.class)
class MockInstanceMatchingTest {

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
    }

    /** The mock. */
    @Mocked
    Collaborator mock;

    /**
     * Match on mock instance.
     *
     * @param otherInstance
     *            the other instance
     */
    @Test
    void matchOnMockInstance(@Mocked Collaborator otherInstance) {
        new Expectations() {
            {
                mock.getValue();
                result = 12;
            }
        };

        int result = mock.getValue();
        Assertions.assertEquals(12, result);

        Collaborator another = new Collaborator();
        Assertions.assertEquals(0, another.getValue());
    }

    /**
     * Record on mock instance but replay on different instance.
     *
     * @param verifiedMock
     *            the verified mock
     */
    @Test
    @ExpectedException(MissingInvocation.class)
    void recordOnMockInstanceButReplayOnDifferentInstance(@Mocked final Collaborator verifiedMock) {
        new Expectations() {
            {
                verifiedMock.getValue();
                result = 12;
            }
        };

        Collaborator collaborator = new Collaborator();
        Assertions.assertEquals(0, collaborator.getValue());
    }

    /**
     * Verify expectation matching on mock instance.
     *
     * @param verifiedMock
     *            the verified mock
     */
    @Test
    void verifyExpectationMatchingOnMockInstance(@Mocked final Collaborator verifiedMock) {
        new Collaborator().setValue(12);
        verifiedMock.setValue(12);

        new Verifications() {
            {
                verifiedMock.setValue(12);
                times = 1;
            }
        };
    }

    /**
     * Verify expectations on same method call for different mocked instances.
     *
     * @param verifiedMock
     *            the verified mock
     */
    @Test
    void verifyExpectationsOnSameMethodCallForDifferentMockedInstances(@Mocked final Collaborator verifiedMock) {
        final Collaborator c1 = new Collaborator();
        c1.getValue();
        verifiedMock.getValue();
        final Collaborator c2 = new Collaborator();
        c2.getValue();

        new Verifications() {
            {
                verifiedMock.getValue();
                times = 1;
                c1.getValue();
                times = 1;
                c2.getValue();
                times = 1;
            }
        };
    }

    /**
     * Verify on mock instance but replay on different instance.
     *
     * @param verifiedMock
     *            the verified mock
     */
    @Test
    @ExpectedException(MissingInvocation.class)
    void verifyOnMockInstanceButReplayOnDifferentInstance(@Mocked final Collaborator verifiedMock) {
        new Collaborator().setValue(12);

        new Verifications() {
            {
                verifiedMock.setValue(12);
            }
        };
    }

    /**
     * Record expectations matching on multiple mock instances.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void recordExpectationsMatchingOnMultipleMockInstances(@Mocked final Collaborator mock2) {
        new Expectations() {
            {
                mock.getValue();
                result = 12;
                mock2.getValue();
                result = 13;
                mock.setValue(20);
            }
        };

        Assertions.assertEquals(12, mock.getValue());
        Assertions.assertEquals(13, mock2.getValue());
        mock.setValue(20);
    }

    /**
     * Record on specific mock instances but replay on different ones.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    @ExpectedException(MissingInvocation.class)
    void recordOnSpecificMockInstancesButReplayOnDifferentOnes(@Mocked final Collaborator mock2) {
        new Expectations() {
            {
                mock.setValue(12);
                mock2.setValue(13);
            }
        };

        mock2.setValue(12);
        mock.setValue(13);
    }

    /**
     * Verify expectations matching on multiple mock instances.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void verifyExpectationsMatchingOnMultipleMockInstances(@Mocked final Collaborator mock2) {
        mock.setValue(12);
        mock2.setValue(13);
        mock.setValue(20);

        new VerificationsInOrder() {
            {
                mock.setValue(12);
                mock2.setValue(13);
                mock.setValue(20);
            }
        };
    }

    /**
     * Verify on specific mock instances but replay on different ones.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    @ExpectedException(UnexpectedInvocation.class)
    void verifyOnSpecificMockInstancesButReplayOnDifferentOnes(@Mocked final Collaborator mock2) {
        mock2.setValue(12);
        mock.setValue(13);

        Assertions.assertThrows(MissingInvocation.class, () -> {
            new FullVerifications() {
                {
                    mock.setValue(12);
                    mock2.setValue(13);
                }
            };
        });
    }

    /**
     * Match on two mock instances.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void matchOnTwoMockInstances(@Mocked final Collaborator mock2) {
        new Expectations() {
            {
                mock.getValue();
                result = 1;
                times = 1;
                mock2.getValue();
                result = 2;
                times = 1;
            }
        };

        Assertions.assertEquals(1, mock.getValue());
        Assertions.assertEquals(2, mock2.getValue());
    }

    /**
     * Match on two mock instances and replay in different order.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void matchOnTwoMockInstancesAndReplayInDifferentOrder(@Mocked final Collaborator mock2) {
        new Expectations() {
            {
                mock.getValue();
                result = 1;
                mock2.getValue();
                result = 2;
            }
        };

        Assertions.assertEquals(2, mock2.getValue());
        Assertions.assertEquals(1, mock.getValue());
        Assertions.assertEquals(1, mock.getValue());
        Assertions.assertEquals(2, mock2.getValue());
    }

    /**
     * Match on two mock instances for otherwise identical expectations.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void matchOnTwoMockInstancesForOtherwiseIdenticalExpectations(@Mocked final Collaborator mock2) {
        mock.getValue();
        mock2.getValue();
        mock2.setValue(1);
        mock.setValue(1);

        new Verifications() {
            {
                mock.getValue();
                times = 1;
                mock2.getValue();
                times = 1;
            }
        };

        new VerificationsInOrder() {
            {
                mock2.setValue(1);
                mock.setValue(1);
            }
        };
    }

    /**
     * Verify expectations matching on multiple mock parameters but replayed out of order.
     *
     * @param es1
     *            the es 1
     * @param es2
     *            the es 2
     */
    @Test
    @ExpectedException(MissingInvocation.class)
    void verifyExpectationsMatchingOnMultipleMockParametersButReplayedOutOfOrder(
            @Mocked final AbstractExecutorService es1, @Mocked final AbstractExecutorService es2) {
        es2.execute(null);
        es1.submit((Runnable) null);

        new VerificationsInOrder() {
            {
                es1.execute((Runnable) any);
                es2.submit((Runnable) any);
            }
        };
    }

    /**
     * Record expectation matching on instance created inside code under test.
     */
    @Test
    void recordExpectationMatchingOnInstanceCreatedInsideCodeUnderTest() {
        new Expectations() {
            {
                new Collaborator().getValue();
                result = 1;
            }
        };

        Assertions.assertEquals(1, new Collaborator().getValue());
    }

    /**
     * Record expectations on two instances of same mocked interface.
     *
     * @param mockDS1
     *            the mock DS 1
     * @param mockDS2
     *            the mock DS 2
     * @param n
     *            the n
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void recordExpectationsOnTwoInstancesOfSameMockedInterface(@Mocked final DataSource mockDS1,
            @Mocked final DataSource mockDS2, @Mocked Attr n) throws Exception {
        new Expectations() {
            {
                mockDS1.getLoginTimeout();
                result = 1000;
                mockDS2.getLoginTimeout();
                result = 2000;
            }
        };

        Assertions.assertNotSame(mockDS1, mockDS2);
        Assertions.assertEquals(1000, mockDS1.getLoginTimeout());
        Assertions.assertEquals(2000, mockDS2.getLoginTimeout());
        mockDS2.setLoginTimeout(3000);

        new Verifications() {
            {
                mockDS2.setLoginTimeout(anyInt);
            }
        };
    }

    /**
     * The Class BaseClass.
     */
    static class BaseClass {
        /**
         * Do something.
         */
        final void doSomething() {
        }
    }

    /**
     * The Class SubclassA.
     */
    static final class SubclassA extends BaseClass {
        /**
         * Do something else.
         */
        void doSomethingElse() {
        }
    }

    /**
     * The Class SubclassB.
     */
    static final class SubclassB extends BaseClass {
        /**
         * Do something else.
         */
        void doSomethingElse() {
        }
    }

    /**
     * Verifying calls on specific instances of different subclasses.
     *
     * @param anyA
     *            the any A
     * @param a
     *            the a
     * @param anyB
     *            the any B
     */
    @Test
    void verifyingCallsOnSpecificInstancesOfDifferentSubclasses(@Mocked SubclassA anyA, @Mocked final SubclassA a,
            @Mocked final SubclassB anyB) {
        a.doSomething();
        new BaseClass().doSomething();
        anyB.doSomething();
        a.doSomethingElse();
        new SubclassA().doSomethingElse();
        anyB.doSomethingElse();

        new Verifications() {
            {
                a.doSomethingElse();
                times = 1;
                anyB.doSomethingElse();
                times = 1;
                a.doSomething();
                times = 1;
                anyB.doSomething();
                times = 1;
            }
        };
    }
}
