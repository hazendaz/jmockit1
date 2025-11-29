/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.integration.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for specifying expected exceptions in JUnit 5 tests using JMockit.
 * <p>
 * This annotation allows you to declare that a test method is expected to throw a specific exception type, optionally
 * with a specific message or message fragment. It is designed to help migrate JMockit-based tests from JUnit 4 to JUnit
 * 5 and flexible message matching.
 * <p>
 * <b>Usage examples:</b>
 *
 * <pre>
 * // Expect a MissingInvocation exception (type only)
 * &#64;Test
 * &#64;ExpectedException(MissingInvocation.class)
 * public void testMissingInvocation() { ... }
 *
 * // Expect a MissingInvocation exception with a message containing "missing call"
 * &#64;Test
 * &#64;ExpectedException(value = MissingInvocation.class, expectedMessages = {"missing call"})
 * public void testMissingInvocationWithMessage() { ... }
 *
 * // Expect either of two message fragments
 * &#64;Test
 * &#64;ExpectedException(value = MissingInvocation.class, expectedMessages = {"missing call", "not called"})
 * public void testMultipleMessages() { ... }
 *
 * // Use exact message matching
 * &#64;Test
 * &#64;ExpectedException(value = MissingInvocation.class, expectedMessages = {"Exact error message"}, messageContains = false)
 * public void testExactMessage() { ... }
 * </pre>
 * <p>
 * For standard exception assertions unrelated to JMockit, use JUnit 5's {@code assertThrows}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExpectedException {

    /**
     * Specifies the type of exception expected to be thrown by the test method.
     * <p>
     * Example:
     *
     * <pre>{@code
     * &#64;ExpectedException(MissingInvocation.class)
     * }</pre>
     *
     * @return the expected exception class
     */
    Class<? extends Throwable> value();

    /**
     * Specifies one or more expected message fragments or exact messages for the exception.
     * <p>
     * If multiple values are provided, the test passes if any fragment matches the exception message.
     *
     * @return array of expected message fragments or exact messages
     */
    String[] expectedMessages() default {};

    /**
     * If true, matches exception messages using {@code String.contains}; if false, uses {@code String.equals}.
     * <p>
     * Default is true for compatibility with JUnit 4's contains-style matching.
     *
     * @return true for contains matching; false for exact matching
     */
    boolean messageContains() default true;

}
