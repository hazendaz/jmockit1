package mockit;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import static mockit.ExpectationsWithArgMatchersTest.Delegates.collectionElement;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import mockit.internal.expectations.invocation.MissingInvocation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * The Class ExpectationsWithArgMatchersTest.
 */
public final class ExpectationsWithArgMatchersTest {

    /** The thrown. */
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    /**
     * The Class Collaborator.
     */
    @SuppressWarnings("unused")
    static class Collaborator {

        /**
         * Sets the value.
         *
         * @param value
         *            the new value
         */
        void setValue(int value) {
        }

        /**
         * Sets the value.
         *
         * @param value
         *            the new value
         */
        void setValue(double value) {
        }

        /**
         * Sets the value.
         *
         * @param value
         *            the value
         *
         * @return true, if successful
         */
        boolean setValue(float value) {
            return false;
        }

        /**
         * Sets the value.
         *
         * @param value
         *            the new value
         */
        void setValue(char value) {
        }

        /**
         * Sets the value.
         *
         * @param value
         *            the new value
         */
        void setValue(String value) {
        }

        /**
         * Sets the values.
         *
         * @param c
         *            the c
         * @param b
         *            the b
         */
        void setValues(char c, boolean b) {
        }

        /**
         * Sets the values.
         *
         * @param values
         *            the new values
         */
        void setValues(String[] values) {
        }

        /**
         * Sets the textual values.
         *
         * @param values
         *            the new textual values
         */
        void setTextualValues(Collection<String> values) {
        }

        /**
         * Do something.
         *
         * @param i
         *            the i
         */
        void doSomething(Integer i) {
        }

        /**
         * Do something.
         *
         * @param s
         *            the s
         *
         * @return true, if successful
         */
        boolean doSomething(String s) {
            return false;
        }

        /**
         * Simple operation.
         *
         * @param a
         *            the a
         * @param b
         *            the b
         * @param c
         *            the c
         */
        final void simpleOperation(int a, String b, Date c) {
        }

        /**
         * Sets the value.
         *
         * @param cert
         *            the new value
         */
        void setValue(Certificate cert) {
        }

        /**
         * Sets the value.
         *
         * @param ex
         *            the new value
         */
        void setValue(Exception ex) {
        }

        void setIntValue(int i) {
        }

        /**
         * Use object.
         *
         * @param arg
         *            the arg
         *
         * @return the string
         */
        String useObject(Object arg) {
            return "";
        }
    }

    /** The mock. */
    @Mocked
    Collaborator mock;

    /**
     * Verify expectation numeric equality matcher but fail to match on replay.
     */
    @Test
    public void verifyExpectationNumericEqualityMatcherButFailToMatchOnReplay() {
        thrown.expect(MissingInvocation.class);
        thrown.expectMessage("setValue");
        thrown.expectMessage("a numeric value within 0.01 of 2.3");
        thrown.expectMessage("instead got");
        thrown.expectMessage("setValue(2.32)");

        mock.setValue(2.32);

        new Verifications() {
            {
                mock.setValue(withEqual(2.3, 0.01));
            }
        };
    }

    /**
     * Verify expectation using numeric equality matcher but replay with non numeric parameter type.
     */
    @Test
    public void verifyExpectationUsingNumericEqualityMatcherButReplayWithNonNumericParameterType() {
        thrown.expect(MissingInvocation.class);

        mock.useObject('2');

        new Verifications() {
            {
                mock.useObject(withEqual(2.3, 0.01));
            }
        };
    }

    /**
     * Verify expectation using inequality matcher but fail to match on replay.
     */
    @Test
    public void verifyExpectationUsingInequalityMatcherButFailToMatchOnReplay() {
        thrown.expect(MissingInvocation.class);
        thrown.expectMessage("(not 2)");
        thrown.expectMessage("got");
        thrown.expectMessage("(2)");

        mock.setValue(2);

        new Verifications() {
            {
                mock.setValue(withNotEqual(2));
            }
        };
    }

    /**
     * Verify expectations using numeric equality matchers.
     */
    @Test
    public void verifyExpectationsUsingNumericEqualityMatchers() {
        new Expectations() {
            {
                mock.setValue(withEqual(2.0F, 0.01F));
                result = true;
                mock.setValue(withEqual(2.0F, 0.05F));
                result = false; // still overwrites the previous expectation, due to overlap in delta
            }
        };

        assertFalse(mock.setValue(2.0F));
        assertFalse(mock.setValue(2.05F));
    }

