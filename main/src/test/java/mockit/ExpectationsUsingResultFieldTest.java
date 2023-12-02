package mockit;

import static java.util.Arrays.asList;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * The Class ExpectationsUsingResultFieldTest.
 */
public final class ExpectationsUsingResultFieldTest {

    /** The thrown. */
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    /**
     * The Class Collaborator.
     */
    static class Collaborator {

        /**
         * Do internal.
         *
         * @return the string
         */
        static String doInternal() {
            return "123";
        }

        /**
         * Provide some service.
         */
        void provideSomeService() {
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        int getValue() {
            return -1;
        }

        /**
         * Gets the integer.
         *
         * @return the integer
         */
        Integer getInteger() {
            return -1;
        }

        /**
         * Gets the byte value.
         *
         * @return the byte value
         */
        byte getByteValue() {
            return -1;
        }

        /**
         * Gets the byte wrapper.
         *
         * @return the byte wrapper
         */
        Byte getByteWrapper() {
            return -1;
        }

        /**
         * Gets the short value.
         *
         * @return the short value
         */
        short getShortValue() {
            return -1;
        }

        /**
         * Gets the short wrapper.
         *
         * @return the short wrapper
         */
        Short getShortWrapper() {
            return -1;
        }

        /**
         * Gets the long value.
         *
         * @return the long value
         */
        long getLongValue() {
            return -1;
        }

        /**
         * Gets the long wrapper.
         *
         * @return the long wrapper
         */
        Long getLongWrapper() {
            return -1L;
        }

        /**
         * Gets the float value.
         *
         * @return the float value
         */
        float getFloatValue() {
            return -1.0F;
        }

        /**
         * Gets the float wrapper.
         *
         * @return the float wrapper
         */
        Float getFloatWrapper() {
            return -1.0F;
        }

        /**
         * Gets the double value.
         *
         * @return the double value
         */
        double getDoubleValue() {
            return -1.0;
        }

        /**
         * Gets the double wrapper.
         *
         * @return the double wrapper
         */
        Double getDoubleWrapper() {
            return -1.0;
        }

        /**
         * Gets the char value.
         *
         * @return the char value
         */
        char getCharValue() {
            return '1';
        }

        /**
         * Gets the character.
         *
         * @return the character
         */
        Character getCharacter() {
            return '1';
        }

        /**
         * Gets the boolean value.
         *
         * @return the boolean value
         */
        boolean getBooleanValue() {
            return true;
        }

        /**
         * Gets the boolean wrapper.
         *
         * @return the boolean wrapper
         */
        Boolean getBooleanWrapper() {
            return true;
        }

        /**
         * Gets the string.
         *
         * @return the string
         */
        String getString() {
            return "";
        }

        /**
         * Gets the object.
         *
         * @return the object
         */
        Object getObject() {
            return null;
        }

        /**
         * Gets the items.
         *
         * @return the items
         */
        Collection<?> getItems() {
            return null;
        }

        /**
         * Gets the list items.
         *
         * @return the list items
         */
        List<?> getListItems() {
            return null;
        }

        /**
         * Gets the sets the items.
         *
         * @return the sets the items
         */
        Set<?> getSetItems() {
            return null;
        }

        /**
         * Gets the sorted set items.
         *
         * @return the sorted set items
         */
        SortedSet<?> getSortedSetItems() {
            return null;
        }

        /**
         * Gets the map items.
         *
         * @return the map items
         */
        Map<?, ?> getMapItems() {
            return null;
        }

        /**
         * Gets the sorted map items.
         *
         * @return the sorted map items
         */
        SortedMap<?, ?> getSortedMapItems() {
            return null;
        }

        /**
         * Gets the iterator.
         *
         * @return the iterator
         */
        Iterator<?> getIterator() {
            return null;
        }

        /**
         * Gets the list iterator.
         *
         * @return the list iterator
         */
        ListIterator<?> getListIterator() {
            return null;
        }

        /**
         * Gets the iterable.
         *
         * @return the iterable
         */
        Iterable<?> getIterable() {
            return null;
        }

        /**
         * Gets the int array.
         *
         * @return the int array
         */
        int[] getIntArray() {
            return null;
        }

        /**
         * Gets the int 2 array.
         *
         * @return the int 2 array
         */
        int[][] getInt2Array() {
            return null;
        }

        /**
         * Gets the byte array.
         *
         * @return the byte array
         */
        byte[] getByteArray() {
            return null;
        }

        /**
         * Gets the short array.
         *
         * @return the short array
         */
        short[] getShortArray() {
            return null;
        }

        /**
         * Gets the short wrapper array.
         *
         * @return the short wrapper array
         */
        Short[] getShortWrapperArray() {
            return null;
        }

        /**
         * Gets the long array.
         *
         * @return the long array
         */
        long[] getLongArray() {
            return null;
        }

        /**
         * Gets the long 2 array.
         *
         * @return the long 2 array
         */
        long[][] getLong2Array() {
            return null;
        }

        /**
         * Gets the float array.
         *
         * @return the float array
         */
        float[] getFloatArray() {
            return null;
        }

        /**
         * Gets the double array.
         *
         * @return the double array
         */
        double[] getDoubleArray() {
            return null;
        }

        /**
         * Gets the char array.
         *
         * @return the char array
         */
        char[] getCharArray() {
            return null;
        }

        /**
         * Gets the boolean array.
         *
         * @return the boolean array
         */
        boolean[] getBooleanArray() {
            return null;
        }

        /**
         * Gets the string array.
         *
         * @return the string array
         */
        String[] getStringArray() {
            return null;
        }

        /**
         * Gets the string 2 array.
         *
         * @return the string 2 array
         */
        String[][] getString2Array() {
            return null;
        }

        /**
         * Gets the array of generic elements.
         *
         * @param <T>
         *            the generic type
         * @param i
         *            the i
         *
         * @return the array of generic elements
         */
        <T extends Number> T[] getArrayOfGenericElements(@SuppressWarnings("unused") int i) {
            return null;
        }

        /**
         * Gets the numbers.
         *
         * @return the numbers
         */
        Collection<Number> getNumbers() {
            return null;
        }

        /**
         * Gets the number list.
         *
         * @return the number list
         */
        List<Number> getNumberList() {
            return null;
        }

        /**
         * Gets the string set.
         *
         * @return the string set
         */
        Set<String> getStringSet() {
            return null;
        }

        /**
         * Gets the sorted number set.
         *
         * @return the sorted number set
         */
        SortedSet<Number> getSortedNumberSet() {
            return null;
        }

        /**
         * Gets the string iterator.
         *
         * @return the string iterator
         */
        Iterator<String> getStringIterator() {
            return null;
        }

        /**
         * Gets the float iterator.
         *
         * @return the float iterator
         */
        ListIterator<Float> getFloatIterator() {
            return null;
        }

        /**
         * Gets the number iterable.
         *
         * @return the number iterable
         */
        Iterable<Number> getNumberIterable() {
            return null;
        }

        /**
         * Gets the number queue.
         *
         * @return the number queue
         */
        Queue<Number> getNumberQueue() {
            return null;
        }
    }

