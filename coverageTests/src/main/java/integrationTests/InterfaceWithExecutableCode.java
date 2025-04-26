package integrationTests;

import java.security.SecureRandom;

/**
 * The Interface InterfaceWithExecutableCode.
 */
public interface InterfaceWithExecutableCode {

    /** The n. */
    SecureRandom RANDOM = new SecureRandom();
    int N = 1 + RANDOM.nextInt(10);

    /**
     * Do something.
     */
    void doSomething();
}
