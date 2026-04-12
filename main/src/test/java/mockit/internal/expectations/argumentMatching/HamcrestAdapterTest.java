/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.argumentMatching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsNot;
import org.hamcrest.core.IsSame;
import org.junit.jupiter.api.Test;

final class HamcrestAdapterTest {

    @Test
    void matchesReturnsTrueWhenMatcherMatches() {
        HamcrestAdapter adapter = new HamcrestAdapter(IsEqual.equalTo("hello"));
        assertTrue(adapter.matches("hello"));
    }

    @Test
    void matchesReturnsFalseWhenMatcherDoesNotMatch() {
        HamcrestAdapter adapter = new HamcrestAdapter(IsEqual.equalTo("hello"));
        assertFalse(adapter.matches("world"));
    }

    @Test
    void sameReturnsTrueForSameMatcher() {
        org.hamcrest.Matcher<String> matcher = IsEqual.equalTo("hello");
        HamcrestAdapter adapter1 = new HamcrestAdapter(matcher);
        HamcrestAdapter adapter2 = new HamcrestAdapter(matcher);
        assertTrue(adapter1.same(adapter2));
    }

    @Test
    void sameReturnsFalseForDifferentMatcher() {
        HamcrestAdapter adapter1 = new HamcrestAdapter(IsEqual.equalTo("hello"));
        HamcrestAdapter adapter2 = new HamcrestAdapter(IsEqual.equalTo("hello"));
        assertFalse(adapter1.same(adapter2));
    }

    @Test
    void writeMismatchPhraseAppendsMatcherDescription() {
        HamcrestAdapter adapter = new HamcrestAdapter(IsEqual.equalTo("hello"));
        ArgumentMismatch mismatch = new ArgumentMismatch();
        adapter.writeMismatchPhrase(mismatch);
        String result = mismatch.toString();
        assertNotNull(result);
        assertTrue(result.contains("hello"));
    }

    @Test
    void writeMismatchPhraseWithNotMatcher() {
        HamcrestAdapter adapter = new HamcrestAdapter(IsNot.not("hello"));
        ArgumentMismatch mismatch = new ArgumentMismatch();
        adapter.writeMismatchPhrase(mismatch);
        String result = mismatch.toString();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getInnerValueFromIsEqualMatcher() {
        HamcrestAdapter adapter = new HamcrestAdapter(IsEqual.equalTo("hello"));
        Object innerValue = adapter.getInnerValue();
        assertEquals("hello", innerValue);
    }

    @Test
    void getInnerValueFromIsSameMatcher() {
        String target = "hello";
        HamcrestAdapter adapter = new HamcrestAdapter(IsSame.sameInstance(target));
        Object innerValue = adapter.getInnerValue();
        assertEquals(target, innerValue);
    }

    @Test
    void getInnerValueFromIsWrapperExtractsInnerValue() {
        HamcrestAdapter adapter = new HamcrestAdapter(Is.is("hello"));
        Object innerValue = adapter.getInnerValue();
        assertEquals("hello", innerValue);
    }

    @Test
    void getInnerValueFromIsNotWrapperUnwrapsInnerMatcher() {
        HamcrestAdapter adapter = new HamcrestAdapter(IsNot.not("hello"));
        Object innerValue = adapter.getInnerValue();
        assertEquals("hello", innerValue);
    }

    @Test
    void getInnerValueFromNestedIsWrappersExtractsInnerValue() {
        HamcrestAdapter adapter = new HamcrestAdapter(Is.is(Is.is("hello")));
        Object innerValue = adapter.getInnerValue();
        assertEquals("hello", innerValue);
    }

    @Test
    void matchesNullArgument() {
        HamcrestAdapter adapter = new HamcrestAdapter(IsEqual.equalTo(null));
        assertTrue(adapter.matches(null));
    }
}
