package de.muenchen.oss.sonar.backend.widerspruch.dto;

import java.time.LocalDate;
import java.util.UUID;

public record WiderspruchResponseDTO(
        UUID id,
        LocalDate datumEingang,
        LocalDate datumRuecknahme,
        LocalDate datumVorlageRegierung,
        LocalDate datumAblehnungRegierung,
        String entscheidungDurchfuehrung,
        boolean sollAbgesetzt,
        boolean neueTeilabrechnungAnlegen,
        String bemerkung) {
}
