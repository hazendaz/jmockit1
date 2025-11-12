/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

/**
 * The Class ClassWithObjectOverrides.
 */
public final class ClassWithObjectOverrides implements Cloneable {

    /** The text. */
    private final StringBuilder text;

    /**
     * Instantiates a new class with object overrides.
     *
     * @param text
     *            the text
     */
    public ClassWithObjectOverrides(String text) {
        this.text = new StringBuilder(text);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ClassWithObjectOverrides && text.equals(((ClassWithObjectOverrides) o).text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    @Override
    public String toString() {
        return text.toString();
    }

    @SuppressWarnings("FinalizeDeclaration")
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        text.setLength(0);
    }

    @Override
    public ClassWithObjectOverrides clone() {
        ClassWithObjectOverrides theClone = null;
        try {
            theClone = (ClassWithObjectOverrides) super.clone();
        } catch (CloneNotSupportedException ignore) {
        }
        return theClone;
    }

    /**
     * Gets the int value.
     *
     * @return the int value
     */
    int getIntValue() {
        return -1;
    }

    /**
     * Do something.
     */
    void doSomething() {
        throw new RuntimeException();
    }

    /**
     * Do something.
     *
     * @param arg
     *            the arg
     *
     * @return the int
     */
    int doSomething(Object arg) {
        return arg.hashCode();
    }
}
