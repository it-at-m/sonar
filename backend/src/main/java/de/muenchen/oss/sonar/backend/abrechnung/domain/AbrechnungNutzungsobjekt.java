package de.muenchen.oss.sonar.backend.abrechnung.domain;

import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.common.Zeitraum;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

public record AbrechnungNutzungsobjekt(
        UUID id,
        Adressart art,
        String adresse,
        String hausnummerVon,
        String hausnummerBis,
        String flurstueck,
        String gemarkung,
        Nutzung nutzung,
        LocalDate unerlaubteNutzungVon,
        LocalDate unerlaubteNutzungBis,
        Integer tageUnerlaubteNutzung,
        String bemerkung,
        List<AbrechnungPosition> positionen,
        List<AbrechnungMast> masten) {

    /**
     * Derives the days here, because the value has to be settled before the Nutzungsobjekt is
     * persisted. Deriving them again when reading a stored one back costs nothing, as the same period
     * yields the same number and directly entered days pass through untouched.
     * <p>
     * The positions and the Masten are copied in, so that the Nutzungsobjekt stays immutable however
     * the caller treats the lists it passed. A missing list becomes an empty one: MapStruct maps a
     * null collection to null, and {@link List#copyOf} would reject it.
     * </p>
     */
    public AbrechnungNutzungsobjekt {
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
        positionen = positionen == null ? List.of() : List.copyOf(positionen);
        masten = masten == null ? List.of() : List.copyOf(masten);
    }

    public String adressbezeichnung() {
        if (art == Adressart.FLURSTUECK) {
            return "Flurstück " + joinNonBlank(", ", flurstueck, gemarkung);
        }
        final String hausnummer = StringUtils.hasText(hausnummerBis)
                ? hausnummerVon + "–" + hausnummerBis
                : hausnummerVon;
        return joinNonBlank(" ", adresse, hausnummer);
    }

    private String joinNonBlank(final String separator, final String... teile) {
        return Arrays.stream(teile).filter(StringUtils::hasText).collect(Collectors.joining(separator));
    }

    /**
     * The Aufschlag belongs to the Nutzungsobjekt and holds for all of its Positionen, but the schema
     * keeps it on the Position. The first one as it was entered decides for the whole Adresse until
     * the flag has moved to the Nutzungsobjekt. An Adresse carrying only Masten has no Fläche to
     * charge, so the answer there is never read.
     */
    public boolean isAufschlag50Prozent() {
        return !positionen.isEmpty() && positionen.getFirst().haelfte();
    }
}
