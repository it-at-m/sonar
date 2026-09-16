package de.muenchen.oss.sonar.backend.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Exception if data conflicts with what is already stored. */
@SuppressWarnings("PMD.MissingSerialVersionUID")
public class ConflictException extends ResponseStatusException {
    /**
     * ConflictException constructor
     *
     * @param message Exception message
     */
    public ConflictException(final String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