    /**
     * Returns expected values.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnsExpectedValues(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getValue();
                result = 3;
                Collaborator.doInternal();
                result = "test";
            }
        };

        assertEquals(3, mock.getValue());
        assertEquals("test", Collaborator.doInternal());
    }

    /**
     * Record thrown exception.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void recordThrownException(@Mocked final Collaborator mock) {
        thrown.expect(ArithmeticException.class);

        new Expectations() {
            {
                mock.provideSomeService();
                result = new ArithmeticException("test");
            }
        };

        mock.provideSomeService();
    }

    /**
     * Record thrown error.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void recordThrownError(@Mocked final Collaborator mock) {
        thrown.expect(LinkageError.class);

        new Expectations() {
            {
                mock.provideSomeService();
                result = new LinkageError("test");
            }
        };

        mock.provideSomeService();
    }

    /**
     * Returns multiple expected values.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnsMultipleExpectedValues(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getValue();
                result = 1;
                result = 2;
                result = 3;
            }
        };

        assertEquals(1, mock.getValue());
        assertEquals(2, mock.getValue());
        assertEquals(3, mock.getValue());
    }

    /**
     * Returns multiple expected values with more invocations allowed.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnsMultipleExpectedValuesWithMoreInvocationsAllowed(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getValue();
                result = 1;
                result = 2;
                times = 3;
            }
        };

        assertEquals(1, mock.getValue());
        assertEquals(2, mock.getValue());
        assertEquals(2, mock.getValue());
    }

    /**
     * Returns null as default value for methods returning string or object.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnsNullAsDefaultValueForMethodsReturningStringOrObject(@Mocked Collaborator mock) {
        assertNull(mock.getString());
        assertNull(Collaborator.doInternal());
        assertNull(mock.getObject());
    }

    /**
     * Returns default values for primitive and wrapper return types.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnsDefaultValuesForPrimitiveAndWrapperReturnTypes(@Mocked Collaborator mock) {
        assertEquals(0, mock.getValue());
        assertEquals(0, mock.getInteger().intValue());
        assertEquals((byte) 0, mock.getByteValue());
        assertEquals(0, mock.getByteWrapper().intValue());
        assertEquals((short) 0, mock.getShortValue());
        assertEquals(0, mock.getShortWrapper().intValue());
        assertEquals(0L, mock.getLongValue());
        assertEquals(0L, mock.getLongWrapper().longValue());
        assertEquals(0.0F, mock.getFloatValue(), 0.0);
        assertEquals(0.0F, mock.getFloatWrapper(), 0);
        assertEquals(0.0, mock.getDoubleValue(), 0.0);
        assertEquals(0.0, mock.getDoubleWrapper(), 0);
        assertEquals('\0', mock.getCharValue());
        assertEquals('\0', mock.getCharacter().charValue());
        assertFalse(mock.getBooleanValue());
        assertFalse(mock.getBooleanWrapper());
    }

    /**
     * Returns default values for collection valued return types.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnsDefaultValuesForCollectionValuedReturnTypes(@Mocked Collaborator mock) {
        List<?> emptyList = Collections.emptyList();
        assertSame(emptyList, mock.getItems());
        assertSame(emptyList, mock.getListItems());

        Set<?> emptySet = Collections.emptySet();
        assertSame(emptySet, mock.getSetItems());
        assertEquals(emptySet, mock.getSortedSetItems());

        Map<?, ?> emptyMap = Collections.emptyMap();
        assertSame(emptyMap, mock.getMapItems());
        assertEquals(emptyMap, mock.getSortedMapItems());
    }

    /**
     * Returns default values for iterator and iterable return types.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnsDefaultValuesForIteratorAndIterableReturnTypes(@Mocked Collaborator mock) {
        assertFalse(mock.getIterator().hasNext());
        assertFalse(mock.getListIterator().hasNext());
        assertFalse(mock.getIterable().iterator().hasNext());
    }

    /**
     * Returns default values for array valued return types.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnsDefaultValuesForArrayValuedReturnTypes(@Mocked Collaborator mock) {
        assertArrayEquals(new int[0], mock.getIntArray());
        assertArrayEquals(new int[0][0], mock.getInt2Array());
        assertArrayEquals(new byte[0], mock.getByteArray());
        assertArrayEquals(new short[0], mock.getShortArray());
        assertArrayEquals(new Short[0], mock.getShortWrapperArray());
        assertArrayEquals(new long[0], mock.getLongArray());
        assertArrayEquals(new long[0][0], mock.getLong2Array());
        assertArrayEquals(new float[0], mock.getFloatArray(), 0.0F);
        assertArrayEquals(new double[0], mock.getDoubleArray(), 0.0);
        assertArrayEquals(new char[0], mock.getCharArray());
        assertEquals(0, mock.getBooleanArray().length);
        assertArrayEquals(new String[0], mock.getStringArray());
        assertArrayEquals(new String[0][0], mock.getString2Array());
    }

    /**
     * Returns multiple values in sequence using collection.
     */
    @Test
    public void returnsMultipleValuesInSequenceUsingCollection() {
        final Collaborator collaborator = new Collaborator();
        final Set<Boolean> booleanSet = new LinkedHashSet<>(asList(true, false));
        final Collection<Integer> intCol = asList(1, 2, 3);
        final List<Character> charList = asList('a', 'b', 'c');

        new Expectations(collaborator) {
            {
                collaborator.getBooleanWrapper();
                result = booleanSet;
                collaborator.getInteger();
                result = intCol;
                collaborator.getCharValue();
                result = charList;
            }
        };

        assertTrue(collaborator.getBooleanWrapper());
        assertFalse(collaborator.getBooleanWrapper());

        assertEquals(1, collaborator.getInteger().intValue());
        assertEquals(2, collaborator.getInteger().intValue());
        assertEquals(3, collaborator.getInteger().intValue());

        assertEquals('a', collaborator.getCharValue());
        assertEquals('b', collaborator.getCharValue());
        assertEquals('c', collaborator.getCharValue());
    }

