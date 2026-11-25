package de.muenchen.oss.sonar.backend.projekt;

import de.muenchen.oss.sonar.backend.stadtbezirk.Stadtbezirk;
import java.time.LocalDate;
import java.util.Set;

public record ProjektFilter(
        String projektnummer,
        LocalDate abrechnungBeginn,
        LocalDate abrechnungEnde,
        Set<Stadtbezirk> permittedStadtbezirke) {

    public ProjektFilter {
        projektnummer = trimmedOrNull(projektnummer);
        permittedStadtbezirke = Set.copyOf(permittedStadtbezirke);
    }

    private static String trimmedOrNull(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