    /**
     * Record expectation with delegate without the parameter type.
     */
    @Test
    public void recordExpectationWithDelegateWithoutTheParameterType() {
        new Expectations() {
            {
                mock.useObject(with(new Delegate() { // only compiles for a parameter of type Object
                    @SuppressWarnings("unused")
                    boolean delegate(Object arg) {
                        return "test".equals(arg);
                    }
                }));
            }
        };

        String result = mock.useObject("test");

        assertNull(result);
    }

    /**
     * The Class CollectionElementDelegate.
     *
     * @param <T>
     *            the generic type
     */
    static final class CollectionElementDelegate<T> implements Delegate<Collection<T>> {

        /** The item. */
        private final T item;

        /**
         * Instantiates a new collection element delegate.
         *
         * @param item
         *            the item
         */
        CollectionElementDelegate(T item) {
            this.item = item;
        }

        /**
         * Checks for item.
         *
         * @param items
         *            the items
         *
         * @return true, if successful
         */
        @SuppressWarnings("unused")
        boolean hasItem(Collection<T> items) {
            return items.contains(item);
        }
    }

    /**
     * The Class Delegates.
     */
    @SuppressWarnings("unused")
    static final class Delegates {

        /**
         * Collection element.
         *
         * @param <T>
         *            the generic type
         * @param item
         *            the item
         *
         * @return the delegate
         */
        static <T> Delegate<Collection<T>> collectionElement(T item) {
            return new CollectionElementDelegate<>(item);
        }
    }

    /**
     * Expect invocations with named delegate matcher.
     */
    @Test
    public void expectInvocationsWithNamedDelegateMatcher() {
        new Expectations() {
            {
                mock.setTextualValues(with(collectionElement("B")));
            }
        };

        List<String> values = asList("a", "B", "c");
        mock.setTextualValues(values);
    }

    @Test
    public void expectInvocationsWithHamcrestMatcher() {
        new Expectations() {
            {
                mock.setTextualValues(this.<Collection<String>>withArgThat(hasItem("B")));
            }
        };

        List<String> values = asList("a", "B", "c");
        mock.setTextualValues(values);
    }

    @Test
    public void expectInvocationsWithHamcrestMatcher2() {
        new Expectations() {
            {
                mock.setTextualValues(withArgThat(containsInAnyOrder("B", "c", "a")));
            }
        };

        List<String> values = asList("a", "B", "c");
        mock.setTextualValues(values);
    }

    @Test
    public void expectInvocationWithMatcherContainingAnotherMatcher() {
        new Expectations() {
            {
                mock.setIntValue(withArgThat(equalTo(3)));
            }
        };

        mock.setIntValue(3);
    }

    /**
     * Use mocked method before recording expectation with argument matcher.
     */
    @Test
    public void useMockedMethodBeforeRecordingExpectationWithArgumentMatcher() {
        assertFalse(mock.doSomething("abc"));

        new Expectations() {
            {
                mock.doSomething(anyString);
                result = true;
            }
        };

        assertTrue(mock.doSomething("xyz"));
        assertTrue(mock.doSomething("abc"));
    }

    /**
     * Record expectations using the any fields for parameter of type object.
     */
    @Test
    public void recordExpectationsUsingTheAnyFieldsForParameterOfTypeObject() {
        new Expectations() {
            {
                mock.useObject(anyString);
                result = "String";
                mock.useObject(anyInt);
                result = "int";
                mock.useObject(anyByte);
                result = "byte";
                mock.useObject(anyShort);
                result = "short";
                mock.useObject(anyLong);
                result = "long";
                mock.useObject(anyBoolean);
                result = "boolean";
                mock.useObject(anyChar);
                result = "char";
                mock.useObject(anyFloat);
                result = "float";
                mock.useObject(anyDouble);
                result = "double";
                mock.useObject(any);
                result = "Object";
            }
        };

        assertInvocationsWithArgumentsOfDifferentTypesToMethodAcceptingAnyObject();
    }

