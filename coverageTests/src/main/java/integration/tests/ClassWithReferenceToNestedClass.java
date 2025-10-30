package integration.tests;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The Class ClassWithReferenceToNestedClass.
 */
public final class ClassWithReferenceToNestedClass {

    /**
     * Instantiates a new class with reference to nested class.
     */
    ClassWithReferenceToNestedClass() {
        new ReentrantReadWriteLock().readLock();
    }

    /**
     * Do something.
     *
     * @return true, if successful
     */
    public static boolean doSomething() {
        return true;
    }
}
