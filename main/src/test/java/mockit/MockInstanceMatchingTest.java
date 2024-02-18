package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.concurrent.AbstractExecutorService;

import javax.sql.DataSource;

import mockit.internal.expectations.invocation.MissingInvocation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Attr;

/**
 * The Class MockInstanceMatchingTest.
 */
public final class MockInstanceMatchingTest {

    /** The thrown. */
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

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
    public void matchOnMockInstance(@Mocked Collaborator otherInstance) {
        new Expectations() {
            {
                mock.getValue();
                result = 12;
            }
        };

        int result = mock.getValue();
        assertEquals(12, result);

        Collaborator another = new Collaborator();
        assertEquals(0, another.getValue());
    }

    /**
     * Record on mock instance but replay on different instance.
     *
     * @param verifiedMock
     *            the verified mock
     */
    @Test
    public void recordOnMockInstanceButReplayOnDifferentInstance(@Mocked final Collaborator verifiedMock) {
        thrown.expect(MissingInvocation.class);

        new Expectations() {
            {
                verifiedMock.getValue();
                result = 12;
            }
        };

        Collaborator collaborator = new Collaborator();
        assertEquals(0, collaborator.getValue());
    }

    /**
     * Verify expectation matching on mock instance.
     *
     * @param verifiedMock
     *            the verified mock
     */
    @Test
    public void verifyExpectationMatchingOnMockInstance(@Mocked final Collaborator verifiedMock) {
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
    public void verifyExpectationsOnSameMethodCallForDifferentMockedInstances(@Mocked final Collaborator verifiedMock) {
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
    public void verifyOnMockInstanceButReplayOnDifferentInstance(@Mocked final Collaborator verifiedMock) {
        thrown.expect(MissingInvocation.class);

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
    public void recordExpectationsMatchingOnMultipleMockInstances(@Mocked final Collaborator mock2) {
        new Expectations() {
            {
                mock.getValue();
                result = 12;
                mock2.getValue();
                result = 13;
                mock.setValue(20);
            }
        };

        assertEquals(12, mock.getValue());
        assertEquals(13, mock2.getValue());
        mock.setValue(20);
    }

    /**
     * Record on specific mock instances but replay on different ones.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    public void recordOnSpecificMockInstancesButReplayOnDifferentOnes(@Mocked final Collaborator mock2) {
        thrown.expect(MissingInvocation.class);

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
    public void verifyExpectationsMatchingOnMultipleMockInstances(@Mocked final Collaborator mock2) {
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
    public void verifyOnSpecificMockInstancesButReplayOnDifferentOnes(@Mocked final Collaborator mock2) {
        thrown.expect(MissingInvocation.class);

        mock2.setValue(12);
        mock.setValue(13);

        new FullVerifications() {
            {
                mock.setValue(12);
                mock2.setValue(13);
            }
        };
    }

    /**
     * Match on two mock instances.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    public void matchOnTwoMockInstances(@Mocked final Collaborator mock2) {
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

        assertEquals(1, mock.getValue());
        assertEquals(2, mock2.getValue());
    }

    /**
     * Match on two mock instances and replay in different order.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    public void matchOnTwoMockInstancesAndReplayInDifferentOrder(@Mocked final Collaborator mock2) {
        new Expectations() {
            {
                mock.getValue();
                result = 1;
                mock2.getValue();
                result = 2;
            }
        };

        assertEquals(2, mock2.getValue());
        assertEquals(1, mock.getValue());
        assertEquals(1, mock.getValue());
        assertEquals(2, mock2.getValue());
    }

    /**
     * Match on two mock instances for otherwise identical expectations.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    public void matchOnTwoMockInstancesForOtherwiseIdenticalExpectations(@Mocked final Collaborator mock2) {
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
    public void verifyExpectationsMatchingOnMultipleMockParametersButReplayedOutOfOrder(
            @Mocked final AbstractExecutorService es1, @Mocked final AbstractExecutorService es2) {
        thrown.expect(MissingInvocation.class);

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
    public void recordExpectationMatchingOnInstanceCreatedInsideCodeUnderTest() {
        new Expectations() {
            {
                new Collaborator().getValue();
                result = 1;
            }
        };

        assertEquals(1, new Collaborator().getValue());
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
    public void recordExpectationsOnTwoInstancesOfSameMockedInterface(@Mocked final DataSource mockDS1,
            @Mocked final DataSource mockDS2, @Mocked Attr n) throws Exception {
        new Expectations() {
            {
                mockDS1.getLoginTimeout();
                result = 1000;
                mockDS2.getLoginTimeout();
                result = 2000;
            }
        };

        assertNotSame(mockDS1, mockDS2);
        assertEquals(1000, mockDS1.getLoginTimeout());
        assertEquals(2000, mockDS2.getLoginTimeout());
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
    public void verifyingCallsOnSpecificInstancesOfDifferentSubclasses(@Mocked SubclassA anyA,
            @Mocked final SubclassA a, @Mocked final SubclassB anyB) {
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
