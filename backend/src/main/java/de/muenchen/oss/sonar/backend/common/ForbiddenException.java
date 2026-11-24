package de.muenchen.oss.sonar.backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@SuppressWarnings("PMD.MissingSerialVersionUID")
public class ForbiddenException extends ResponseStatusException {
    public ForbiddenException(final String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