    /**
     * Returns multiple values in sequence using iterator.
     */
    @Test
    public void returnsMultipleValuesInSequenceUsingIterator() {
        final Collaborator collaborator = new Collaborator();
        final Collection<String> strCol = asList("ab", "cde", "Xyz");

        new Expectations(collaborator) {
            {
                collaborator.getString();
                result = strCol.iterator();
            }
        };

        assertEquals("ab", collaborator.getString());
        assertEquals("cde", collaborator.getString());
        assertEquals("Xyz", collaborator.getString());
    }

    /**
     * Returns multiple values in sequence using array.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void returnsMultipleValuesInSequenceUsingArray(@Injectable final Collaborator collaborator) {
        final boolean[] arrayOfBooleanPrimitives = { true, false };
        final Boolean[] arrayOfBooleanWrappers = { Boolean.TRUE, Boolean.FALSE };
        final int[] intArray = { 1, 2, 3 };
        final Character[] charArray = { 'a', 'b', 'c' };

        new Expectations() {
            {
                collaborator.getBooleanValue();
                result = arrayOfBooleanPrimitives;
                collaborator.getBooleanWrapper();
                result = arrayOfBooleanWrappers;
                collaborator.getValue();
                result = intArray;
                collaborator.getCharValue();
                result = charArray;
            }
        };

        assertTrue(collaborator.getBooleanValue());
        assertFalse(collaborator.getBooleanValue());

        assertTrue(collaborator.getBooleanWrapper());
        assertFalse(collaborator.getBooleanWrapper());

        assertEquals(1, collaborator.getValue());
        assertEquals(2, collaborator.getValue());
        assertEquals(3, collaborator.getValue());

        assertEquals('a', collaborator.getCharValue());
        assertEquals('b', collaborator.getCharValue());
        assertEquals('c', collaborator.getCharValue());
    }

    /**
     * Returns multiple values in sequence using iterable.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void returnsMultipleValuesInSequenceUsingIterable(@Injectable final Collaborator collaborator) {
        final Iterable<Integer> intValues = () -> asList(3, 2, 1).iterator();

        new Expectations() {
            {
                collaborator.getValue();
                result = intValues;
            }
        };

        assertEquals(3, collaborator.getValue());
        assertEquals(2, collaborator.getValue());
        assertEquals(1, collaborator.getValue());
    }

    /**
     * Returns multiple values from method with return type of object.
     *
     * @param collaborator
     *            the collaborator
     */
    @Test
    public void returnsMultipleValuesFromMethodWithReturnTypeOfObject(@Mocked final Collaborator collaborator) {
        new Expectations() {
            {
                collaborator.getObject();
                result = new int[] { 1, 2 };
                result = new Object[] { "test", 'X' };
                result = asList(5L, 67L);
                result = null;
            }
        };

        assertArrayEquals(new int[] { 1, 2 }, (int[]) collaborator.getObject());
        assertArrayEquals(new Object[] { "test", 'X' }, (Object[]) collaborator.getObject());
        assertEquals(asList(5L, 67L), collaborator.getObject());
        assertNull(collaborator.getObject());
        assertNull(collaborator.getObject());
    }

