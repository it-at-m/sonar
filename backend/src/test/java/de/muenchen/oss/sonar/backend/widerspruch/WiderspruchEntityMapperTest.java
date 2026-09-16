package de.muenchen.oss.sonar.backend.widerspruch;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.widerspruch.domain.Widerspruch;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class WiderspruchEntityMapperTest {

    private static final LocalDate EINGANG = LocalDate.of(2026, 4, 1);

    private final WiderspruchEntityMapper widerspruchEntityMapper = Mappers.getMapper(WiderspruchEntityMapper.class);

    @Nested
    class ToEntity {
        @Test
        void givenWiderspruch_thenDropTheIdAndKeepTheRest() {
            final UUID abrechnungId = UUID.randomUUID();
            final Widerspruch widerspruch = new Widerspruch(UUID.randomUUID(), abrechnungId, EINGANG,
                    LocalDate.of(2026, 4, 15), LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 1),
                    "Abrechnung wird durchgeführt", true, true, "Bemerkung");

            final WiderspruchEntity result = widerspruchEntityMapper.toEntity(widerspruch);

            assertThat(result.getId()).isNull();
            assertThat(result.getAbrechnungId()).isEqualTo(abrechnungId);
            assertThat(result.getDatumEingang()).isEqualTo(EINGANG);
            assertThat(result.getDatumRuecknahme()).isEqualTo(LocalDate.of(2026, 4, 15));
            assertThat(result.getDatumVorlageRegierung()).isEqualTo(LocalDate.of(2026, 5, 1));
            assertThat(result.getDatumAblehnungRegierung()).isEqualTo(LocalDate.of(2026, 6, 1));
            assertThat(result.getEntscheidungDurchfuehrung()).isEqualTo("Abrechnung wird durchgeführt");
            assertThat(result.isSollAbgesetzt()).isTrue();
            assertThat(result.isNeueTeilabrechnungAnlegen()).isTrue();
            assertThat(result.getBemerkung()).isEqualTo("Bemerkung");
        }

        @Test
        void givenOnlyDatumEingang_thenLeaveTheOptionalFieldsEmpty() {
            final Widerspruch widerspruch = new Widerspruch(null, UUID.randomUUID(), EINGANG, null, null, null, null,
                    false, false, null);

            final WiderspruchEntity result = widerspruchEntityMapper.toEntity(widerspruch);

            assertThat(result.getDatumEingang()).isEqualTo(EINGANG);
            assertThat(result.getDatumRuecknahme()).isNull();
            assertThat(result.getDatumVorlageRegierung()).isNull();
            assertThat(result.getDatumAblehnungRegierung()).isNull();
            assertThat(result.getEntscheidungDurchfuehrung()).isNull();
            assertThat(result.isSollAbgesetzt()).isFalse();
            assertThat(result.isNeueTeilabrechnungAnlegen()).isFalse();
            assertThat(result.getBemerkung()).isNull();
        }
    }

    @Nested
    class ToWiderspruch {
        @Test
        void givenPersistedEntity_thenReturnItWithItsId() {
            final UUID widerspruchId = UUID.randomUUID();
            final UUID abrechnungId = UUID.randomUUID();
            final WiderspruchEntity entity = new WiderspruchEntity();
            entity.setId(widerspruchId);
            entity.setAbrechnungId(abrechnungId);
            entity.setDatumEingang(EINGANG);
            entity.setEntscheidungDurchfuehrung("Abrechnung wird abgesetzt");
            entity.setSollAbgesetzt(true);
            entity.setNeueTeilabrechnungAnlegen(false);
            entity.setBemerkung("Bemerkung");

            final Widerspruch result = widerspruchEntityMapper.toWiderspruch(entity);

            assertThat(result.id()).isEqualTo(widerspruchId);
            assertThat(result.abrechnungId()).isEqualTo(abrechnungId);
            assertThat(result.datumEingang()).isEqualTo(EINGANG);
            assertThat(result.entscheidungDurchfuehrung()).isEqualTo("Abrechnung wird abgesetzt");
            assertThat(result.sollAbgesetzt()).isTrue();
            assertThat(result.neueTeilabrechnungAnlegen()).isFalse();
            assertThat(result.bemerkung()).isEqualTo("Bemerkung");
        }
    }
}
