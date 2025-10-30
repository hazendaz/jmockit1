package integration.tests;

/**
 * The Class AbstractClassWithNoExecutableLines.
 */
abstract class AbstractClassWithNoExecutableLines {

    /** The an int field. */
    protected int anIntField;

    /**
     * Do something.
     *
     * @param s
     *            the s
     * @param b
     *            the b
     */
    abstract void doSomething(String s, boolean b);

    /**
     * Return value.
     *
     * @return the int
     */
    abstract int returnValue();
}
