package de.muenchen.oss.sonar.backend.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("PMD.DataClass")
public class ExceptionMessageConstants {
    public static final String MSG_NOT_FOUND = "Could not find entity with id %s";
    public static final String MSG_WIDERSPRUCH_ALREADY_EXISTS = "The Abrechnung with id %s already has a Widerspruch";
    public static final String MSG_NEWER_VERSION_ALREADY_EXISTS = "The Abrechnung with id %s already has a newer version";
    public static final String MSG_PROJEKT_CONNECTED_TO_ITSELF = "The Projekt with id %s cannot be connected to itself";
    public static final String MSG_PROJEKT_HAS_VERBUNDENE_PROJEKTE = "Other Projekte are connected to the Projekt with id %s, so it cannot be connected to another Projekt";
}
