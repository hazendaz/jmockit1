/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertSame;

import javax.swing.JFormattedTextField;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class CovariantReturnTypesTest.
 */
@ExtendWith(JMockitExtension.class)
class CovariantReturnTypesTest {

    /**
     * The Class SuperClass.
     */
    public static class SuperClass {
        /**
         * Gets the text field.
         *
         * @return the text field
         */
        public JTextField getTextField() {
            return null;
        }
    }

    /**
     * The Class SubClass.
     */
    public static final class SubClass extends SuperClass {
        @Override
        public JPasswordField getTextField() {
            return null;
        }
    }

    /**
     * Method in class hierarchy using recorded expectation.
     *
     * @param mock
     *            the mock
     */
    @Test
    void methodInClassHierarchyUsingRecordedExpectation(@Mocked final SubClass mock) {
        final JPasswordField passwordField = new JPasswordField();

        new Expectations() {
            {
                mock.getTextField();
                result = passwordField;
            }
        };

        SubClass subClassInstance = new SubClass();
        assertSame(passwordField, subClassInstance.getTextField());
        assertSame(passwordField, ((SuperClass) subClassInstance).getTextField());
    }

    /**
     * The Class AbstractBaseClass.
     */
    public abstract static class AbstractBaseClass {

        /**
         * Instantiates a new abstract base class.
         */
        protected AbstractBaseClass() {
        }

        /**
         * Gets the text field.
         *
         * @return the text field
         */
        public abstract JTextField getTextField();
    }

    /**
     * The Class ConcreteClass.
     */
    public static class ConcreteClass extends AbstractBaseClass {
        @Override
        public JFormattedTextField getTextField() {
            return null;
        }
    }

    /**
     * Concrete method implementation using recorded expectation.
     *
     * @param mock
     *            the mock
     */
    @Test
    void concreteMethodImplementationUsingRecordedExpectation(@Mocked final ConcreteClass mock) {
        final JTextField formattedField1 = new JFormattedTextField();
        final JTextField formattedField2 = new JFormattedTextField();

        new Expectations() {
            {
                mock.getTextField();
                returns(formattedField1, formattedField2);
            }
        };

        assertSame(formattedField1, mock.getTextField());
        assertSame(formattedField2, ((AbstractBaseClass) mock).getTextField());
    }

    /**
     * Abstract method implementation using recorded expectation.
     *
     * @param mock
     *            the mock
     */
    @Test
    void abstractMethodImplementationUsingRecordedExpectation(@Capturing final AbstractBaseClass mock) {
        final JTextField regularField = new JTextField();
        final JTextField formattedField = new JFormattedTextField();

        new Expectations() {
            {
                mock.getTextField();
                result = new JTextField[] { regularField, formattedField };
            }
        };

        AbstractBaseClass firstInstance = new AbstractBaseClass() {
            @Override
            public JTextField getTextField() {
                return null;
            }
        };
        assertSame(regularField, firstInstance.getTextField());

        assertSame(formattedField, firstInstance.getTextField());
    }

    /**
     * The Interface SuperInterface.
     */
    public interface SuperInterface {
        /**
         * Gets the value.
         *
         * @return the value
         */
        Object getValue();
    }

    /**
     * The Interface SubInterface.
     */
    public interface SubInterface extends SuperInterface {
        /**
         * Gets the value.
         *
         * @return the value
         */
        @Override
        String getValue();
    }

    /**
     * Method in super interface with varying return values using recorded expectation.
     *
     * @param mock
     *            the mock
     */
    @Test
    void methodInSuperInterfaceWithVaryingReturnValuesUsingRecordedExpectation(@Mocked final SuperInterface mock) {
        final Object value = new Object();
        final String specificValue = "test";

        new Expectations() {
            {
                mock.getValue();
                result = value;
                result = specificValue;
            }
        };

        assertSame(value, mock.getValue());
        assertSame(specificValue, mock.getValue());
    }

    /**
     * Method in sub interface using recorded expectations.
     *
     * @param mock
     *            the mock
     */
    @Test
    void methodInSubInterfaceUsingRecordedExpectations(@Mocked final SubInterface mock) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        final SuperInterface base = mock;
        final Object value = new Object();
        final String specificValue1 = "test1";
        final String specificValue2 = "test2";

        new Expectations() {
            {
                base.getValue();
                returns(specificValue1, value);

                mock.getValue();
                result = specificValue2;
            }
        };

        assertSame(specificValue1, base.getValue());
        assertSame(value, base.getValue());

        assertSame(specificValue2, mock.getValue());
    }

    /**
     * Method in sub interface replayed through super interface using recorded expectation.
     *
     * @param mock
     *            the mock
     */
    @Test
    void methodInSubInterfaceReplayedThroughSuperInterfaceUsingRecordedExpectation(@Mocked final SubInterface mock) {
        final String specificValue = "test";

        new Expectations() {
            {
                mock.getValue();
                result = specificValue;
            }
        };

        assertSame(specificValue, ((SuperInterface) mock).getValue());
    }
}
