package de.muenchen.oss.sonar.backend.projekt;

import java.time.LocalDate;

public record ProjektFilter(
        String projektnummer,
        LocalDate abrechnungBeginn,
        LocalDate abrechnungEnde) {

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
