package de.muenchen.oss.sonar.backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Exception if a request passes the validation of its fields and is still invalid. */
@SuppressWarnings("PMD.MissingSerialVersionUID")
public class BadRequestException extends ResponseStatusException {
    public BadRequestException(final String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
