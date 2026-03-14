/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.argumentMatching;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

final class ArgumentMatcherTest {

    @Test
    void nullityMatcherInstanceNotNull() {
        assertNotNull(NullityMatcher.INSTANCE);
    }

    @Test
    void nullityMatcherMatchesNull() {
        assertTrue(NullityMatcher.INSTANCE.matches(null));
    }

    @Test
    void nullityMatcherDoesNotMatchNonNull() {
        assertFalse(NullityMatcher.INSTANCE.matches("not null"));
    }

    @Test
    void nullityMatcherWritesMismatchPhrase() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        ((NullityMatcher) NullityMatcher.INSTANCE).writeMismatchPhrase(mismatch);
        assertTrue(mismatch.toString().contains("null"));
    }

    @Test
    void nonNullityMatcherInstanceNotNull() {
        assertNotNull(NonNullityMatcher.INSTANCE);
    }

    @Test
    void nonNullityMatcherMatchesNonNull() {
        assertTrue(NonNullityMatcher.INSTANCE.matches("hello"));
    }

    @Test
    void nonNullityMatcherDoesNotMatchNull() {
        assertFalse(NonNullityMatcher.INSTANCE.matches(null));
    }

    @Test
    void nonNullityMatcherWritesMismatchPhrase() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        ((NonNullityMatcher) NonNullityMatcher.INSTANCE).writeMismatchPhrase(mismatch);
        assertTrue(mismatch.toString().contains("not null"));
    }

    @Test
    void captureMatcherCapturesValue() {
        List<String> captured = new ArrayList<>();
        CaptureMatcher<String> matcher = new CaptureMatcher<>(captured);
        assertTrue(matcher.matches("test-value"));
        assertTrue(captured.contains("test-value"));
    }

    @Test
    void captureMatcherWithExpectedTypeCaptures() {
        List<String> captured = new ArrayList<>();
        CaptureMatcher<String> matcher = new CaptureMatcher<>(captured);
        matcher.setExpectedType(String.class);
        assertTrue(matcher.matches("test-value"));
        assertTrue(captured.contains("test-value"));
    }

    @Test
    void captureMatcherWithExpectedTypeDoesNotCaptureWrongType() {
        List<String> captured = new ArrayList<>();
        CaptureMatcher<String> matcher = new CaptureMatcher<>(captured);
        matcher.setExpectedType(String.class);
        // Integer doesn't match String type, but matches() still returns true
        assertTrue(matcher.matches(Integer.valueOf(42)));
        assertTrue(captured.isEmpty()); // Not captured because wrong type
    }

    @Test
    void captureMatcherSameReturnsFalse() {
        List<String> captured1 = new ArrayList<>();
        List<String> captured2 = new ArrayList<>();
        CaptureMatcher<String> matcher1 = new CaptureMatcher<>(captured1);
        CaptureMatcher<String> matcher2 = new CaptureMatcher<>(captured2);
        assertFalse(matcher1.same(matcher2));
    }

    @Test
    void stringContainmentMatcherMatchesContainingString() {
        StringContainmentMatcher matcher = new StringContainmentMatcher("hello");
        assertTrue(matcher.matches("say hello world"));
    }

    @Test
    void stringContainmentMatcherDoesNotMatchNonContaining() {
        StringContainmentMatcher matcher = new StringContainmentMatcher("hello");
        assertFalse(matcher.matches("goodbye"));
    }

    @Test
    void stringContainmentMatcherWritesMismatchPhrase() {
        StringContainmentMatcher matcher = new StringContainmentMatcher("hello");
        ArgumentMismatch mismatch = new ArgumentMismatch();
        matcher.writeMismatchPhrase(mismatch);
        assertTrue(mismatch.toString().contains("hello"));
    }

    @Test
    void stringSuffixMatcherMatchesSuffix() {
        StringSuffixMatcher matcher = new StringSuffixMatcher("world");
        assertTrue(matcher.matches("hello world"));
    }

    @Test
    void stringSuffixMatcherDoesNotMatchNonSuffix() {
        StringSuffixMatcher matcher = new StringSuffixMatcher("world");
        assertFalse(matcher.matches("world hello"));
    }

    @Test
    void stringSuffixMatcherWritesMismatchPhrase() {
        StringSuffixMatcher matcher = new StringSuffixMatcher("world");
        ArgumentMismatch mismatch = new ArgumentMismatch();
        matcher.writeMismatchPhrase(mismatch);
        assertTrue(mismatch.toString().contains("world"));
    }
}
