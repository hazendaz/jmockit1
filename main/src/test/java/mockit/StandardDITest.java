package mockit;

import static java.util.Collections.singletonList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import mockit.integration.junit5.JMockitExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The Class StandardDITest.
 */
@ExtendWith(JMockitExtension.class)
class StandardDITest {

    /**
     * The Class TestedClass.
     */
    public static class TestedClass {

        /** The global action. */
        @Inject
        static Runnable globalAction;

        /** The collaborator. */
        private final Collaborator collaborator;

        /** The collaborator 1. */
        @Inject
        private Collaborator collaborator1;

        /** The collaborator 2. */
        Collaborator collaborator2;

        /** The some value. */
        @Inject
        int someValue;

        /** The another value. */
        @Inject
        private int anotherValue;

        /** The non annotated field. */
        String nonAnnotatedField;

        /** The non annotated generic field. */
        Callable<String> nonAnnotatedGenericField;

        /**
         * Instantiates a new tested class.
         *
         * @param collaborator
         *            the collaborator
         */
        @Inject
        public TestedClass(Collaborator collaborator) {
            this.collaborator = collaborator;
        }

        /**
         * Instantiates a new tested class.
         *
         * @param collaborator
         *            the collaborator
         * @param anotherValue
         *            the another value
         */
        @SuppressWarnings("unused")
        public TestedClass(Collaborator collaborator, int anotherValue) {
            throw new RuntimeException("Must not occur");
        }
    }

    /**
     * The Class Collaborator.
     */
    static class Collaborator {
        /** The b. */
        boolean b = true;
    }

    /** The tested 1. */
    @Tested
    TestedClass tested1;

    /** The collaborator. */
    @Injectable
    Collaborator collaborator; // for constructor injection

    /** The collaborator 1. */
    @Injectable
    Collaborator collaborator1; // for field injection

    /** The some value. */
    @Injectable("123")
    int someValue;

    /** The another value. */
    @Injectable
    final int anotherValue = 45;

    /** The callable. */
    @Injectable
    Callable<String> callable;

    /**
     * The Class TestedClassWithNoAnnotatedConstructor.
     */
    static final class TestedClassWithNoAnnotatedConstructor {

        /** The value. */
        @Inject
        int value;

        /** The a text. */
        @Inject
        String aText;

        /** The another text. */
        String anotherText;
    }

    /** The tested 2. */
    @Tested
    TestedClassWithNoAnnotatedConstructor tested2;

    /** The a text. */
    @Injectable
    final String aText = "Abc";

    /**
     * The Class TestedClassWithInjectOnConstructorOnly.
     */
    public static class TestedClassWithInjectOnConstructorOnly {

        /** The name. */
        String name;

        /**
         * Instantiates a new tested class with inject on constructor only.
         */
        @Inject
        public TestedClassWithInjectOnConstructorOnly() {
        }
    }

    /** The tested 3. */
    @Tested
    TestedClassWithInjectOnConstructorOnly tested3;

    /**
     * Invoke inject annotated constructor only.
     */
    @Test
    void invokeInjectAnnotatedConstructorOnly() {
        assertSame(collaborator, tested1.collaborator);
        assertSame(collaborator1, tested1.collaborator1);
        assertNull(tested1.collaborator2);
        assertEquals(123, tested1.someValue);
        assertEquals(45, tested1.anotherValue);

        assertEquals(123, tested2.value);
    }

    /**
     * Assign inject annotated fields and also non annotated ones.
     *
     * @param collaborator2
     *            the collaborator 2
     * @param notToBeUsed
     *            the not to be used
     */
    @Test
    void assignInjectAnnotatedFieldsAndAlsoNonAnnotatedOnes(@Injectable Collaborator collaborator2,
            @Injectable("67") int notToBeUsed) {
        assertSame(collaborator, tested1.collaborator);
        assertSame(collaborator1, tested1.collaborator1);
        assertSame(collaborator2, tested1.collaborator2);
        assertEquals(123, tested1.someValue);
        assertEquals(45, tested1.anotherValue);

        assertEquals(123, tested2.value);
    }

    /**
     * Assign annotated field even if tested class has no annotated constructor.
     *
     * @param value
     *            the value
     */
    @Test
    void assignAnnotatedFieldEvenIfTestedClassHasNoAnnotatedConstructor(@Injectable("123") int value) {
        assertEquals(123, tested2.value);
    }

    /** The action. */
    @Injectable
    Runnable action;

    /**
     * Assign annotated static field during field injection.
     */
    @Test
    void assignAnnotatedStaticFieldDuringFieldInjection() {
        assertSame(action, TestedClass.globalAction);
    }

    /**
     * Consider annotated and non annotated fields for injection.
     *
     * @param text2
     *            the text 2
     */
    @Test
    void considerAnnotatedAndNonAnnotatedFieldsForInjection(@Injectable("XY") String text2) {
        assertEquals(aText, tested2.aText);
        assertNull(tested2.anotherText);
        assertEquals(aText, tested3.name);
    }

    /**
     * The Class TestedClassWithProviders.
     */
    static final class TestedClassWithProviders {

        /** The port. */
        final int port;

        /** The collaborator. */
        final Collaborator collaborator;

        /** The user. */
        @Inject
        Provider<String> user;

        /** The password. */
        @Inject
        Provider<String> password;

