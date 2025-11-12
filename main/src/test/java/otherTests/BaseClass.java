/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package otherTests;

import java.util.Set;

/**
 * The Class BaseClass.
 */
public class BaseClass {

    /** The base int. */
    protected int baseInt;

    /** The base string. */
    protected String baseString;

    /** The base set. */
    protected Set<Boolean> baseSet;

    /** The long field. */
    @SuppressWarnings({ "FieldCanBeLocal", "unused" })
    private long longField;

    /**
     * Sets the long field.
     *
     * @param value
     *            the new long field
     */
    void setLongField(long value) {
        longField = value;
    }
}
