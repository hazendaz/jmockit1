/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.expectations.argumentMatching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ArgumentMismatchTest {

    @Test
    void appendNullValue() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted((Object) null);
        assertEquals("null", mismatch.toString());
    }

    @Test
    void appendStringValue() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted("hello");
        assertEquals("\"hello\"", mismatch.toString());
    }

    @Test
    void appendStringWithEscapedQuote() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted("say \"hi\"");
        assertEquals("\"say \\\"hi\\\"\"", mismatch.toString());
    }

    @Test
    void appendStringWithTabAndNewline() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted("a\tb\nc");
        assertEquals("\"a\\tb\\nc\"", mismatch.toString());
    }

    @Test
    void appendStringWithCarriageReturn() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted("a\rb");
        assertEquals("\"a\\rb\"", mismatch.toString());
    }

    @Test
    void appendCharValue() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted('x');
        assertEquals("'x'", mismatch.toString());
    }

    @Test
    void appendCharValueWithEscapedSingleQuote() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted('\'');
        assertEquals("'\\''", mismatch.toString());
    }

    @Test
    void appendByteValue() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted((byte) 42);
        assertEquals("42b", mismatch.toString());
    }

    @Test
    void appendShortValue() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted((short) 100);
        assertEquals("100s", mismatch.toString());
    }

    @Test
    void appendLongValue() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted(123L);
        assertEquals("123L", mismatch.toString());
    }

    @Test
    void appendFloatValue() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted(1.5f);
        assertEquals("1.5F", mismatch.toString());
    }

    @Test
    void appendIntValue() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted(42);
        assertEquals("42", mismatch.toString());
    }

    @Test
    void appendDoubleValue() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted(3.14);
        assertEquals("3.14", mismatch.toString());
    }

    @Test
    void appendBooleanValue() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted(true);
        assertEquals("true", mismatch.toString());
    }

    @Test
    void appendIntArray() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted(new int[] { 1, 2, 3 });
        assertEquals("[1, 2, 3]", mismatch.toString());
    }

    @Test
    void appendObjectArray() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted((Object) new String[] { "a", "b" });
        assertEquals("[\"a\", \"b\"]", mismatch.toString());
    }

    @Test
    void appendObjectWithCustomToString() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted(new Object() {
            @Override
            public String toString() {
                return "custom";
            }
        });
        assertEquals("\"custom\"", mismatch.toString());
    }

    @Test
    void appendObjectWithEmptyToString() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted(new Object() {
            @Override
            public String toString() {
                return "";
            }
        });
        String result = mismatch.toString();
        assertNotNull(result);
        assertTrue(result.contains("Object@") || result.startsWith("mockit"));
    }

    @Test
    void appendMultipleValuesArray() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted(new Object[] { "hello", 42 });
        assertEquals("\"hello\", 42", mismatch.toString());
    }

    @Test
    void appendFormattedWithMatcher() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        HamcrestAdapter matcher = new HamcrestAdapter(org.hamcrest.core.IsEqual.equalTo("hello"));
        mismatch.appendFormatted("String", "hello", matcher);
        String result = mismatch.toString();
        assertNotNull(result);
        assertTrue(result.contains("hello"));
    }

    @Test
    void appendFormattedWithNullMatcher() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.appendFormatted("String", "hello", null);
        assertEquals("\"hello\"", mismatch.toString());
    }

    @Test
    void getParameterTypeIsNullByDefault() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        assertNull(mismatch.getParameterType());
    }

    @Test
    void getParameterTypeAfterAppendFormattedWithMatcher() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        HamcrestAdapter matcher = new HamcrestAdapter(org.hamcrest.core.IsEqual.equalTo("hello"));
        mismatch.appendFormatted("java.lang.String", "hello", matcher);
        assertEquals("java.lang.String", mismatch.getParameterType());
    }

    @Test
    void appendChar() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.append('A');
        assertEquals("A", mismatch.toString());
    }

    @Test
    void appendInt() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.append(99);
        assertEquals("99", mismatch.toString());
    }

    @Test
    void appendDouble() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.append(2.718);
        assertEquals("2.718", mismatch.toString());
    }

    @Test
    void appendCharSequence() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.append("test");
        assertEquals("test", mismatch.toString());
    }

    @Test
    void appendNullCharSequence() {
        ArgumentMismatch mismatch = new ArgumentMismatch();
        mismatch.append((CharSequence) null);
        assertEquals("null", mismatch.toString());
    }
}
