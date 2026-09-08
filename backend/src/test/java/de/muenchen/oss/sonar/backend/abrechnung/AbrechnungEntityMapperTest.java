package de.muenchen.oss.sonar.backend.abrechnung;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class AbrechnungEntityMapperTest {

    private static final LocalDate VON = LocalDate.of(2026, 1, 1);
    private static final LocalDate BIS = LocalDate.of(2026, 3, 31);

    private final AbrechnungEntityMapper abrechnungEntityMapper = Mappers.getMapper(AbrechnungEntityMapper.class);

    @Nested
    class ToEntity {
        @Test
        void givenAbrechnung_thenDropTheIdsAndKeepTheChildren() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), true, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(),
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, Nutzung.NUTZUNG_A,
                    VON, BIS, null, "Bemerkung", List.of(position));
            final Abrechnung abrechnung = new Abrechnung(UUID.randomUUID(), UUID.randomUUID(), "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            final AbrechnungEntity result = abrechnungEntityMapper.toEntity(abrechnung);

            assertThat(result.getId()).isNull();
            assertThat(result.getProjektId()).isNotNull();
            assertThat(result.getNutzungsobjekte()).hasSize(1);

            final AbrechnungNutzungsobjektEntity nutzungsobjektEntity = result.getNutzungsobjekte().getFirst();
            assertThat(nutzungsobjektEntity.getId()).isNull();
            assertThat(nutzungsobjektEntity.getAdressdaten().getAdresse()).isEqualTo("Marienplatz");
            assertThat(nutzungsobjektEntity.getAdressdaten().getTageUnerlaubteNutzung()).isEqualTo(90);
            assertThat(nutzungsobjektEntity.getPositionen()).hasSize(1);

            final AbrechnungPositionEntity positionEntity = nutzungsobjektEntity.getPositionen().getFirst();
            assertThat(positionEntity.getId()).isNull();
            assertThat(positionEntity.getFlaeche()).isEqualByComparingTo("36.00");
            assertThat(positionEntity.isHaelfte()).isTrue();
        }
    }

    @Nested
    class ToAbrechnung {
        @Test
        void givenPersistedEntity_thenReturnItWithItsIds() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), true, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(),
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, Nutzung.NUTZUNG_A,
                    VON, BIS, null, "Bemerkung", List.of(position));
            final Abrechnung abrechnung = new Abrechnung(UUID.randomUUID(), UUID.randomUUID(), "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            final AbrechnungEntity entity = abrechnungEntityMapper.toEntity(abrechnung);
            final UUID abrechnungId = UUID.randomUUID();
            final UUID nutzungsobjektId = UUID.randomUUID();
            final UUID positionId = UUID.randomUUID();
            entity.setId(abrechnungId);
            entity.getNutzungsobjekte().getFirst().setId(nutzungsobjektId);
            entity.getNutzungsobjekte().getFirst().getPositionen().getFirst().setId(positionId);

            final Abrechnung result = abrechnungEntityMapper.toAbrechnung(entity);

            assertThat(result.id()).isEqualTo(abrechnungId);
            assertThat(result.nutzungsobjekte().getFirst().id()).isEqualTo(nutzungsobjektId);
            assertThat(result.nutzungsobjekte().getFirst().positionen().getFirst().id()).isEqualTo(positionId);
            assertThat(result.nutzungsobjekte().getFirst().positionen().getFirst().flaeche())
                    .isEqualByComparingTo("36.00");
        }
    }
}
