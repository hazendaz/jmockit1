/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.testRedundancy;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.lang.reflect.Method;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

public final class JUnitListener extends RunListener {
    @NonNull
    private final TestCoverage testCoverage;

    public JUnitListener(@NonNull TestCoverage testCoverage) {
        this.testCoverage = testCoverage;
    }

    @Override
    public void testStarted(@NonNull Description description) {
        if (description.isTest()) {
            Class<?> testClass = description.getTestClass();
            String testMethodName = description.getMethodName();

            for (Method testMethod : testClass.getDeclaredMethods()) {
                if (testMethod.getName().equals(testMethodName)) {
                    testCoverage.setCurrentTestMethod(testMethod);
                    return;
                }
            }
        }
    }

    @Override
    public void testFinished(@NonNull Description description) {
        if (description.isTest()) {
            testCoverage.setCurrentTestMethod(null);
        }
    }
}
