package de.muenchen.oss.sonar.backend.widerspruch.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record WiderspruchRequestDTO(
        @NotNull LocalDate datumEingang,
        LocalDate datumRuecknahme,
        LocalDate datumVorlageRegierung,
        LocalDate datumAblehnungRegierung,
        @Size(min = 1, max = 255) String entscheidungDurchfuehrung,
        boolean sollAbgesetzt,
        boolean neueTeilabrechnungAnlegen,
        @Size(min = 1, max = 10_000) String bemerkung) {
}
