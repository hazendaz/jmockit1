/*
 * MIT License
 * Copyright (c) 2006-2025 JMockit developers
 * See LICENSE file for full license text.
 */
package jmockit.loginExample.domain.userLogin;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

import mockit.Mock;
import mockit.MockUp;
import mockit.Tested;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jmockit.loginExample.domain.userAccount.UserAccount;

/**
 * Equivalent to {@link LoginServiceNGTest}, but with minimal mocking and no redundant tests.
 */
public final class LoginServiceIntegrationTest {

    /** The service. */
    @Tested
    LoginService service;

    /** The user id. */
    String userId;

    /** The user password. */
    String userPassword;

    /** The account. */
    UserAccount account;

    /**
     * Sets the up one account to be found.
     */
    @BeforeMethod
    public void setUpOneAccountToBeFound() {
        userId = "john";
        userPassword = "password";
        account = new UserAccount(userId, userPassword);

        new MockUp<UserAccount>() {
            @Mock
            UserAccount find(String accountId) {
                return account;
            }
        };
    }

    /**
     * Sets the account to logged in when password matches.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void setAccountToLoggedInWhenPasswordMatches() throws Exception {
        // Failed login attempts are inconsequential, provided they don't exceed the maximum number of attempts.
        service.login(userId, "wrong password");
        service.login(userId, "wrong password");
        service.login(userId, userPassword);
        service.login(userId, "wrong password");

        assertTrue(account.isLoggedIn());
        assertFalse(account.isRevoked());
    }

    /**
     * Sets the account to revoked after three failed login attempts.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void setAccountToRevokedAfterThreeFailedLoginAttempts() throws Exception {
        service.login(userId, "wrong password");
        service.login(userId, "wrong password");
        service.login(userId, "wrong password");

        assertFalse(account.isLoggedIn());
        assertTrue(account.isRevoked());
    }

    /**
     * Not revoke second account after two failed attempts on first account.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    public void notRevokeSecondAccountAfterTwoFailedAttemptsOnFirstAccount() throws Exception {
        UserAccount secondAccount = new UserAccount("roger", "password");
        String accountId = account.getId();
        account = secondAccount;

        service.login(accountId, "wrong password");
        service.login(accountId, "wrong password");
        service.login(secondAccount.getId(), "wrong password");

        assertFalse(secondAccount.isRevoked());
    }

    /**
     * Disallow concurrent logins.
     */
    @Test
    public void disallowConcurrentLogins() {
        account.setLoggedIn(true);

        assertThrows(AccountLoginLimitReachedException.class, () -> {
            service.login(userId, userPassword);
        });
    }

    /**
     * Throw exception if account not found.
     */
    @Test
    public void throwExceptionIfAccountNotFound() {
        account = null;

        assertThrows(UserAccountNotFoundException.class, () -> {
            service.login("roger", "password");
        });
    }

    /**
     * Disallow logging into revoked account.
     */
    @Test
    public void disallowLoggingIntoRevokedAccount() {
        account.setRevoked(true);

        assertThrows(UserAccountRevokedException.class, () -> {
            service.login(userId, userPassword);
        });
    }
}
