package de.muenchen.oss.sonar.backend.projekt.domain;

import de.muenchen.oss.sonar.backend.common.Zeitraum;
import java.time.LocalDate;
import java.util.UUID;

public record ProjektAdresse(
        UUID id,
        String bezeichnung,
        String baunutzung,
        LocalDate unerlaubteNutzungVon,
        LocalDate unerlaubteNutzungBis,
        Integer tageUnerlaubteNutzung,
        Integer anzahlMahnungen,
        boolean sondernutzungErlaubt) {

    /**
     * Derives the days here, because the value has to be settled before the Adresse is persisted.
     * Deriving again when reading a stored Adresse back costs nothing, as the same period yields the
     * same number and directly entered days pass through untouched.
     * <p>
     * A stored Adresse carries the period and the derived days together, so days given alongside a
     * period are rejected only when they disagree with it.
     */
    public ProjektAdresse {
        if ((unerlaubteNutzungVon == null) != (unerlaubteNutzungBis == null)) {
            throw new IllegalArgumentException("the Zeitraum der unerlaubten Nutzung needs both dates or neither");
        }
        if (!Zeitraum.isOrdered(unerlaubteNutzungVon, unerlaubteNutzungBis)) {
            throw new IllegalArgumentException("unerlaubteNutzungBis is before unerlaubteNutzungVon");
        }
        final Integer derived = Zeitraum.tageInklusiv(unerlaubteNutzungVon, unerlaubteNutzungBis, tageUnerlaubteNutzung);
        if (tageUnerlaubteNutzung != null && !tageUnerlaubteNutzung.equals(derived)) {
            throw new IllegalArgumentException("tageUnerlaubteNutzung contradicts the given Zeitraum");
        }
        tageUnerlaubteNutzung = derived;
    }
}
