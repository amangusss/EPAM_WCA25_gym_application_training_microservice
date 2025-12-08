package com.github.amangusss.exception;

public class UnsupportedCodeException extends BusinessException {
    public UnsupportedCodeException(String code, String className) {
        super(String.format("Unsupported code: " + code + " for enum " + className), "UNSUPPORTED_CODE");
    }
}
