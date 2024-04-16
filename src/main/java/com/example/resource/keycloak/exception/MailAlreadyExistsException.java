package com.example.resource.keycloak.exception;

public class MailAlreadyExistsException extends RuntimeException {

    public MailAlreadyExistsException() {
        super("E-Mail already used.");
    }
}
