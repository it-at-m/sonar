package de.muenchen.oss.sonar.backend.abrechnung.domain;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungsArt;
import de.muenchen.oss.sonar.backend.abrechnung.ZustellungsbevollmaechtigterTyp;
import de.muenchen.oss.sonar.backend.common.Zeitraum;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record Abrechnung(
        UUID id,
        UUID projektId,
        String geschaeftspartnerId,
        boolean zustellungsbevollmaechtigterGenutzt,
        String zustellungsbevollmaechtigterId,
        ZustellungsbevollmaechtigterTyp zustellungsbevollmaechtigterTyp,
        LocalDate zeitraumVon,
        LocalDate zeitraumBis,
        AbrechnungsArt abrechnungsArt,
        List<AbrechnungNutzungsobjekt> nutzungsobjekte) {

    /**
     * Copies the Nutzungsobjekte in, so that the Abrechnung stays immutable however the caller treats
     * the list it passed. A missing list becomes an empty one: MapStruct maps a null collection to
     * null, and {@link List#copyOf} would reject it.
     */
    public Abrechnung {
        if (!Zeitraum.isOrdered(zeitraumVon, zeitraumBis)) {
            throw new IllegalArgumentException("zeitraumBis is before zeitraumVon");
        }
        nutzungsobjekte = nutzungsobjekte == null ? List.of() : List.copyOf(nutzungsobjekte);
    }
}
