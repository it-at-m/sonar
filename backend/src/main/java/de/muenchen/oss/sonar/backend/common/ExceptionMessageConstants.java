package de.muenchen.oss.sonar.backend.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionMessageConstants {
    public static final String MSG_NOT_FOUND = "Could not find entity with id %s";
    public static final String MSG_WIDERSPRUCH_ALREADY_EXISTS = "The Abrechnung with id %s already has a Widerspruch";
    public static final String MSG_NEWER_VERSION_ALREADY_EXISTS = "The Abrechnung with id %s already has a newer version";
}
