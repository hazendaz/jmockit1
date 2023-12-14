package integrationTests;

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
