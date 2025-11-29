/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static java.util.Arrays.asList;

import static mockit.internal.util.Utilities.JAVA8;
import static org.junit.Assume.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import mockit.integration.junit5.ExpectedException;
import mockit.integration.junit5.JMockitExtension;
import mockit.internal.expectations.invocation.MissingInvocation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class ExpectationsUsingReturnTypeConversionTest.
 */
@ExtendWith(JMockitExtension.class)
class ExpectationsUsingReturnTypeConversionTest {

    /**
     * The Class Collaborator.
     */
    static class Collaborator {

        /**
         * Gets the int.
         *
         * @return the int
         */
        int getInt() {
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
         * Gets the short.
         *
         * @return the short
         */
        short getShort() {
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
         * Gets the long.
         *
         * @return the long
         */
        long getLong() {
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
         * Gets the byte.
         *
         * @return the byte
         */
        byte getByte() {
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
         * Gets the float.
         *
         * @return the float
         */
        float getFloat() {
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
         * Gets the double.
         *
         * @return the double
         */
        double getDouble() {
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
         * Gets the char.
         *
         * @return the char
         */
        char getChar() {
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
         * Gets the boolean.
         *
         * @return the boolean
         */
        boolean getBoolean() {
            return true;
        }

        /**
         * Gets the boolean wrapper.
         *
         * @return the boolean wrapper
         */
        Boolean getBooleanWrapper() {
            return Boolean.TRUE;
        }

        /**
         * Gets the string builder.
         *
         * @return the string builder
         */
        StringBuilder getStringBuilder() {
            return null;
        }

        /**
         * Gets the input stream.
         *
         * @return the input stream
         */
        InputStream getInputStream() {
            return null;
        }

        /**
         * Gets the byte array input stream.
         *
         * @return the byte array input stream
         */
        ByteArrayInputStream getByteArrayInputStream() {
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
         * Gets the reader.
         *
         * @return the reader
         */
        Reader getReader() {
            return null;
        }

        /**
         * Gets the string reader.
         *
         * @return the string reader
         */
        StringReader getStringReader() {
            return null;
        }

        /**
         * Gets the big decimal.
         *
         * @return the big decimal
         */
        BigDecimal getBigDecimal() {
            return null;
        }

        /**
         * Gets the big integer.
         *
         * @return the big integer
         */
        BigInteger getBigInteger() {
            return null;
        }

        /**
         * Gets the atomic integer.
         *
         * @return the atomic integer
         */
        AtomicInteger getAtomicInteger() {
            return null;
        }

        /**
         * Gets the atomic long.
         *
         * @return the atomic long
         */
        AtomicLong getAtomicLong() {
            return null;
        }
    }

    /** The mock. */
    @Mocked
    Collaborator mock;

    /**
     * Attempt to return value not compatible with primitive return type.
     */
    @Test
    @ExpectedException(value = MissingInvocation.class)
    void attemptToReturnValueNotCompatibleWithPrimitiveReturnType() {
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Expectations() {
                {
                    mock.getInt();
                    result = "test";
                }
            };
        });
        Assertions.assertTrue(exception.getMessage().contains("String"));
        Assertions.assertTrue(exception.getMessage().contains("int"));
    }

    /**
     * Attempt to return value not compatible with primitive wrapper return type.
     */
    @Test
    @ExpectedException(value = MissingInvocation.class)
    void attemptToReturnValueNotCompatibleWithPrimitiveWrapperReturnType() {
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Expectations() {
                {
                    mock.getFloatWrapper();
                    result = true;
                }
            };
        });
        Assertions.assertTrue(exception.getMessage().contains("Boolean"));
        Assertions.assertTrue(exception.getMessage().contains("Float"));
    }

    /**
     * Attempt to return value not compatible with boolean return type.
     */
    @Test
    @ExpectedException(MissingInvocation.class)
    void attemptToReturnValueNotCompatibleWithBooleanReturnType() {
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Expectations() {
                {
                    mock.getBoolean();
                    result = 123;
                }
            };
        });
        Assertions.assertTrue(exception.getMessage().contains("Integer"));
        Assertions.assertTrue(exception.getMessage().contains("boolean"));
    }

    /**
     * Attempt to return value not compatible with boolean wrapper return type.
     */
    @Test
    @ExpectedException(MissingInvocation.class)
    void attemptToReturnValueNotCompatibleWithBooleanWrapperReturnType() {
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Expectations() {
                {
                    mock.getBooleanWrapper();
                    result = 'a';
                }
            };
        });
        Assertions.assertTrue(exception.getMessage().contains("Character"));
        Assertions.assertTrue(exception.getMessage().contains("Boolean"));
    }

