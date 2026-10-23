/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package otherTests.coverage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import mockit.coverage.CallPoint;

import org.junit.jupiter.api.Test;

/**
 * The Class CallPointTest.
 */
final class CallPointTest {

    /**
     * Capture call point.
     *
     * @return the call point
     */
    private static CallPoint captureCallPoint() {
        // CallPoint#create looks for a test method starting at stack depth 2, assuming it is invoked through one level
        // of indirection (as is the case in production, where TestRun calls CallPoint.create, itself called from
        // instrumented application code, itself called from the test method). These two helper methods reproduce that
        // shape so the real detection logic can be exercised.
        return createFromThrowable();
    }

    /**
     * Creates the from throwable.
     *
     * @return the call point
     */
    private static CallPoint createFromThrowable() {
        try {
            Method method = CallPoint.class.getDeclaredMethod("create", Throwable.class);
            method.setAccessible(true);
            return (CallPoint) method.invoke(null, new Throwable());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates the finds the calling test method.
     */
    @Test
    void createFindsTheCallingTestMethod() {
        CallPoint callPoint = captureCallPoint();

        assertNotNull(callPoint);
        assertEquals("createFindsTheCallingTestMethod", callPoint.getStackTraceElement().getMethodName());
        assertEquals(CallPointTest.class.getName(), callPoint.getStackTraceElement().getClassName());
    }

    /**
     * Repetition count starts at zero and increments.
     */
    @Test
    void repetitionCountStartsAtZeroAndIncrements() {
        CallPoint callPoint = captureCallPoint();
        assertNotNull(callPoint);

        assertEquals(0, callPoint.getRepetitionCount());

        callPoint.incrementRepetitionCount();
        callPoint.incrementRepetitionCount();

        assertEquals(2, callPoint.getRepetitionCount());
    }

    /**
     * Checks if is same test method is true for call points from the same method.
     */
    @Test
    void isSameTestMethodIsTrueForCallPointsFromTheSameMethod() {
        CallPoint callPoint1 = captureCallPoint();
        CallPoint callPoint2 = captureCallPoint();

        assertNotNull(callPoint1);
        assertNotNull(callPoint2);
        assertTrue(callPoint1.isSameTestMethod(callPoint2));
    }

    /**
     * Checks if is same line in test code is false for call points from different lines.
     */
    @Test
    void isSameLineInTestCodeIsFalseForCallPointsFromDifferentLines() {
        CallPoint callPoint1 = captureCallPoint();
        CallPoint callPoint2 = captureCallPoint();

        assertNotNull(callPoint1);
        assertNotNull(callPoint2);
        assertFalse(callPoint1.isSameLineInTestCode(callPoint2));
    }

    /**
     * Creates the returns null when no test method is found on the stack.
     */
    @Test
    void createReturnsNullWhenNoTestMethodIsFoundOnTheStack() {
        // Called directly (no intermediate frame), so the real test method is one frame too shallow to be found.
        try {
            Method method = CallPoint.class.getDeclaredMethod("create", Throwable.class);
            method.setAccessible(true);
            assertEquals(null, (CallPoint) method.invoke(null, new Throwable()));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
