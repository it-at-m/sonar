package de.muenchen.oss.sonar.backend.projekt;

import java.time.LocalDate;
import java.util.UUID;

public record ProjektFilter(
        String projektnummer,
        LocalDate abrechnungBeginn,
        LocalDate abrechnungEnde,
        UUID sharesAdresseWithProjektId) {

    public ProjektFilter {
        projektnummer = trimmedOrNull(projektnummer);
    }

    private static String trimmedOrNull(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
