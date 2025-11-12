/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package mockit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import org.junit.jupiter.api.Test;

public final class MockLoginContextTest {

    @Test
    public void mockJREMethodAndConstructorUsingAnnotatedMockClass() throws Exception {
        new MockLoginContext();

        new LoginContext("test", (CallbackHandler) null).login();
    }

    public static class MockLoginContext extends MockUp<LoginContext> {
        @Mock
        public void $init(String name, CallbackHandler callbackHandler) {
            assertEquals("test", name);
            assertNull(callbackHandler);
        }

        @Mock
        public void login() {
        }

        @Mock
        public Subject getSubject() {
            return null;
        }
    }

    @Test
    public void mockJREMethodAndConstructorWithMockUpClass() throws Exception {
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

        assertThrows(LoginException.class, () -> {
            new LoginContext("test").login();
        });
    }

    @Test
    public void mockJREClassWithStubs() throws Exception {
        new MockLoginContextWithStubs();

        LoginContext context = new LoginContext("");
        context.login();
        context.logout();
    }

    final class MockLoginContextWithStubs extends MockUp<LoginContext> {
        @Mock
        void $init(String s) {
        }

        @Mock
        void logout() {
        }

        @Mock
        void login() {
        }
    }

    @Test
    public void proceedIntoRealImplementationsOfMockedMethods() throws Exception {
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

        // Set up mocks:
        ProceedingMockLoginContext mockInstance = new ProceedingMockLoginContext();

        // Exercise the code under test:
        assertNull(loginContext.getSubject());
        loginContext.login();
        assertNotNull(loginContext.getSubject());
        assertTrue(mockInstance.loggedIn);

        mockInstance.ignoreLogout = true;
        loginContext.logout();
        assertTrue(mockInstance.loggedIn);

        mockInstance.ignoreLogout = false;
        loginContext.logout();
        assertFalse(mockInstance.loggedIn);
    }

    static final class ProceedingMockLoginContext extends MockUp<LoginContext> {
        boolean ignoreLogout;
        boolean loggedIn;

        @Mock
        void login(Invocation inv) throws LoginException {
            LoginContext it = inv.getInvokedInstance();

            try {
                inv.proceed();
                loggedIn = true;
            } finally {
                it.getSubject();
            }
        }

        @Mock
        void logout(Invocation inv) throws LoginException {
            if (!ignoreLogout) {
                inv.proceed();
                loggedIn = false;
            }
        }
    }

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
