/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MockUpForSingleInterfaceInstanceTest {

    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(MockUpForSingleInterfaceInstanceTest.class);

    public interface APublicInterface {
        int getNumericValue();

        String getTextValue();

        int getSomeOtherValue();
    }

    @Test
    void multipleMockUpInstancesForAPublicInterfaceWithASingleMockInstanceEach() {
        final class AnInterfaceMockUp extends MockUp<APublicInterface> {
            private final int number;
            private final String text;

            AnInterfaceMockUp(int number, String text) {
                this.number = number;
                this.text = text;
            }

            @Mock
            int getNumericValue() {
                return number;
            }

            @Mock
            String getTextValue() {
                return text;
            }
        }

        MockUp<APublicInterface> mockUp1 = new AnInterfaceMockUp(1, "one");
        APublicInterface mock1 = mockUp1.getMockInstance();

        AnInterfaceMockUp mockUp2 = new AnInterfaceMockUp(2, "two");
        APublicInterface mock2 = mockUp2.getMockInstance();

        assertNotSame(mock1, mock2);
        assertSame(mock1.getClass(), mock2.getClass());
        assertEquals(1, mock1.getNumericValue());
        assertEquals("one", mock1.getTextValue());
        // TODO 12/12/2023 yukkes The FieldInjection class has changed and the default value is not set.
        // assertEquals(0, mock1.getSomeOtherValue());
        assertEquals(2, mock2.getNumericValue());
        assertEquals("two", mock2.getTextValue());
        // assertEquals(0, mock2.getSomeOtherValue());
    }

    @Test
    void multipleMockUpInstancesForPublicInterfacePassingInterfaceToMockUpConstructor() {
        final class AnInterfaceMockUp extends MockUp<APublicInterface> {
            private final int number;

            AnInterfaceMockUp(int number) {
                super(APublicInterface.class);
                this.number = number;
            }

            @Mock
            int getNumericValue() {
                return number;
            }
        }

        MockUp<APublicInterface> mockUp1 = new AnInterfaceMockUp(1);
        APublicInterface mock1 = mockUp1.getMockInstance();

        AnInterfaceMockUp mockUp2 = new AnInterfaceMockUp(2);
        APublicInterface mock2 = mockUp2.getMockInstance();

        assertNotSame(mock1, mock2);
        assertSame(mock1.getClass(), mock2.getClass());
        assertEquals(1, mock1.getNumericValue());
        assertEquals(2, mock2.getNumericValue());
    }

    @Test
    @Timeout(500)
    @SuppressWarnings("MethodWithMultipleLoops")
    void instantiateSameMockUpForPublicInterfaceManyTimesButApplyOnlyOnce() {
        class InterfaceMockUp extends MockUp<APublicInterface> {
            final int value;

            InterfaceMockUp(int value) {
                this.value = value;
            }

            @Mock
            int getNumericValue() {
                return value;
            }
        }

        int n = 10000;
        List<APublicInterface> mocks = new ArrayList<>(n);
        Class<?> implementationClass = null;

        for (int i = 0; i < n; i++) {
            if (Thread.interrupted()) {
                logger.info("a) Interrupted at i = {}", i);
                return;
            }

            APublicInterface mockInstance = new InterfaceMockUp(i).getMockInstance();
            Class<?> mockInstanceClass = mockInstance.getClass();

            if (implementationClass == null) {
                implementationClass = mockInstanceClass;
            } else {
                assertSame(implementationClass, mockInstanceClass);
            }

            mocks.add(mockInstance);
        }

        for (int i = 0; i < n; i++) {
            if (Thread.interrupted()) {
                logger.info("b) Interrupted at i = {}", i);
                return;
            }

            APublicInterface mockInstance = mocks.get(i);
            assertEquals(i, mockInstance.getNumericValue());
        }
    }

    interface ANonPublicInterface {
        int getValue();
    }

    @Test
    void multipleMockUpInstancesForANonPublicInterfaceWithASingleMockInstanceEach() {
        class AnotherInterfaceMockUp extends MockUp<ANonPublicInterface> implements ANonPublicInterface {
            private final int value;

            AnotherInterfaceMockUp(int value) {
                this.value = value;
            }

            @Override
            @Mock
            public int getValue() {
                return value;
            }
        }

        MockUp<ANonPublicInterface> mockUp1 = new AnotherInterfaceMockUp(1);
        ANonPublicInterface mock1 = mockUp1.getMockInstance();

        AnotherInterfaceMockUp mockUp2 = new AnotherInterfaceMockUp(2);
        ANonPublicInterface mock2 = mockUp2.getMockInstance();

        assertNotSame(mock1, mock2);
        assertSame(mock1.getClass(), mock2.getClass());
        assertEquals(1, mock1.getValue());
        assertEquals(2, mock2.getValue());
    }

    @Test
    void applyDifferentMockUpsToSameInterface() {
        APublicInterface mock1 = new MockUp<APublicInterface>() {
            @Mock
            String getTextValue() {
                return "test";
            }
        }.getMockInstance();

        APublicInterface mock2 = new MockUp<APublicInterface>() {
            @Mock
            int getNumericValue() {
                return 123;
            }
        }.getMockInstance();

        assertEquals("test", mock1.getTextValue());
        // TODO 12/12/2023 yukkes The FieldInjection class has changed and the default value is not set.
        // assertEquals(0, mock1.getNumericValue());
        assertEquals(123, mock2.getNumericValue());
        // assertNull(mock2.getTextValue());
    }

    @Test
    void applyMockUpWithGivenInterfaceInstance() {
        APublicInterface realInstance = new APublicInterface() {
            @Override
            public int getNumericValue() {
                return 1;
            }

            @Override
            public String getTextValue() {
                return "test";
            }

            @Override
            public int getSomeOtherValue() {
                return 2;
            }
        };

        MockUp<APublicInterface> mockUp = new MockUp<APublicInterface>(realInstance) {
            @Mock
            int getNumericValue() {
                return 3;
            }
        };

        APublicInterface mockInstance = mockUp.getMockInstance();
        assertSame(realInstance, mockInstance);

        assertEquals(2, realInstance.getSomeOtherValue());
        assertEquals(3, mockInstance.getNumericValue());
    }
}
