/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.invocation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class InvocationErrorsTest {

    @Test
    void missingInvocationToString() {
        MissingInvocation error = new MissingInvocation("method myMethod() not invoked");
        assertEquals("method myMethod() not invoked", error.toString());
    }

    @Test
    void unexpectedInvocationToString() {
        UnexpectedInvocation error = new UnexpectedInvocation("unexpected call to doSomething()");
        assertEquals("unexpected call to doSomething()", error.toString());
    }

    @Test
    void missingInvocationMessage() {
        MissingInvocation error = new MissingInvocation("missing call");
        assertEquals("missing call", error.getMessage());
    }

    @Test
    void unexpectedInvocationMessage() {
        UnexpectedInvocation error = new UnexpectedInvocation("unexpected call");
        assertEquals("unexpected call", error.getMessage());
    }
}
