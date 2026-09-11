package de.muenchen.oss.sonar.backend.widerspruch.domain;

import java.time.LocalDate;
import java.util.UUID;

public record Widerspruch(
        UUID id,
        UUID abrechnungId,
        LocalDate datumEingang,
        LocalDate datumRuecknahme,
        LocalDate datumVorlageRegierung,
        LocalDate datumAblehnungRegierung,
        String entscheidungDurchfuehrung,
        boolean sollAbgesetzt,
        boolean neueTeilabrechnungAnlegen,
        String bemerkung) {
}