        /**
         * Instantiates a new tested class with providers.
         *
         * @param port
         *            the port
         * @param collaborator
         *            the collaborator
         */
        @Inject
        TestedClassWithProviders(Provider<Integer> port, Collaborator collaborator) {
            this.port = port.get();
            this.collaborator = collaborator;
        }
    }

    /** The tested 4. */
    @Tested
    TestedClassWithProviders tested4;

    /** The port number. */
    @Injectable
    Integer portNumber = 4567;

    /** The user. */
    @Injectable
    String user = "John";

    /** The password. */
    @Injectable
    String password = "123";

    /**
     * Support provider fields and parameters.
     */
    @Test
    void supportProviderFieldsAndParameters() {
        assertEquals(portNumber.intValue(), tested4.port);
        assertSame(collaborator, tested4.collaborator);
        assertEquals(user, tested4.user.get());
        assertEquals(password, tested4.password.get());
    }

    /**
     * The Class TestedClassWithVarargsParameterForProviders.
     */
    static final class TestedClassWithVarargsParameterForProviders {

        /** The collaborator 1. */
        final Collaborator collaborator1;

        /** The collaborator 2. */
        final Collaborator collaborator2;

        /** The optional collaborators. */
        final List<Collaborator> optionalCollaborators = new ArrayList<>();

        /** The name provider. */
        @Inject
        Provider<String> nameProvider;

        /**
         * Instantiates a new tested class with varargs parameter for providers.
         *
         * @param collaborators
         *            the collaborators
         */
        @SuppressWarnings({ "unchecked", "VariableArgumentMethod" })
        @Inject
        TestedClassWithVarargsParameterForProviders(Provider<Collaborator>... collaborators) {
            int n = collaborators.length;
            assertTrue(n > 1);

            collaborator1 = collaborators[0].get();
            assertSame(collaborator1, collaborators[0].get()); // default (singleton)

            collaborator2 = collaborators[2].get();
            assertNull(collaborators[2].get()); // recorded

            if (n > 3) {
                Collaborator col = collaborators[3].get();
                optionalCollaborators.add(col);
            }
        }
    }

    /** The tested 5. */
    @Tested
    TestedClassWithVarargsParameterForProviders tested5;

    /** The collaborator provider. */
    @Injectable
    Provider<Collaborator> collaboratorProvider;

    /** The col 3. */
    @Injectable
    Collaborator col3;

    /**
     * Configure provider used by constructor of tested class.
     */
    @BeforeEach
    void configureProviderUsedByConstructorOfTestedClass() {
        new Expectations() {
            {
                Collaborator[] collaborators = { col3, null };
                collaboratorProvider.get();
                result = collaborators;
            }
        };
    }

    /**
     * Support varargs parameter with providers.
     *
     * @param nameProvider
     *            the name provider
     */
    @Test
    void supportVarargsParameterWithProviders(@Injectable final Provider<String> nameProvider) {
        final String[] names = { "John", "Mary" };
        new Expectations() {
            {
                nameProvider.get();
                result = names;
            }
        };

        assertSame(collaborator, tested5.collaborator1);
        assertNotNull(tested5.collaborator2);
        assertNotSame(tested5.collaborator1, tested5.collaborator2);
        assertEquals(singletonList(col3), tested5.optionalCollaborators);

        assertEquals(names[0], tested5.nameProvider.get());
        assertEquals(names[1], tested5.nameProvider.get());
    }

    /**
     * Fields not annotated with known DI annotations should still be injected.
     */
    @Test
    void fieldsNotAnnotatedWithKnownDIAnnotationsShouldStillBeInjected() {
        assertEquals("Abc", tested1.nonAnnotatedField);
        assertSame(callable, tested1.nonAnnotatedGenericField);
    }

    /**
     * The Class DependencyToBeProvided.
     */
    public static final class DependencyToBeProvided {
    }

    /**
     * The Class TestedClassWithProvider.
     */
    public static final class TestedClassWithProvider {
        /** The provider. */
        @Inject
        Provider<DependencyToBeProvided> provider;
    }

    /** The tested 6. */
    @Tested(fullyInitialized = true)
    TestedClassWithProvider tested6;

    /**
     * Instantiate class with dependency from standard provider.
     */
    @Test
    void instantiateClassWithDependencyFromStandardProvider() {
        DependencyToBeProvided providedDependency1 = tested6.provider.get();
        DependencyToBeProvided providedDependency2 = tested6.provider.get();
        assertNotNull(providedDependency1);
        assertNotNull(providedDependency2);
        assertNotSame(providedDependency1, providedDependency2);
    }

    /**
     * The Class SingletonDependencyToBeProvided.
     */
    @Singleton
    public static final class SingletonDependencyToBeProvided {
    }

    /**
     * The Class TestedClassWithSingletonProvider.
     */
    public static final class TestedClassWithSingletonProvider {
        /** The provider. */
        @Inject
        Provider<SingletonDependencyToBeProvided> provider;
    }

    /** The tested 7. */
    @Tested(fullyInitialized = true)
    TestedClassWithSingletonProvider tested7;

    /**
     * Instantiate class with singleton dependency from standard provider.
     */
    @Test
    void instantiateClassWithSingletonDependencyFromStandardProvider() {
        SingletonDependencyToBeProvided providedDependency1 = tested7.provider.get();
        SingletonDependencyToBeProvided providedDependency2 = tested7.provider.get();
        assertNotNull(providedDependency1);
        assertNotNull(providedDependency2);
        assertSame(providedDependency1, providedDependency2);
    }
}
