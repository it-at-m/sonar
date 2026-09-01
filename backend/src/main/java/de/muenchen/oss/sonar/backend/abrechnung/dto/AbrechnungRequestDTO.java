package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungsArt;
import de.muenchen.oss.sonar.backend.abrechnung.ZustellungsbevollmaechtigterTyp;
import de.muenchen.oss.sonar.backend.common.ZeitraumOrdered;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * The Abrechnung to create.
 * <p>
 * An Abrechnung is created below its Projekt, so the path decides which one it belongs to.
 * </p>
 */
@ZeitraumOrdered(von = "zeitraumVon", bis = "zeitraumBis")
@ZustellungsbevollmaechtigterValid
public record AbrechnungRequestDTO(
        @NotNull @Size(min = 1, max = 10) String geschaeftspartnerId,
        boolean zustellungsbevollmaechtigterGenutzt,
        @Size(min = 1, max = 10) String zustellungsbevollmaechtigterId,
        ZustellungsbevollmaechtigterTyp zustellungsbevollmaechtigterTyp,
        @NotNull LocalDate zeitraumVon,
        @NotNull LocalDate zeitraumBis,
        @NotNull AbrechnungsArt abrechnungsArt,
        @NotEmpty List<@Valid AbrechnungNutzungsobjektRequestDTO> nutzungsobjekte) {

    /**
     * Copies the Nutzungsobjekte in, so that the request stays immutable however the caller treats the
     * list it passed. A missing list becomes an empty one, which {@code @NotEmpty} rejects just the
     * same.
     */
    public AbrechnungRequestDTO {
        nutzungsobjekte = nutzungsobjekte == null ? List.of() : List.copyOf(nutzungsobjekte);
    }
}
