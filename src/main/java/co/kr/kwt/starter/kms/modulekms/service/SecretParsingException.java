package co.kr.kwt.starter.kms.modulekms.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SecretParsingException extends RuntimeException {

    public SecretParsingException(String failedToParseSecretResponse, JsonProcessingException e) {
    }

    public SecretParsingException() {
    }

    public SecretParsingException(String message) {
        super(message);
    }

    public SecretParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SecretParsingException(Throwable cause) {
        super(cause);
    }
}