    /**
     * Returns empty array for method with return type of object.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnsEmptyArrayForMethodWithReturnTypeOfObject(@Mocked final Collaborator mock) {
        final String[] emptyArray = {};

        new Expectations() {
            {
                mock.getObject();
                result = emptyArray;
            }
        };

        assertSame(emptyArray, mock.getObject());
    }

    /**
     * Returns multiple values from generic method.
     *
     * @param callable
     *            the callable
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void returnsMultipleValuesFromGenericMethod(@Mocked final Callable<Integer> callable) throws Exception {
        new Expectations() {
            {
                callable.call();
                result = new int[] { 3, 2, 1 };
            }
        };

        Integer firstCall = callable.call();
        assertEquals(3, firstCall.intValue());
        assertEquals(2, callable.call().intValue());
        assertEquals(1, callable.call().intValue());
    }

    /**
     * Returns specified collections for methods that return collections.
     */
    @Test
    public void returnsSpecifiedCollectionsForMethodsThatReturnCollections() {
        final Collaborator collaborator = new Collaborator();
        final Collection<String> strCol = asList("ab", "cde");
        final List<Byte> byteList = asList((byte) 5, (byte) 68);
        final Set<Character> charSet = new HashSet<>(asList('g', 't', 'x'));
        final SortedSet<String> sortedSet = new TreeSet<>(asList("hpq", "Abc"));

        new Expectations(collaborator) {
            {
                collaborator.getItems();
                result = strCol;
                collaborator.getListItems();
                result = byteList;
                collaborator.getSetItems();
                result = charSet;
                collaborator.getSortedSetItems();
                result = sortedSet;
            }
        };

        assertSame(strCol, collaborator.getItems());
        assertSame(byteList, collaborator.getListItems());
        assertSame(charSet, collaborator.getSetItems());
        assertSame(sortedSet, collaborator.getSortedSetItems());
    }

