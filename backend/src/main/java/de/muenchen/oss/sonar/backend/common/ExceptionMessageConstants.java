package de.muenchen.oss.sonar.backend.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@SuppressWarnings("PMD.DataClass")
public class ExceptionMessageConstants {
    public static final String MSG_NOT_FOUND = "Could not find entity with id %s";
    public static final String MSG_WIDERSPRUCH_ALREADY_EXISTS = "The Abrechnung with id %s already has a Widerspruch";
    public static final String MSG_NEWER_VERSION_ALREADY_EXISTS = "The Abrechnung with id %s already has a newer version";
    public static final String MSG_PROJEKT_LINKED_TO_ITSELF = "The Projekt with id %s cannot be its own Vorgaenger";
    public static final String MSG_PROJEKT_ALREADY_LINKED = "The Projekt with id %s is already linked to another Projekt";
    public static final String MSG_PROJEKT_HAS_NACHFOLGER = "The Projekt with id %s already has a Nachfolger";
    public static final String MSG_VORGAENGER_WITHOUT_ABRECHNUNG = "The Vorgaenger of the Projekt with id %s has no Abrechnung yet";
    public static final String MSG_ZEITRAUM_VON_NOT_AFTER_VORGAENGER = "The first Abrechnung of the Projekt with id %s has to start on %s";
}
