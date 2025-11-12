/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package otherTests;

import mockit.integration.MockedClass;

/**
 * The Class TestedClass.
 */
public final class TestedClass {

    /** The dependency. */
    private final MockedClass dependency;

    /**
     * Instantiates a new tested class.
     *
     * @param dependency
     *            the dependency
     */
    public TestedClass(MockedClass dependency) {
        this.dependency = dependency;
    }

    /**
     * Do something.
     *
     * @param i
     *            the i
     *
     * @return true, if successful
     */
    public boolean doSomething(int i) {
        return dependency.doSomething(i);
    }
}
