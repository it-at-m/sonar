package de.muenchen.oss.sonar.backend.abrechnung.dto;

import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record NutzungsobjektResponseDTO(
        UUID id,
        Adressart art,
        String adresse,
        String hausnummerVon,
        String hausnummerBis,
        String flurstueck,
        String gemarkung,
        List<Nutzung> nutzungen,
        String nutzungSonstiges,
        LocalDate unerlaubteNutzungVon,
        LocalDate unerlaubteNutzungBis,
        Integer tageUnerlaubteNutzung,
        String bemerkung,
        boolean aufschlag50prozent) {

    public NutzungsobjektResponseDTO {
        nutzungen = List.copyOf(Nutzung.distinctSorted(nutzungen));
    }
}
