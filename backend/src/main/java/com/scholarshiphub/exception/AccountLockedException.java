package com.scholarshiphub.exception;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AccountLockedException extends BusinessException {

    public AccountLockedException(Instant lockedUntil) {
        super(ErrorCode.ACCOUNT_LOCKED, "Account is locked due to repeated failed login attempts. Try again in "
                + Math.max(1, ChronoUnit.MINUTES.between(Instant.now(), lockedUntil)) + " minute(s).");
    }
}
