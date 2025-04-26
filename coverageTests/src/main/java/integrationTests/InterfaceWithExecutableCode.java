package integrationTests;

import java.security.SecureRandom;

/**
 * The Interface InterfaceWithExecutableCode.
 */
public interface InterfaceWithExecutableCode {

    /** The n. */
    int N = 1 + new SecureRandom().nextInt(10);

    /**
     * Do something.
     */
    void doSomething();
}
