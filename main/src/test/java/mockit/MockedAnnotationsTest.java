package mockit;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import edu.umd.cs.findbugs.annotations.NonNull;

import jakarta.annotation.Resource;

import java.lang.annotation.Annotation;

import mockit.integration.junit5.JMockitExtension;

import org.checkerframework.checker.index.qual.NonNegative;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class MockedAnnotationsTest.
 */
@ExtendWith(JMockitExtension.class)
class MockedAnnotationsTest {

    /**
     * The Interface MyAnnotation.
     */
    public @interface MyAnnotation {

        /**
         * Value.
         *
         * @return the string
         */
        String value();

        /**
         * Flag.
         *
         * @return true, if successful
         */
        boolean flag() default true;

        /**
         * Values.
         *
         * @return the string[]
         */
        String[] values() default {};
    }

    /**
     * Specify values for annotation attributes.
     *
     * @param a
     *            the a
     */
    @Test
    void specifyValuesForAnnotationAttributes(@Mocked final MyAnnotation a) {
        assertSame(MyAnnotation.class, a.annotationType());

        new Expectations() {
            {
                a.flag();
                result = false;
                a.value();
                result = "test";
                a.values();
                returns("abc", "dEf");
            }
        };

        assertFalse(a.flag());
        assertEquals("test", a.value());
        assertArrayEquals(new String[] { "abc", "dEf" }, a.values());
    }

    /**
     * Verify uses of annotation attributes.
     *
     * @param a
     *            the a
     */
    @Test
    void verifyUsesOfAnnotationAttributes(@Mocked final MyAnnotation a) {
        new Expectations() {
            {
                a.value();
                result = "test";
                times = 2;
                a.values();
                returns("abc", "dEf");
            }
        };

        // Same rule for regular methods applies (ie, if no return value was recorded, invocations
        // will get the default for the return type).
        assertFalse(a.flag());

        assertEquals("test", a.value());
        assertArrayEquals(new String[] { "abc", "dEf" }, a.values());
        a.value();

        new FullVerifications() {
            {
                // Mocked methods called here always return the default value according to return type.
                a.flag();
            }
        };
    }

    /**
     * The Interface AnInterface.
     */
    @Resource
    public interface AnInterface {
    }

    /**
     * Mocking an annotated public interface.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockingAnAnnotatedPublicInterface(@Mocked AnInterface mock) {
        Annotation[] mockClassAnnotations = mock.getClass().getAnnotations();

        assertEquals(0, mockClassAnnotations.length);
    }

    /**
     * The Class ClassWithNullabilityAnnotations.
     */
    static class ClassWithNullabilityAnnotations {

        /**
         * Do something.
         *
         * @param i
         *            the i
         * @param obj
         *            the obj
         *
         * @return the string
         */
        @NonNull
        String doSomething(@NonNegative int i, @NonNull Object obj) {
            return "";
        }
    }

    /**
     * Mock class with nullability annotations.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockClassWithNullabilityAnnotations(@Injectable final ClassWithNullabilityAnnotations mock) {
        new Expectations() {
            {
                mock.doSomething(anyInt, any);
                result = "test";
            }
        };

        assertEquals("test", mock.doSomething(123, "test"));
    }

    /**
     * The Class ClassWithAnnotatedField.
     */
    static final class ClassWithAnnotatedField {
        /** The a field. */
        @Resource(type = int.class)
        Object aField;
    }

    /**
     * Mock class having field annotated with attribute having A primitive class as value.
     *
     * @param mock
     *            the mock
     */
    @Test
    void mockClassHavingFieldAnnotatedWithAttributeHavingAPrimitiveClassAsValue(@Mocked ClassWithAnnotatedField mock) {
        assertNull(mock.aField);
    }
}
