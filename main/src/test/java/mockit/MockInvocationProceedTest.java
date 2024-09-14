/*
 * Copyright (c) 2006 JMockit developers
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public final class MockInvocationProceedTest {

    public static class BaseClassToBeMocked {
        protected String name;

        public final int baseMethod(int i) {
            return i + 1;
        }

        protected int methodToBeMocked(int i) throws IOException {
            return i;
        }
    }

    public static class ClassToBeMocked extends BaseClassToBeMocked {
        public ClassToBeMocked() {
            name = "";
        }

        public ClassToBeMocked(String name) {
            this.name = name;
        }

        public boolean methodToBeMocked() {
            return true;
        }

        protected int methodToBeMocked(int i, Object... args) {
            int result = i;

            for (Object arg : args) {
                if (arg != null)
                    result++;
            }

            return result;
        }

        public String anotherMethodToBeMocked(String s, boolean b, List<Integer> ints) {
            return (b ? s.toUpperCase() : s.toLowerCase()) + ints;
        }

        public static boolean staticMethodToBeMocked() throws FileNotFoundException {
            throw new FileNotFoundException();
        }

        protected static native void nativeMethod();
    }

    @Test
    public void proceedFromMockMethodWithoutParameters() {
        new MockUp<ClassToBeMocked>() {
            @Mock
            boolean methodToBeMocked(Invocation inv) {
                return inv.proceed();
            }
        };

        assertTrue(new ClassToBeMocked().methodToBeMocked());
    }

    @Test
    public void proceedFromMockMethodWithParameters() throws Exception {
        new MockUp<ClassToBeMocked>() {
            @Mock
            int methodToBeMocked(Invocation inv, int i) {
                Integer j = inv.proceed();
                return j + 1;
            }

            @Mock
            private int methodToBeMocked(Invocation inv, int i, Object... args) {
                args[2] = "mock";
                return inv.<Integer> proceed();
            }
        };

        ClassToBeMocked mocked = new ClassToBeMocked();

        assertEquals(124, mocked.methodToBeMocked(123));
        assertEquals(-8, mocked.methodToBeMocked(-9));
        assertEquals(7, mocked.methodToBeMocked(3, "Test", new Object(), null, 45));
    }

    @Test
    public void proceedConditionallyFromMockMethod() throws Exception {
        new MockUp<ClassToBeMocked>() {
            @Mock
            String anotherMethodToBeMocked(Invocation inv, String s, boolean b, List<Number> ints) {
                if (!b) {
                    return s;
                }

                ints.add(45);
                return inv.proceed();
            }
        };

        ClassToBeMocked mocked = new ClassToBeMocked();

        // Do not proceed:
        assertNull(mocked.anotherMethodToBeMocked(null, false, null));

        // Do proceed:
        List<Integer> values = new ArrayList<>();
        assertEquals("TEST[45]", mocked.anotherMethodToBeMocked("test", true, values));

        // Do not proceed again:
        assertEquals("No proceed", mocked.anotherMethodToBeMocked("No proceed", false, null));
    }

    @Test
    public void proceedFromMockMethodWhichThrowsCheckedException() throws Exception {
        new MockUp<ClassToBeMocked>() {
            @Mock
            boolean staticMethodToBeMocked(Invocation inv) throws Exception {
                if (inv.getInvocationIndex() == 0) {
                    return inv.<Boolean> proceed();
                }

                throw new InterruptedException("fake");
            }
        };

        try {
            ClassToBeMocked.staticMethodToBeMocked();
            fail();
        } catch (FileNotFoundException ignored) {
        }

        assertThrows(InterruptedException.class, () -> {
            ClassToBeMocked.staticMethodToBeMocked();
        });
    }

    @Test
    public void cannotProceedFromMockMethodIntoNativeMethod() {
        new MockUp<ClassToBeMocked>() {
            @Mock
            void nativeMethod(Invocation inv) {
                inv.proceed();
                fail("Should not get here");
            }
        };

        Throwable throwable = assertThrows(UnsupportedOperationException.class, () -> {
            ClassToBeMocked.nativeMethod();
        });
        assertEquals("Cannot proceed into real implementation of native method", throwable.getMessage());
    }

    @Test
    public void proceedFromMockMethodIntoConstructor() {
        new MockUp<ClassToBeMocked>() {
            @Mock
            void $init(Invocation inv) {
                assertNotNull(inv.<ClassToBeMocked> getInvokedInstance());
                inv.proceed();
            }
        };

        ClassToBeMocked obj = new ClassToBeMocked();
        assertEquals("", obj.name);
    }

    @Test
    public void proceedConditionallyFromMockMethodIntoConstructor() {
        new MockUp<ClassToBeMocked>() {
            @Mock
            void $init(Invocation inv, String name) {
                assertNotNull(inv.getInvokedInstance());

                if ("proceed".equals(name)) {
                    inv.proceed();
                }
            }
        };

        assertEquals("proceed", new ClassToBeMocked("proceed").name);
        assertNull(new ClassToBeMocked("do not proceed").name);
    }

    @Test
    public void proceedConditionallyFromMockMethodIntoJREConstructor() {
        new MockUp<File>() {
            @Mock
            void $init(Invocation inv, String name) {
                if ("proceed".equals(name)) {
                    inv.proceed();
                }
            }
        };

        assertEquals("proceed", new File("proceed").getPath());
        assertNull(new File("do not proceed").getPath());
    }

    @Test
    public void proceedFromMockMethodIntoMethodInheritedFromBaseClass() {
        new MockUp<ClassToBeMocked>() {
            @Mock
            int baseMethod(Invocation inv, int i) {
                return inv.proceed(i + 1);
            }
        };

        assertEquals(3, new ClassToBeMocked().baseMethod(1));
    }
}
