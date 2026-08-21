package de.muenchen.oss.sonar.backend.projekt.model;

import java.time.LocalDate;

/**
 * Every criterion is optional and they are combined with AND.
 * <p>
 * A blank Projektnummer is normalized to null, so that clearing the search field does not turn into
 * a search for the empty string.
 * </p>
 */
public record ProjektFilter(
        String projektnummer,
        LocalDate abrechnungBeginn,
        LocalDate abrechnungEnde) {

    public ProjektFilter {
        projektnummer = blankToNull(projektnummer);
    }

    private static String blankToNull(final String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static ProjektFilter none() {
        return new ProjektFilter(null, null, null);
    }
}