    /**
     * Attempt to return value of reference type not assignable to return type.
     */
    @Test
    @ExpectedException(MissingInvocation.class)
    void attemptToReturnValueOfReferenceTypeNotAssignableToReturnType() {
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new Expectations() {
                {
                    mock.getInputStream();
                    result = mock;
                }
            };
        });
        Assertions.assertTrue(exception.getMessage().contains("Collaborator"));
        Assertions.assertTrue(exception.getMessage().contains("InputStream"));
    }

    /**
     * Convert number value to wider numerical return type.
     */
    @Test
    void convertNumberValueToWiderNumericalReturnType() {
        new Expectations() {
            {
                mock.getShort();
                result = (byte) 51;
                mock.getShortWrapper();
                result = 'z';
                mock.getInt();
                result = 'A';
                mock.getInteger();
                result = (byte) 123;
                mock.getLong();
                result = 52;
                result = 'b';
                mock.getLongWrapper();
                result = (short) -50;
                mock.getFloat();
                result = (short) 1234;
                result = 'a';
                mock.getFloatWrapper();
                result = (byte) -123;
                mock.getDouble();
                result = 54321;
                result = 'á';
                mock.getDoubleWrapper();
                result = (short) -10203;
                mock.getChar();
                result = (byte) 66;
                mock.getCharacter();
                result = (byte) 48;
            }
        };

        assertEquals(51, mock.getShort());
        assertEquals('z', mock.getShortWrapper().shortValue());
        assertEquals(65, mock.getInt());
        assertEquals(123, mock.getInteger().intValue());
        assertEquals(52, mock.getLong());
        assertEquals('b', mock.getLong());
        assertEquals(-50, mock.getLongWrapper().longValue());
        assertEquals(1234.0F, mock.getFloat(), 0);
        assertEquals('a', mock.getFloat(), 0);
        assertEquals(-123.0F, mock.getFloatWrapper(), 0);
        assertEquals(54321.0, mock.getDouble(), 0);
        assertEquals('á', mock.getDouble(), 0);
        assertEquals(-10203.0, mock.getDoubleWrapper(), 0);
        assertEquals('B', mock.getChar());
        assertEquals('0', mock.getCharacter().charValue());
    }

    /**
     * Convert number value to narrower numerical return type when the actual value fits the return type.
     */
    @Test
    void convertNumberValueToNarrowerNumericalReturnTypeWhenTheActualValueFitsTheReturnType() {
        new Expectations() {
            {
                mock.getByte();
                result = 23;
                result = 'C';
                mock.getByteWrapper();
                result = (short) 127;
                mock.getShort();
                result = 51;
                mock.getShortWrapper();
                result = -50L;
                mock.getInt();
                result = 123L;
                mock.getInteger();
                result = 12345L;
                mock.getFloat();
                result = 1234.0;
                mock.getFloatWrapper();
                result = -123.45;
                mock.getChar();
                result = 66;
                mock.getCharacter();
                result = 48L;
            }
        };

        assertEquals(23, mock.getByte());
        assertEquals('C', mock.getByte());
        assertEquals(127, mock.getByteWrapper().byteValue());
        assertEquals(51, mock.getShort());
        assertEquals(-50, mock.getShortWrapper().shortValue());
        assertEquals(123, mock.getInt());
        assertEquals(12345, mock.getInteger().intValue());
        assertEquals(1234.0F, mock.getFloat(), 0);
        assertEquals(-123.45F, mock.getFloatWrapper(), 0);
        assertEquals('B', mock.getChar());
        assertEquals('0', mock.getCharacter().charValue());
    }

    /**
     * Convert number value to narrower numerical return type when the actual value does not fit the return type.
     */
    @Test
    @SuppressWarnings({ "NumericCastThatLosesPrecision", "CharUsedInArithmeticContext" })
    void convertNumberValueToNarrowerNumericalReturnTypeWhenTheActualValueDoesNotFitTheReturnType() {
        new Expectations() {
            {
                mock.getByte();
                result = 230;
                mock.getByteWrapper();
                result = 'ç';
                mock.getShort();
                result = 51000;
                mock.getShortWrapper();
                result = -5000000000L;
                mock.getInt();
                result = -12300000000L;
                mock.getInteger();
                result = 12345678901L;
                mock.getFloat();
                result = 1234543212345.678901234567890;
                mock.getFloatWrapper();
                result = -1234567890.9876543210;
                mock.getChar();
                result = 66000000;
                mock.getCharacter();
                result = Character.MAX_VALUE + 1;
            }
        };

        assertEquals((byte) 230, mock.getByte());
        assertEquals((byte) 'ç', mock.getByteWrapper().byteValue());
        assertEquals((short) 51000, mock.getShort());
        assertEquals((short) -5000000000L, mock.getShortWrapper().shortValue());
        assertEquals((int) -12300000000L, mock.getInt());
        assertEquals((int) 12345678901L, mock.getInteger().intValue());
        assertEquals(1234543212345.67890123F, mock.getFloat(), 0);
        assertEquals(-1234567890.9876543210F, mock.getFloatWrapper(), 0);
        assertEquals((char) 66000000, mock.getChar());
        assertEquals(0, mock.getCharacter().charValue());
    }

    /**
     * Convert recorded textual result for methods with eligible return types.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void convertRecordedTextualResultForMethodsWithEligibleReturnTypes() throws Exception {
        assertNull(mock.getStringBuilder());

        final String text = "Some textual value";

        new Expectations() {
            {
                mock.getStringBuilder();
                result = text;
                mock.getInputStream();
                result = text;
                mock.getByteArrayInputStream();
                result = text;
                mock.getByteArray();
                result = text;
                mock.getReader();
                result = text;
                mock.getStringReader();
                result = text;
            }
        };

        assertEquals(text, mock.getStringBuilder().toString());

        byte[] buf = new byte[text.getBytes().length];
        mock.getInputStream().read(buf);
        assertArrayEquals(text.getBytes(), buf);

        mock.getByteArrayInputStream().read(buf);
        assertArrayEquals(text.getBytes(), buf);

        byte[] bytes = mock.getByteArray();
        assertArrayEquals(text.getBytes(), bytes);

        char[] cbuf = new char[text.length()];
        mock.getReader().read(cbuf);
        assertArrayEquals(text.toCharArray(), cbuf);

        mock.getStringReader().read(cbuf);
        assertArrayEquals(text.toCharArray(), cbuf);
    }

    /**
     * Convert textual and numerical results to number subtypes.
     */
    @Test
    void convertTextualAndNumericalResultsToNumberSubtypes() {
        assertNull(mock.getBigDecimal());
        assertNull(mock.getBigInteger());
        assertNull(mock.getAtomicInteger());
        assertNull(mock.getAtomicLong());

        new Expectations() {
            {
                mock.getBigDecimal();
                result = "1.50";
                result = 123;
                result = 56L;
                result = -4.125;
                mock.getBigInteger();
                result = "123";
                result = 567L;
                mock.getAtomicInteger();
                result = 1234;
                mock.getAtomicLong();
                result = 12345L;
            }
        };

        assertEquals(new BigDecimal("1.50"), mock.getBigDecimal());
        assertEquals(new BigDecimal("123"), mock.getBigDecimal());
        assertEquals(new BigDecimal("56"), mock.getBigDecimal());
        assertEquals(new BigDecimal("-4.125"), mock.getBigDecimal());
        assertEquals(new BigInteger("123"), mock.getBigInteger());
        assertEquals(BigInteger.valueOf(567L), mock.getBigInteger());
        assertEquals(1234, mock.getAtomicInteger().intValue());
        assertEquals(12345L, mock.getAtomicLong().longValue());
    }

    /**
     * The Class Java8Collaborator.
     */
    static class Java8Collaborator {

        /**
         * Gets the optional value.
         *
         * @return the optional value
         */
        Optional<String> getOptionalValue() {
            return Optional.empty();
        }

        /**
         * Gets the stream.
         *
         * @return the stream
         */
        Stream<String> getStream() {
            return null;
        }
    }

    /**
     * Convert value to optional of value.
     *
     * @param mock2
     *            the mock 2
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void convertValueToOptionalOfValue(@Mocked final Java8Collaborator mock2) throws Exception {
        assumeTrue(JAVA8);
        new Expectations() {
            {
                mock2.getOptionalValue();
                result = "Test";
            }
        };

        Optional<String> value = mock2.getOptionalValue();

        assertEquals("Test", value.orElseThrow(Exception::new));
    }

    /**
     * Convert single value to stream.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void convertSingleValueToStream(@Mocked final Java8Collaborator mock2) {
        assumeTrue(JAVA8);
        new Expectations() {
            {
                mock2.getStream();
                result = "Test";
            }
        };

        Iterator<String> values = mock2.getStream().iterator();

        assertEquals("Test", values.next());
        assertFalse(values.hasNext());
    }

    /**
     * Convert collection to stream.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void convertCollectionToStream(@Mocked final Java8Collaborator mock2) {
        assumeTrue(JAVA8);
        new Expectations() {
            {
                mock2.getStream();
                result = asList("Test", " abc ");
            }
        };

        Stream<String> values = mock2.getStream();

        assertEquals(2, values.count());
    }

    /**
     * Convert array to stream.
     *
     * @param mock2
     *            the mock 2
     */
    @Test
    void convertArrayToStream(@Mocked final Java8Collaborator mock2) {
        assumeTrue(JAVA8);
        final String[] values = { "Test", " abc " };
        new Expectations() {
            {
                mock2.getStream();
                result = values;
            }
        };

        Object[] resultingValues = mock2.getStream().toArray();

        assertArrayEquals(values, resultingValues);
    }
}
