package de.muenchen.oss.sonar.backend.widerspruch.dto;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.widerspruch.domain.Widerspruch;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class WiderspruchDTOMapperTest {

    private static final LocalDate EINGANG = LocalDate.of(2026, 4, 1);

    private final WiderspruchDTOMapper widerspruchDTOMapper = Mappers.getMapper(WiderspruchDTOMapper.class);

    @Nested
    class ToDTO {
        @Test
        void givenWiderspruch_thenReturnsCorrectDTO() {
            final UUID widerspruchId = UUID.randomUUID();
            final UUID abrechnungId = UUID.randomUUID();
            final Widerspruch widerspruch = new Widerspruch(widerspruchId, abrechnungId, EINGANG,
                    LocalDate.of(2026, 4, 15), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 1),
                    "Abrechnung wird durchgeführt", true, false, "Bemerkung");

            final WiderspruchResponseDTO result = widerspruchDTOMapper.toDTO(widerspruch);

            assertThat(result.id()).isEqualTo(widerspruchId);
            assertThat(result.abrechnungId()).isEqualTo(abrechnungId);
            assertThat(result.datumEingang()).isEqualTo(EINGANG);
            assertThat(result.datumRuecknahme()).isEqualTo(LocalDate.of(2026, 4, 15));
            assertThat(result.datumVorlageRegierung()).isEqualTo(LocalDate.of(2026, 5, 1));
            assertThat(result.datumAblehnungRegierung()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(result.entscheidungDurchfuehrung()).isEqualTo("Abrechnung wird durchgeführt");
            assertThat(result.sollAbgesetzt()).isTrue();
            assertThat(result.neueTeilabrechnungAnlegen()).isFalse();
            assertThat(result.bemerkung()).isEqualTo("Bemerkung");
        }
    }

    @Nested
    class ToWiderspruch {
        @Test
        void givenRequestDTO_thenTakeTheAbrechnungFromThePath() {
            final UUID abrechnungId = UUID.randomUUID();
            final WiderspruchRequestDTO requestDTO = new WiderspruchRequestDTO(EINGANG, LocalDate.of(2026, 4, 15),
                    LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 1), "Abrechnung wird durchgeführt", true, true,
                    "Bemerkung");

            final Widerspruch result = widerspruchDTOMapper.toWiderspruch(abrechnungId, requestDTO);

            assertThat(result.id()).isNull();
            assertThat(result.abrechnungId()).isEqualTo(abrechnungId);
            assertThat(result.datumEingang()).isEqualTo(EINGANG);
            assertThat(result.datumRuecknahme()).isEqualTo(LocalDate.of(2026, 4, 15));
            assertThat(result.datumVorlageRegierung()).isEqualTo(LocalDate.of(2026, 5, 1));
            assertThat(result.datumAblehnungRegierung()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(result.entscheidungDurchfuehrung()).isEqualTo("Abrechnung wird durchgeführt");
            assertThat(result.sollAbgesetzt()).isTrue();
            assertThat(result.neueTeilabrechnungAnlegen()).isTrue();
            assertThat(result.bemerkung()).isEqualTo("Bemerkung");
        }

        @Test
        void givenOnlyDatumEingang_thenLeaveTheOptionalFieldsEmpty() {
            final UUID abrechnungId = UUID.randomUUID();
            final WiderspruchRequestDTO requestDTO = new WiderspruchRequestDTO(EINGANG, null, null, null, null, false,
                    false, null);

            final Widerspruch result = widerspruchDTOMapper.toWiderspruch(abrechnungId, requestDTO);

            assertThat(result.datumEingang()).isEqualTo(EINGANG);
            assertThat(result.datumRuecknahme()).isNull();
            assertThat(result.datumVorlageRegierung()).isNull();
            assertThat(result.datumAblehnungRegierung()).isNull();
            assertThat(result.entscheidungDurchfuehrung()).isNull();
            assertThat(result.sollAbgesetzt()).isFalse();
            assertThat(result.neueTeilabrechnungAnlegen()).isFalse();
            assertThat(result.bemerkung()).isNull();
        }
    }
}