    /**
     * Returns specified iterator for method that returns iterator.
     */
    @Test
    public void returnsSpecifiedIteratorForMethodThatReturnsIterator() {
        final Collaborator collaborator = new Collaborator();
        final Iterator<String> itr = asList("ab", "cde").iterator();

        new Expectations(collaborator) {
            {
                collaborator.getIterator();
                result = itr;
            }
        };

        assertSame(itr, collaborator.getIterator());
    }

    /**
     * Returns value of incompatible type for method returning array.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnsValueOfIncompatibleTypeForMethodReturningArray(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getBooleanArray();
                result = new HashSet<Object>();
                mock.getStringArray();
                result = Collections.emptyList();
                mock.getIntArray();
                result = new short[] { 1, 2 };
            }
        };

        try {
            mock.getBooleanArray();
            fail();
        } catch (ClassCastException ignore) {
        }
        try {
            mock.getStringArray();
            fail();
        } catch (ClassCastException ignore) {
        }
        try {
            mock.getIntArray();
            fail();
        } catch (ClassCastException ignore) {
        }
    }

    /**
     * Returns value of incompatible type for method returning collection.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnsValueOfIncompatibleTypeForMethodReturningCollection(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getListItems();
                result = Collections.emptySet();
                mock.getSetItems();
                result = new ArrayList<Object>();
            }
        };

        try {
            mock.getListItems();
            fail();
        } catch (ClassCastException ignore) {
        }
        try {
            mock.getSetItems();
            fail();
        } catch (ClassCastException ignore) {
        }
    }

    /**
     * Attempt to return value of type set from method returning iterator.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void attemptToReturnValueOfTypeSetFromMethodReturningIterator(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getIterator();
                result = Collections.emptySet();
            }
        };

        thrown.expect(ClassCastException.class);

        mock.getIterator();
    }

    /**
     * Attempt to return value of type list from method returning iterator.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void attemptToReturnValueOfTypeListFromMethodReturningIterator(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getIterator();
                result = asList("a", true, 123);
            }
        };

        thrown.expect(ClassCastException.class);

        mock.getIterator();
    }

    /**
     * Return iterable or iterator from recorded array.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void returnIterableOrIteratorFromRecordedArray(@Injectable final Collaborator mock) {
        final String[] items = { "Abc", "test" };
        final int[] listItems = { 1, 2, 3 };
        final boolean[] iterable = { false, true };
        final Boolean[] iterator = { true, false, true };
        final Object[] listIterator = { "test", 123, true };

        new Expectations() {
            {
                mock.getItems();
                result = items;
                mock.getListItems();
                result = listItems;
                mock.getSetItems();
                result = new char[] { 'A', 'c', 'b', 'A' };
                mock.getSortedSetItems();
                result = new Object[] { "test", "123", "abc" };
                mock.getIterable();
                result = iterable;
                mock.getIterator();
                result = iterator;
                mock.getListIterator();
                result = listIterator;
            }
        };

        assertEquals(Arrays.toString(items), mock.getItems().toString());
        assertEquals(Arrays.toString(listItems), mock.getListItems().toString());
        assertEquals("[A, c, b]", mock.getSetItems().toString());
        assertEquals("[123, abc, test]", mock.getSortedSetItems().toString());
        assertEquals(Arrays.toString(iterable), mock.getIterable().toString());
        assertEquals(asList(iterator), fromIterator(mock.getIterator()));
        assertEquals(asList(listIterator), fromIterator(mock.getListIterator()));
    }

    /**
     * From iterator.
     *
     * @param itr
     *            the itr
     *
     * @return the list
     */
    private List<?> fromIterator(Iterator<?> itr) {
        List<Object> values = new ArrayList<>();

        while (itr.hasNext()) {
            values.add(itr.next());
        }

        return values;
    }

