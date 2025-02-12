package co.kr.kwt.starter.kms.modulekms.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SecretParsingException extends RuntimeException {
    public SecretParsingException(String failedToParseSecretResponse, JsonProcessingException e) {
    }
}
