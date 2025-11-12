/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package integration.tests;

/**
 * The Class ClassLoadedByCustomLoaderOnly.
 */
public final class ClassLoadedByCustomLoaderOnly {

    /** The value. */
    private final String value;

    /**
     * Instantiates a new class loaded by custom loader only.
     *
     * @param value
     *            the value
     */
    public ClassLoadedByCustomLoaderOnly(String value) {
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }
}
