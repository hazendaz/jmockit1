package jmockit.loginExample.domain.userLogin;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.checkerframework.checker.index.qual.NonNegative;

import jmockit.loginExample.domain.userAccount.UserAccount;

public final class LoginService {
    private static final int MAX_LOGIN_ATTEMPTS = 3;

    @NonNegative
    private int loginAttemptsRemaining = MAX_LOGIN_ATTEMPTS;
    @Nullable
    private String previousAccountId;
    @Nullable
    private UserAccount account;

    public void login(@NonNull String accountId, @NonNull String password)
            throws UserAccountNotFoundException, UserAccountRevokedException, AccountLoginLimitReachedException {
        account = UserAccount.find(accountId);

        if (account == null) {
            throw new UserAccountNotFoundException();
        }

        if (account.passwordMatches(password)) {
            registerNewLogin();
        } else {
            handleFailedLoginAttempt(accountId);
        }
    }

    private void registerNewLogin() throws AccountLoginLimitReachedException, UserAccountRevokedException {
        // noinspection ConstantConditions
        if (account.isLoggedIn()) {
            throw new AccountLoginLimitReachedException();
        }

        if (account.isRevoked()) {
            throw new UserAccountRevokedException();
        }

        account.setLoggedIn(true);
        loginAttemptsRemaining = MAX_LOGIN_ATTEMPTS;
    }

    private void handleFailedLoginAttempt(@NonNull String accountId) {
        if (previousAccountId == null) {
            loginAttemptsRemaining--;
            previousAccountId = accountId;
        } else if (accountId.equals(previousAccountId)) {
            loginAttemptsRemaining--;

            if (loginAttemptsRemaining == 0) {
                // noinspection ConstantConditions
                account.setRevoked(true);
                loginAttemptsRemaining = MAX_LOGIN_ATTEMPTS;
            }
        } else {
            loginAttemptsRemaining = MAX_LOGIN_ATTEMPTS;
            previousAccountId = accountId;
        }
    }
}
