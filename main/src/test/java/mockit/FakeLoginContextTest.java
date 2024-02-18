package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * The Class FakeLoginContextTest.
 */
public final class FakeLoginContextTest {

    /** The thrown. */
    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    /**
     * Fake JRE method and constructor using fake class.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void fakeJREMethodAndConstructorUsingFakeClass() throws Exception {
        new FakeLoginContext();

        new LoginContext("test", (CallbackHandler) null).login();
    }

    /**
     * The Class FakeLoginContext.
     */
    public static class FakeLoginContext extends MockUp<LoginContext> {

        /**
         * $init.
         *
         * @param name
         *            the name
         * @param callbackHandler
         *            the callback handler
         */
        @Mock
        public void $init(String name, CallbackHandler callbackHandler) {
            assertEquals("test", name);
            assertNull(callbackHandler);
        }

        /**
         * Login.
         */
        @Mock
        public void login() {
        }

        /**
         * Gets the subject.
         *
         * @return the subject
         */
        @Mock
        public Subject getSubject() {
            return null;
        }
    }

    /**
     * Fake JRE method and constructor with fake class.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void fakeJREMethodAndConstructorWithFakeClass() throws Exception {
        thrown.expect(LoginException.class);

        new MockUp<LoginContext>() {
            @Mock
            void $init(String name) {
                assertEquals("test", name);
            }

            @Mock
            void login() throws LoginException {
                throw new LoginException();
            }
        };

        new LoginContext("test").login();
    }

    /**
     * Fake JRE class with stubs.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void fakeJREClassWithStubs() throws Exception {
        new FakeLoginContextWithStubs();

        LoginContext context = new LoginContext("");
        context.login();
        context.logout();
    }

    /**
     * The Class FakeLoginContextWithStubs.
     */
    static final class FakeLoginContextWithStubs extends MockUp<LoginContext> {

        /**
         * $init.
         *
         * @param s
         *            the s
         */
        @Mock
        void $init(String s) {
        }

        /**
         * Logout.
         */
        @Mock
        void logout() {
        }

        /**
         * Login.
         */
        @Mock
        void login() {
        }
    }

    /**
     * Access faked instance.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void accessFakedInstance() throws Exception {
        new MockUp<LoginContext>() {
            Subject testSubject;

            @Mock
            void $init(Invocation inv, String name, Subject subject) {
                assertNotNull(name);
                assertNotNull(subject);
                LoginContext it = inv.getInvokedInstance();
                assertNotNull(it);
                assertEquals(1, inv.getInvocationCount());
            }

            @Mock
            void login(Invocation inv) {
                LoginContext it = inv.getInvokedInstance();
                assertNull(it.getSubject()); // returns null until the subject is authenticated
                testSubject = new Subject();
            }

            @Mock
            void logout() {
                testSubject = null;
            }

            @Mock
            Subject getSubject() {
                return testSubject;
            }
        };

        LoginContext fakedInstance = new LoginContext("test", new Subject());
        assertNull(fakedInstance.getSubject());
        fakedInstance.login();
        assertNotNull(fakedInstance.getSubject());
        fakedInstance.logout();
        assertNull(fakedInstance.getSubject());
    }

    /**
     * Proceed into real implementations of faked methods.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void proceedIntoRealImplementationsOfFakedMethods() throws Exception {
        // Create objects to be exercised by the code under test:
        Configuration configuration = new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                Map<String, ?> options = Collections.emptyMap();

                return new AppConfigurationEntry[] { new AppConfigurationEntry(TestLoginModule.class.getName(),
                        AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT, options) };
            }
        };

        LoginContext loginContext = new LoginContext("test", null, null, configuration);

        // Apply fakes:
        ProceedingFakeLoginContext fakeInstance = new ProceedingFakeLoginContext();

        // Exercise the code under test:
        assertNull(loginContext.getSubject());
        loginContext.login();
        assertNotNull(loginContext.getSubject());
        assertTrue(fakeInstance.loggedIn);

        fakeInstance.ignoreLogout = true;
        loginContext.logout();
        assertTrue(fakeInstance.loggedIn);

        fakeInstance.ignoreLogout = false;
        loginContext.logout();
        assertFalse(fakeInstance.loggedIn);
    }

    /**
     * The Class ProceedingFakeLoginContext.
     */
    static final class ProceedingFakeLoginContext extends MockUp<LoginContext> {

        /** The ignore logout. */
        boolean ignoreLogout;

        /** The logged in. */
        boolean loggedIn;

        /**
         * Login.
         *
         * @param inv
         *            the inv
         */
        @Mock
        void login(Invocation inv) {
            LoginContext it = inv.getInvokedInstance();

            try {
                inv.proceed();
                loggedIn = true;
            } finally {
                it.getSubject();
            }
        }

        /**
         * Logout.
         *
         * @param inv
         *            the inv
         */
        @Mock
        void logout(Invocation inv) {
            if (!ignoreLogout) {
                inv.proceed();
                loggedIn = false;
            }
        }
    }

    /**
     * The Class TestLoginModule.
     */
    public static class TestLoginModule implements LoginModule {
        @Override
        public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
                Map<String, ?> options) {
        }

        @Override
        public boolean login() {
            return true;
        }

        @Override
        public boolean commit() {
            return true;
        }

        @Override
        public boolean abort() {
            return false;
        }

        @Override
        public boolean logout() {
            return true;
        }
    }
}
