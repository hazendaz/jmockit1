/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.argumentMatching;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class PatternMatcherTest {

    @Test
    void matchesValidPattern() {
        PatternMatcher matcher = new PatternMatcher("[a-z]+");
        assertTrue(matcher.matches("hello"));
    }

    @Test
    void doesNotMatchNonMatchingString() {
        PatternMatcher matcher = new PatternMatcher("[a-z]+");
        assertFalse(matcher.matches("123"));
    }

    @Test
    void doesNotMatchNull() {
        PatternMatcher matcher = new PatternMatcher("[a-z]+");
        assertFalse(matcher.matches(null));
    }

    @Test
    void doesNotMatchNonCharSequence() {
        PatternMatcher matcher = new PatternMatcher("[a-z]+");
        assertFalse(matcher.matches(Integer.valueOf(42)));
    }

    @Test
    void samePatternReturnsFalseForDifferentPattern() {
        PatternMatcher matcher1 = new PatternMatcher("[a-z]+");
        PatternMatcher matcher2 = new PatternMatcher("[0-9]+");
        assertFalse(matcher1.same(matcher2));
    }

    @Test
    void writeMismatchPhrase() {
        PatternMatcher matcher = new PatternMatcher("[a-z]+");
        ArgumentMismatch mismatch = new ArgumentMismatch();
        matcher.writeMismatchPhrase(mismatch);
        assertTrue(mismatch.toString().contains("[a-z]+"));
    }
}
