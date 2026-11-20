package de.muenchen.oss.sonar.backend.abrechnung.domain;

/**
 * A Nutzungsobjekt that an Abrechnung of the Projekt can take over.
 *
 * @param vorgaengerProjektnummer the Projektnummer of the nearest Vorgaenger that bills it, or null
 *            when the Projekt itself bills it
 */
public record NutzungsobjektSuggestion(AbrechnungNutzungsobjekt nutzungsobjekt, String vorgaengerProjektnummer) {
}
