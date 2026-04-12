/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

final class MethodFormatterTest {

    @Test
    void singleArgConstructorCreatesEmptyOutput() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo");
        assertEquals("", formatter.toString());
        assertTrue(formatter.getParameterTypes().isEmpty());
    }

    @Test
    void twoArgConstructorFormatsSimpleMethod() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo", "bar()V");
        String result = formatter.toString();
        assertNotNull(result);
        assertTrue(result.contains("com.example.Foo#bar()"));
    }

    @Test
    void twoArgConstructorFormatsMethodWithIntParam() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo", "bar(I)V");
        String result = formatter.toString();
        assertTrue(result.contains("com.example.Foo#bar("));
        List<String> paramTypes = formatter.getParameterTypes();
        assertEquals(1, paramTypes.size());
        assertEquals("int", paramTypes.get(0));
    }

    @Test
    void twoArgConstructorFormatsMethodWithMultiplePrimitives() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo", "bar(IZD)V");
        List<String> paramTypes = formatter.getParameterTypes();
        assertEquals(3, paramTypes.size());
        assertEquals("int", paramTypes.get(0));
        assertEquals("boolean", paramTypes.get(1));
        assertEquals("double", paramTypes.get(2));
    }

    @Test
    void twoArgConstructorFormatsMethodWithLongParam() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo", "bar(J)V");
        List<String> paramTypes = formatter.getParameterTypes();
        assertEquals(1, paramTypes.size());
        assertEquals("long", paramTypes.get(0));
    }

    @Test
    void twoArgConstructorFormatsMethodWithFloatParam() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo", "bar(F)V");
        List<String> paramTypes = formatter.getParameterTypes();
        assertEquals(1, paramTypes.size());
        assertEquals("float", paramTypes.get(0));
    }

    @Test
    void twoArgConstructorFormatsMethodWithByteParam() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo", "bar(B)V");
        List<String> paramTypes = formatter.getParameterTypes();
        assertEquals(1, paramTypes.size());
        assertEquals("byte", paramTypes.get(0));
    }

    @Test
    void twoArgConstructorFormatsMethodWithCharParam() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo", "bar(C)V");
        List<String> paramTypes = formatter.getParameterTypes();
        assertEquals(1, paramTypes.size());
        assertEquals("char", paramTypes.get(0));
    }

    @Test
    void twoArgConstructorFormatsMethodWithShortParam() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo", "bar(S)V");
        List<String> paramTypes = formatter.getParameterTypes();
        assertEquals(1, paramTypes.size());
        assertEquals("short", paramTypes.get(0));
    }

    @Test
    void twoArgConstructorFormatsMethodWithObjectParam() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo", "bar(Ljava/lang/String;)V");
        List<String> paramTypes = formatter.getParameterTypes();
        assertEquals(1, paramTypes.size());
        assertEquals("String", paramTypes.get(0));
    }

    @Test
    void twoArgConstructorFormatsConstructor() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo", "<init>()V");
        String result = formatter.toString();
        assertTrue(result.contains("Foo#Foo()"));
    }

    @Test
    void threeArgConstructorWithFalseDoesNotAppendParams() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo", "bar(I)V", false);
        String result = formatter.toString();
        assertNotNull(result);
        List<String> paramTypes = formatter.getParameterTypes();
        assertEquals(1, paramTypes.size());
        assertEquals("int", paramTypes.get(0));
    }

    @Test
    void appendMethodAddsTextToOutput() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo");
        formatter.append("extra text");
        assertEquals("extra text", formatter.toString());
    }

    @Test
    void appendMethodConcatenatesMultipleStrings() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo");
        formatter.append("hello");
        formatter.append(" world");
        assertEquals("hello world", formatter.toString());
    }

    @Test
    void twoArgConstructorFormatsMethodWithArrayParam() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo", "bar([I)V");
        List<String> paramTypes = formatter.getParameterTypes();
        assertEquals(1, paramTypes.size());
        assertEquals("int[]", paramTypes.get(0));
    }

    @Test
    void twoArgConstructorFormatsMethodNoParams() {
        MethodFormatter formatter = new MethodFormatter("com/example/Foo", "bar()V");
        List<String> paramTypes = formatter.getParameterTypes();
        assertTrue(paramTypes.isEmpty());
        assertTrue(formatter.toString().contains("bar()"));
    }

    @Test
    void twoArgConstructorFormatsInnerClassConstructor() {
        MethodFormatter formatter = new MethodFormatter("com/example/Outer$Inner", "<init>()V");
        String result = formatter.toString();
        assertTrue(result.contains("Inner#Inner()"));
    }
}