    /**
     * Return map from recorded two dimensional array.
     *
     * @param mock1
     *            the mock 1
     * @param mock2
     *            the mock 2
     */
    @Test
    public void returnMapFromRecordedTwoDimensionalArray(@Injectable final Collaborator mock1,
            @Injectable final Collaborator mock2) {
        final int[][] sortedItems1 = { { 13, 1 }, { 2, 2 }, { 31, 3 }, { 5, 4 } };
        final Object[][] items2 = { { 1, "first" }, { 2 }, { 3, true } };

        new Expectations() {
            {
                mock1.getMapItems();
                result = new String[][] { { "Abc", "first" }, { "test", "Second" }, { "Xyz", null } };
                mock1.getSortedMapItems();
                result = sortedItems1;
                mock2.getMapItems();
                result = items2;
            }
        };

        assertEquals("{Abc=first, test=Second, Xyz=null}", mock1.getMapItems().toString());
        assertEquals("{2=2, 5=4, 13=1, 31=3}", mock1.getSortedMapItems().toString());
        assertEquals("{1=first, 2=null, 3=true}", mock2.getMapItems().toString());
    }

    /**
     * Record null return value for constructor.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void recordNullReturnValueForConstructor(@Mocked Collaborator mock) {
        new Expectations() {
            {
                new Collaborator();
                result = null;
            }
        };

        new Collaborator().provideSomeService();
    }

    /**
     * Record null return value for void method.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void recordNullReturnValueForVoidMethod(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                result = null;
            }
        };

        new Collaborator().provideSomeService();
    }

    /**
     * Record null return value for void method and then A thrown error.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void recordNullReturnValueForVoidMethodAndThenAThrownError(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                result = null;
                result = new UnknownError();
            }
        };

        thrown.expect(UnknownError.class);

        try {
            mock.provideSomeService();
        } catch (Throwable ignore) {
            fail();
        }

        mock.provideSomeService();
    }

    /**
     * Throw exception from second invocation of constructor.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void throwExceptionFromSecondInvocationOfConstructor(@Mocked Collaborator mock) {
        new Expectations() {
            {
                new Collaborator();
                result = null;
                result = new NoSuchElementException();
            }
        };

        thrown.expect(NoSuchElementException.class);

        try {
            new Collaborator();
        } catch (NoSuchElementException ignore) {
            fail();
        }

        new Collaborator();
    }

    /**
     * Record return value for void method.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void recordReturnValueForVoidMethod(@Mocked final Collaborator mock) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("incompatible with return type void");
        thrown.expectMessage("Integer");

        new Expectations() {
            {
                mock.provideSomeService();
                result = 123;
            }
        };
    }

    /**
     * Record consecutive return values for void method.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void recordConsecutiveReturnValuesForVoidMethod(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                result = new int[] { 123, 45 }; // will have the effect of allowing two invocations
            }
        };

        mock.provideSomeService();
        mock.provideSomeService();
    }

    /**
     * Record return value for constructor.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void recordReturnValueForConstructor(@Mocked Collaborator mock) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Invalid assignment");
        thrown.expectMessage("for constructor");

        new Expectations() {
            {
                new Collaborator();
                result = "test"; // invalid, throws IllegalArgumentException
            }
        };
    }

    /**
     * Record return values mixed with throwables for non void method.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void recordReturnValuesMixedWithThrowablesForNonVoidMethod(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getString();
                result = asList("Abc", new IllegalStateException(), "DEF", null, new UnknownError());
            }
        };

        Collaborator c = new Collaborator();
        assertEquals("Abc", c.getString());
        try {
            c.getString();
            fail();
        } catch (IllegalStateException ignored) {
        }
        assertEquals("DEF", c.getString());
        assertNull(c.getString());
        try {
            c.getString();
            fail();
        } catch (UnknownError ignored) {
        }
        try {
            c.getString();
            fail();
        } catch (UnknownError ignored) {
        }
    }

    /**
     * Record exception followed by null return value for void method.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void recordExceptionFollowedByNullReturnValueForVoidMethod(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.provideSomeService();
                result = new IllegalArgumentException();
                result = null;
            }
        };

        try {
            mock.provideSomeService();
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        mock.provideSomeService();
    }

    /**
     * Record arrays of generic element types.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void recordArraysOfGenericElementTypes(@Mocked final Collaborator mock) {
        final Integer[] integerValues = { 1, 2 };
        final Number[] numberValues = { 5L, 12.5F };
        final String[] stringValues = { "a", "b" };

        new Expectations() {
            {
                mock.getArrayOfGenericElements(1);
                result = integerValues;
                mock.getArrayOfGenericElements(2);
                result = numberValues;
                mock.getArrayOfGenericElements(3);
                result = stringValues;
            }
        };

        assertSame(numberValues, mock.getArrayOfGenericElements(2));
        assertSame(integerValues, mock.getArrayOfGenericElements(1));
        try {
            mock.getArrayOfGenericElements(3);
            fail();
        } catch (ClassCastException ignore) {
        }
    }

    /**
     * Creates the array from single recorded value of the element type.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void createArrayFromSingleRecordedValueOfTheElementType(@Mocked final Collaborator mock) {
        new Expectations() {
            {
                mock.getIntArray();
                result = 123;
                mock.getStringArray();
                result = "test";
            }
        };

        assertArrayEquals(new int[] { 123 }, mock.getIntArray());
        assertArrayEquals(new String[] { "test" }, mock.getStringArray());
    }

    /**
     * Creates the appropriate container from single recorded value of the element type.
     *
     * @param mock
     *            the mock
     */
    @Test
    public void createAppropriateContainerFromSingleRecordedValueOfTheElementType(@Mocked final Collaborator mock) {
        final Double d = 1.2;
        final Float f = 3.45F;
        final BigDecimal price = new BigDecimal("123.45");

        new Expectations() {
            {
                mock.getNumbers();
                result = 123;
                mock.getNumberList();
                result = 45L;
                mock.getStringSet();
                result = "test";
                mock.getSortedNumberSet();
                result = d;
                mock.getNumberIterable();
                result = price;
                mock.getNumberQueue();
                result = d;
                mock.getStringIterator();
                result = "Abc";
                mock.getFloatIterator();
                result = f;
            }
        };

        assertContainerWithSingleElement(mock.getNumbers(), 123);
        assertContainerWithSingleElement(mock.getNumberList(), 45L);
        assertContainerWithSingleElement(mock.getStringSet(), "test");
        assertContainerWithSingleElement(mock.getSortedNumberSet(), d);
        assertContainerWithSingleElement(mock.getNumberIterable(), price);
        assertContainerWithSingleElement(mock.getNumberQueue(), d);
        assertContainerWithSingleElement(mock.getStringIterator(), "Abc");
        assertContainerWithSingleElement(mock.getFloatIterator(), f);
    }

    /**
     * Assert container with single element.
     *
     * @param container
     *            the container
     * @param expectedElement
     *            the expected element
     */
    void assertContainerWithSingleElement(Iterable<?> container, Object expectedElement) {
        assertContainerWithSingleElement(container.iterator(), expectedElement);
    }

    /**
     * Assert container with single element.
     *
     * @param container
     *            the container
     * @param expectedElement
     *            the expected element
     */
    void assertContainerWithSingleElement(Iterator<?> container, Object expectedElement) {
        assertTrue(container.hasNext());
        assertSame(expectedElement, container.next());
        assertFalse(container.hasNext());
    }
}
