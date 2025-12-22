package com.github.amangusss.exception;

public class UnsupportedCodeException extends BusinessException {
    public UnsupportedCodeException(String code, String className) {
        super(String.format("Unsupported code: %s for enum %s", code, className), "UNSUPPORTED_CODE");
    }
}
