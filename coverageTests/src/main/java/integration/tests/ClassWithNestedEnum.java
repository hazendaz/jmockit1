package integration.tests;

/**
 * The Class ClassWithNestedEnum.
 */
final class ClassWithNestedEnum {

    /**
     * The Enum NestedEnum.
     */
    enum NestedEnum {

        /** The elem. */
        ELEM
    }

    /**
     * The Class NestedClass.
     */
    static final class NestedClass {

        /**
         * Use enum from outer class.
         */
        static void useEnumFromOuterClass() {
            NestedEnum.values();
        }

        /**
         * Gets the outer.
         *
         * @return the outer
         */
        @SuppressWarnings("unused")
        ClassWithNestedEnum getOuter() {
            return null;
        }
    }
}
