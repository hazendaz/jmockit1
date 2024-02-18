package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import java.awt.Graphics2D;
import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;

import org.junit.FixMethodOrder;
import org.junit.Test;

/**
 * The Class MultiThreadedExpectationsTest.
 */
@FixMethodOrder(NAME_ASCENDING)
public final class MultiThreadedExpectationsTest {

    /**
     * The Class Collaborator.
     */
    static class Collaborator {

        /**
         * Do something.
         *
         * @return the int
         */
        int doSomething() {
            return -1;
        }

        /**
         * Do something else.
         */
        void doSomethingElse() {
        }
    }

    /** The mock. */
    @Mocked
    Collaborator mock;

    /**
     * Use mocked collaborator from worker thread.
     */
    void useMockedCollaboratorFromWorkerThread() {
        Thread worker = new Thread() {
            @Override
            public void run() {
                mock.doSomethingElse();
            }
        };
        worker.start();
        try {
            worker.join();
        } catch (InterruptedException ignore) {
        }
    }

    /**
     * Use mocked object from worker thread while verifying expectation.
     */
    @Test
    public void useMockedObjectFromWorkerThreadWhileVerifyingExpectation() {
        mock.doSomething();
        mock.doSomething();

        new Verifications() {
            {
                mock.doSomething();
                useMockedCollaboratorFromWorkerThread();
                times = 2;
            }
        };
    }

    /**
     * Use mocked object from worker thread while recording and verifying expectation.
     */
    @Test
    public void useMockedObjectFromWorkerThreadWhileRecordingAndVerifyingExpectation() {
        new Expectations() {
            {
                mock.doSomething();
                useMockedCollaboratorFromWorkerThread();
                result = 123;
            }
        };

        assertEquals(123, mock.doSomething());
        mock.doSomethingElse();

        new VerificationsInOrder() {
            {
                useMockedCollaboratorFromWorkerThread();
                mock.doSomething();
                mock.doSomethingElse();
            }
        };
    }

    /**
     * Replay recorded expectation from another thread.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void replayRecordedExpectationFromAnotherThread() throws Exception {
        new Expectations() {
            {
                mock.doSomething();
            }
        };

        Thread task = new Thread() {
            @Override
            public void run() {
                mock.doSomething();
            }
        };
        task.start();
        task.join();
    }

    /**
     * The Class Dependency.
     */
    static class Dependency {

        /**
         * Do something.
         */
        void doSomething() {
        }

        /**
         * Do something else.
         */
        static void doSomethingElse() {
        }
    }

    /**
     * Verify invocations replayed in another thread whose class is no longer mocked.
     *
     * @param dep
     *            the dep
     * @param g2D
     *            the g 2 D
     * @param runnable
     *            the runnable
     */
    @Test
    public void verifyInvocationsReplayedInAnotherThreadWhoseClassIsNoLongerMocked(@Mocked final Dependency dep,
            @Mocked final Graphics2D g2D, @Mocked final Runnable runnable) {
        new Thread() {
            @Override
            public void run() {
                dep.doSomething();
                g2D.dispose();
                runnable.run();
                Dependency.doSomethingElse();
            }
        }.start();
    }

    /**
     * The Interface APublicInterface.
     */
    public interface APublicInterface {
        /**
         * Do something.
         *
         * @return true, if successful
         */
        boolean doSomething();
    }

    /**
     * Invoke method on mocked public interface from EDT.
     *
     * @param mock2
     *            the mock 2
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void invokeMethodOnMockedPublicInterfaceFromEDT(@Mocked final APublicInterface mock2) throws Exception {
        new Expectations() {
            {
                mock2.doSomething();
                result = true;
            }
        };

        SwingUtilities.invokeAndWait(() -> assertTrue(mock2.doSomething()));

        assertTrue(mock2.doSomething());
    }

    /**
     * The Class AnAbstractClass.
     */
    public abstract static class AnAbstractClass {
        /**
         * Do something.
         *
         * @return true, if successful
         */
        public abstract boolean doSomething();
    }

    /**
     * Invoke method on mocked abstract class from EDT.
     *
     * @param mock2
     *            the mock 2
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void invokeMethodOnMockedAbstractClassFromEDT(@Mocked final AnAbstractClass mock2) throws Exception {
        new Expectations() {
            {
                mock2.doSomething();
                result = true;
            }
        };

        SwingUtilities.invokeAndWait(() -> assertTrue(mock2.doSomething()));

        assertTrue(mock2.doSomething());
    }

    /**
     * Invoke method on mocked generic interface from EDT.
     *
     * @param mock2
     *            the mock 2
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void invokeMethodOnMockedGenericInterfaceFromEDT(@Mocked final Callable<Boolean> mock2) throws Exception {
        new Expectations() {
            {
                mock2.call();
                result = true;
            }
        };

        SwingUtilities.invokeAndWait(() -> {
            try {
                assertTrue(mock2.call());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertTrue(mock2.call());
    }
}
