package com.scholarshiphub.exception;

public class EmailNotVerifiedException extends BusinessException {

    public EmailNotVerifiedException() {
        super(ErrorCode.EMAIL_NOT_VERIFIED, "Email address has not been verified yet. Please check your inbox.");
    }
}
