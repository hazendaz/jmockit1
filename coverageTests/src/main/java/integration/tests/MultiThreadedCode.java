package integration.tests;

/**
 * The Class MultiThreadedCode.
 */
public final class MultiThreadedCode {

    /**
     * Non blocking operation.
     *
     * @return the thread
     */
    public static Thread nonBlockingOperation() {
        Thread worker = new Thread(() -> new Object() // NPE only happened with this line break
        {
        });

        worker.start();
        return worker;
    }
}