    /**
     * Assert invocations with arguments of different types to method accepting any object.
     */
    void assertInvocationsWithArgumentsOfDifferentTypesToMethodAcceptingAnyObject() {
        assertEquals("String", mock.useObject("test"));
        assertEquals("String", mock.useObject(null)); // uses the first recorded expectation, since they all match null
        assertEquals("int", mock.useObject(2));
        assertEquals("Object", mock.useObject(new Object()));
        assertEquals("byte", mock.useObject((byte) -3));
        assertEquals("short", mock.useObject((short) 4));
        assertEquals("long", mock.useObject(-5L));
        assertEquals("boolean", mock.useObject(true));
        assertEquals("boolean", mock.useObject(false));
        assertEquals("char", mock.useObject('A'));
        assertEquals("float", mock.useObject(-1.5F));
        assertEquals("double", mock.useObject(23.456));
    }

    /**
     * Record expectations using the with any method for parameter of type object.
     */
    @Test
    public void recordExpectationsUsingTheWithAnyMethodForParameterOfTypeObject() {
        new Expectations() {
            {
                mock.useObject(withAny("a"));
                result = "String";
                mock.useObject(withAny(2));
                result = "int";
                mock.useObject(withAny((byte) 3));
                result = "byte";
                mock.useObject(withAny((short) 4));
                result = "short";
                mock.useObject(withAny(5L));
                result = "long";
                mock.useObject(withAny(true));
                result = "boolean";
                mock.useObject(withAny('\0'));
                result = "char";
                mock.useObject(withAny(0.41F));
                result = "float";
                mock.useObject(withAny(0.41));
                result = "double";
                mock.useObject(withAny(new Object()));
                result = "Object";
            }
        };

        assertInvocationsWithArgumentsOfDifferentTypesToMethodAcceptingAnyObject();
    }

    /**
     * Declare field in expectation block with name having same prefix as argument matching field.
     */
    @Test
    public void declareFieldInExpectationBlockWithNameHavingSamePrefixAsArgumentMatchingField() {
        new Expectations() {
            final Integer anyValue = 1;

            {
                mock.setValue(anyValue);
            }
        };

        mock.setValue(1);
    }

    /**
     * Declare method in expectation block with name having same prefix as argument matching method.
     */
    @Test
    public void declareMethodInExpectationBlockWithNameHavingSamePrefixAsArgumentMatchingMethod() {
        final List<Integer> values = new ArrayList<>();

        new Expectations() {
            {
                mock.setValues(withEqual('c'), anyBoolean);
                mock.setValue(withCapture(values));
            }

            char withEqual(char c) {
                return c;
            }
        };

        mock.setValues('c', true);
        final Collaborator col = new Collaborator();
        col.setValue(1);

        assertEquals(singletonList(1), values);

        new Verifications() {
            {
                int i;
                mock.setValue(i = withCapture());
                assertEquals(1, i);

                List<Collaborator> collaborators = withCapture(new Collaborator());
                assertSame(col, collaborators.get(0));
            }
        };
    }

    // "Missing invocations"
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Expect invocation with same mock instance but replay with null.
     *
     * @param cert
     *            the cert
     */
    @Test
    public void expectInvocationWithSameMockInstanceButReplayWithNull(
            // This class defines an abstract "toString" override, which initially was erroneously
            // mocked, causing a new expectation to be created during replay:
            @Mocked final Certificate cert) {
        new Expectations() {
            {
                mock.setValue(withSameInstance(cert));
                times = 1;
            }
        };

        mock.setValue((Certificate) null);

        thrown.expect(MissingInvocation.class);
    }

    /**
     * Expect invocation with matcher which invokes mocked method.
     */
    @Test
    public void expectInvocationWithMatcherWhichInvokesMockedMethod() {
        new Expectations() {
            {
                mock.setValue(with(new Delegate<Integer>() {
                    @Mock
                    boolean validateAsPositive(int value) {
                        // Invoking mocked method caused ConcurrentModificationException (bug fixed):
                        mock.simpleOperation(1, "b", null);
                        return value > 0;
                    }
                }));
            }
        };

        mock.setValue(-3);

        thrown.expect(MissingInvocation.class);
    }

    // Verifications
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The Class ReusableMatcher.
     */
    static class ReusableMatcher implements Delegate<Integer> {

        /**
         * Checks if is positive.
         *
         * @param i
         *            the i
         *
         * @return true, if is positive
         */
        @Mock
        final boolean isPositive(int i) {
            return i > 0;
        }
    }

    /**
     * Extending A reusable argument matcher.
     */
    @Test
    public void extendingAReusableArgumentMatcher() {
        mock.setValue(5);
        mock.setValue(123);

        new Verifications() {
            {
                mock.setValue(with(new ReusableMatcher() {
                }));
                times = 2;
            }
        };
    }
}
