package jmockit.loginExample.domain.userLogin;

import static org.testng.Assert.assertThrows;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;

import org.testng.annotations.Test;

import jmockit.loginExample.domain.userAccount.UserAccount;

/**
 * A small TestNG test suite for a single class (<code>LoginService</code>), based on
 * <a href="http://schuchert.wikispaces.com/Mockito.LoginServiceExample">this article</a>.
 */
public final class LoginServiceNGTest {

    /** The service. */
    @Tested
    LoginService service;

    /** The account. */
    @Mocked
    UserAccount account;

    /**
     * This test is redundant, as it exercises the same path as the last test. It cannot simply be removed, because the
     * last test does not perform the "account.setLoggedIn(true)" verification; if said verification is added there,
     * however, then this test could be removed without weakening the test suite.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void setAccountToLoggedInWhenPasswordMatches() throws Exception {
        willMatchPassword(true);

        service.login("john", "password");

        new Verifications() {
            {
                account.setLoggedIn(true);
            }
        };
    }

    /**
     * Will match password.
     *
     * @param matches
     *            the matches
     */
    void willMatchPassword(boolean... matches) {
        new Expectations() {
            {
                account.passwordMatches(anyString);
                result = matches;
            }
        };
    }

    /**
     * Sets the account to revoked after three failed login attempts.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void setAccountToRevokedAfterThreeFailedLoginAttempts() throws Exception {
        willMatchPassword(false);

        for (int i = 0; i < 3; i++) {
            service.login("john", "password");
        }

        new Verifications() {
            {
                account.setRevoked(true);
            }
        };
    }

    /**
     * This test is also redundant, as it exercises the same path as the previous one. Again, it cannot simply be
     * removed since the previous test does not verify that "account.setLoggedIn(true)" is never called; if said
     * verification is added there, however, this test could safely be removed.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void notSetAccountLoggedInIfPasswordDoesNotMatch() throws Exception {
        willMatchPassword(false);

        service.login("john", "password");

        new Verifications() {
            {
                account.setLoggedIn(true);
                times = 0;
            }
        };
    }

    /**
     * Not revoke second account after two failed attempts on first account.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void notRevokeSecondAccountAfterTwoFailedAttemptsOnFirstAccount() throws Exception {
        new Expectations() {
            {
                account.passwordMatches(anyString);
                result = false;
            }
        };

        service.login("john", "password");
        service.login("john", "password");
        service.login("roger", "password");

        new Verifications() {
            {
                account.setRevoked(true);
                times = 0;
            }
        };
    }

    /**
     * Disallow concurrent logins.
     */
    @Test
    public void disallowConcurrentLogins() {
        willMatchPassword(true);

        new Expectations() {
            {
                account.isLoggedIn();
                result = true;
            }
        };

        assertThrows(AccountLoginLimitReachedException.class, () -> {
            service.login("john", "password");
        });
    }

    /**
     * Throw exception if account not found.
     */
    @Test
    public void throwExceptionIfAccountNotFound() {
        new Expectations() {
            {
                UserAccount.find("roger");
                result = null;
            }
        };

        assertThrows(UserAccountNotFoundException.class, () -> {
            service.login("roger", "password");
        });
    }

    /**
     * Disallow logging into revoked account.
     */
    @Test
    public void disallowLoggingIntoRevokedAccount() {
        willMatchPassword(true);

        new Expectations() {
            {
                account.isRevoked();
                result = true;
            }
        };

        assertThrows(UserAccountRevokedException.class, () -> {
            service.login("john", "password");
        });
    }

    /**
     * Reset back to initial state after successful login.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void resetBackToInitialStateAfterSuccessfulLogin() throws Exception {
        willMatchPassword(false, false, true, false);

        service.login("john", "password");
        service.login("john", "password");
        service.login("john", "password");
        service.login("john", "password");

        new Verifications() {
            {
                account.setRevoked(true);
                times = 0;
            }
        };
    }
}
