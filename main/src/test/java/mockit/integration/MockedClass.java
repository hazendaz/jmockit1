/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.integration;

public final class MockedClass {
    public String getValue() {
        return "REAL";
    }

    public boolean doSomething(int i) {
        return i > 0;
    }

    public boolean doSomethingElse(int i) {
        return i < 0;
    }
}
