/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests;

import org.junit.jupiter.api.Test;

class ShutdownTest {
    @Test
    void addShutdownHookToExerciseSUTAfterTestRunHasFinished() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                exerciseSUT();
            }
        });
    }

    void exerciseSUT() {
        new ClassNotExercised().doSomething(123, "not to be counted");
        ClassWithNestedClasses.doSomething();
    }
}
