package de.muenchen.oss.sonar.backend.projekt.dto;

import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import java.time.LocalDate;
import java.util.UUID;

public record ProjektAdresseResponseDTO(
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
        Integer anzahlMahnungen,
        boolean sondernutzungErlaubt) {
}
