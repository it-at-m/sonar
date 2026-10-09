package de.muenchen.oss.sonar.backend.abrechnung;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.widerspruch.WiderspruchEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(null,
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, Nutzung.NUTZUNG_A,
                    VON, BIS, null, "Bemerkung", true, List.of(position));
            final Abrechnung abrechnung = new Abrechnung(UUID.randomUUID(), UUID.randomUUID(), 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            final AbrechnungEntity result = abrechnungEntityMapper.toEntity(abrechnung, Map.of());

            assertThat(result.getId()).isNull();
            assertThat(result.getWiderspruch()).isNull();
            assertThat(result.getProjektId()).isNotNull();
            assertThat(result.getNutzungsobjekte()).hasSize(1);

            final NutzungsobjektEntity nutzungsobjektEntity = result.getNutzungsobjekte().getFirst();
            assertThat(nutzungsobjektEntity.getId()).isNull();
            assertThat(nutzungsobjektEntity.getAdressdaten().getAdresse()).isEqualTo("Marienplatz");
            assertThat(nutzungsobjektEntity.getAdressdaten().getTageUnerlaubteNutzung()).isEqualTo(90);
            assertThat(nutzungsobjektEntity.isAufschlag50prozent()).isTrue();
            assertThat(result.getPositionen()).hasSize(1);

            final AbrechnungPositionEntity positionEntity = result.getPositionen().getFirst();
            assertThat(positionEntity.getId()).isNull();
            assertThat(positionEntity.getFlaeche()).isEqualByComparingTo("36.00");
            assertThat(positionEntity.getNutzungsobjekt()).isSameAs(nutzungsobjektEntity);
        }

        @Test
        void givenUebernommenesNutzungsobjekt_thenBillTheStoredOneInsteadOfACopy() {
            final UUID nutzungsobjektId = UUID.randomUUID();
            final NutzungsobjektEntity gespeichertesNutzungsobjekt = new NutzungsobjektEntity();
            gespeichertesNutzungsobjekt.setId(nutzungsobjektId);
            gespeichertesNutzungsobjekt.getAdressdaten().setArt(Adressart.ADRESSE);
            gespeichertesNutzungsobjekt.getAdressdaten().setAdresse("Marienplatz");

            final AbrechnungPosition position = new AbrechnungPosition(null, VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(nutzungsobjektId,
                    Adressart.ADRESSE, "Sendlinger Straße", "1", null, null, null, Nutzung.NUTZUNG_B,
                    null, null, null, "Wird ignoriert", false, List.of(position));
            final Abrechnung abrechnung = new Abrechnung(null, UUID.randomUUID(), 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            final AbrechnungEntity result = abrechnungEntityMapper.toEntity(abrechnung,
                    Map.of(nutzungsobjektId, gespeichertesNutzungsobjekt));

            assertThat(result.getNutzungsobjekte()).containsExactly(gespeichertesNutzungsobjekt);
            assertThat(result.getNutzungsobjekte().getFirst().getAdressdaten().getAdresse()).isEqualTo("Marienplatz");
            assertThat(result.getPositionen()).hasSize(1);
            assertThat(result.getPositionen().getFirst().getNutzungsobjekt()).isSameAs(gespeichertesNutzungsobjekt);
        }

        @Test
        void givenAbrechnung_thenLeaveThePlaceInTheChainOfVersionsUnset() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(null,
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, Nutzung.NUTZUNG_A,
                    VON, BIS, null, "Bemerkung", true, List.of(position));
            final Abrechnung abrechnung = new Abrechnung(UUID.randomUUID(), UUID.randomUUID(), 3, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            final AbrechnungEntity result = abrechnungEntityMapper.toEntity(abrechnung, Map.of());

            assertThat(result.getVersionsnummer()).isNull();
            assertThat(result.getVorgaengerAbrechnungId()).isNull();
        }
    }

    @Nested
    class ToAbrechnung {
        @Test
        void givenPersistedEntity_thenReturnItWithItsIds() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(null,
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, Nutzung.NUTZUNG_A,
                    VON, BIS, null, "Bemerkung", true, List.of(position));
            final Abrechnung abrechnung = new Abrechnung(UUID.randomUUID(), UUID.randomUUID(), 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            final AbrechnungEntity entity = abrechnungEntityMapper.toEntity(abrechnung, Map.of());
            final UUID abrechnungId = UUID.randomUUID();
            final UUID vorgaengerId = UUID.randomUUID();
            final UUID nutzungsobjektId = UUID.randomUUID();
            final UUID positionId = UUID.randomUUID();
            entity.setId(abrechnungId);
            entity.setVersionsnummer(2);
            entity.setVorgaengerAbrechnungId(vorgaengerId);
            entity.getNutzungsobjekte().getFirst().setId(nutzungsobjektId);
            entity.getPositionen().getFirst().setId(positionId);

            final Abrechnung result = abrechnungEntityMapper.toAbrechnung(entity, false);

            assertThat(result.id()).isEqualTo(abrechnungId);
            assertThat(result.versionsnummer()).isEqualTo(2);
            assertThat(result.vorgaengerAbrechnungId()).isEqualTo(vorgaengerId);
            assertThat(result.nutzungsobjekte().getFirst().id()).isEqualTo(nutzungsobjektId);
            assertThat(result.nutzungsobjekte().getFirst().positionen().getFirst().id()).isEqualTo(positionId);
            assertThat(result.nutzungsobjekte().getFirst().positionen().getFirst().flaeche())
                    .isEqualByComparingTo("36.00");
        }

        @Test
        void givenSeveralNutzungsobjekte_thenShowEachOfThemOnlyItsOwnPositionen() {
            final UUID erstesNutzungsobjektId = UUID.randomUUID();
            final NutzungsobjektEntity erstesNutzungsobjekt = new NutzungsobjektEntity();
            erstesNutzungsobjekt.setId(erstesNutzungsobjektId);
            erstesNutzungsobjekt.getAdressdaten().setArt(Adressart.ADRESSE);
            erstesNutzungsobjekt.getAdressdaten().setAdresse("Marienplatz");

            final UUID zweitesNutzungsobjektId = UUID.randomUUID();
            final NutzungsobjektEntity zweitesNutzungsobjekt = new NutzungsobjektEntity();
            zweitesNutzungsobjekt.setId(zweitesNutzungsobjektId);
            zweitesNutzungsobjekt.getAdressdaten().setArt(Adressart.ADRESSE);
            zweitesNutzungsobjekt.getAdressdaten().setAdresse("Sendlinger Straße");

            final AbrechnungPositionEntity positionAmMarienplatz = new AbrechnungPositionEntity();
            positionAmMarienplatz.setNutzungsobjekt(erstesNutzungsobjekt);
            positionAmMarienplatz.setBeginn(VON);
            positionAmMarienplatz.setEnde(BIS);
            positionAmMarienplatz.setLaenge(new BigDecimal("12.00"));
            positionAmMarienplatz.setBreite(new BigDecimal("3.00"));
            positionAmMarienplatz.setFlaeche(new BigDecimal("36.00"));
            positionAmMarienplatz.setAnteilAnFlaeche(new BigDecimal("30.00"));

            final AbrechnungPositionEntity positionInDerSendlingerStrasse = new AbrechnungPositionEntity();
            positionInDerSendlingerStrasse.setNutzungsobjekt(zweitesNutzungsobjekt);
            positionInDerSendlingerStrasse.setBeginn(VON);
            positionInDerSendlingerStrasse.setEnde(BIS);
            positionInDerSendlingerStrasse.setLaenge(new BigDecimal("15.00"));
            positionInDerSendlingerStrasse.setBreite(new BigDecimal("3.00"));
            positionInDerSendlingerStrasse.setFlaeche(new BigDecimal("45.00"));
            positionInDerSendlingerStrasse.setAnteilAnFlaeche(new BigDecimal("45.00"));

            final AbrechnungEntity entity = new AbrechnungEntity();
            entity.setId(UUID.randomUUID());
            entity.setProjektId(UUID.randomUUID());
            entity.setVersionsnummer(1);
            entity.setGeschaeftspartnerId("1000000001");
            entity.setZeitraumVon(VON);
            entity.setZeitraumBis(BIS);
            entity.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);
            entity.addNutzungsobjekt(erstesNutzungsobjekt);
            entity.addNutzungsobjekt(zweitesNutzungsobjekt);
            entity.addPosition(positionAmMarienplatz);
            entity.addPosition(positionInDerSendlingerStrasse);

            final Abrechnung result = abrechnungEntityMapper.toAbrechnung(entity, false);

            assertThat(result.nutzungsobjekte()).hasSize(2);
            assertThat(result.nutzungsobjekte().getFirst().id()).isEqualTo(erstesNutzungsobjektId);
            assertThat(result.nutzungsobjekte().getFirst().positionen())
                    .extracting(AbrechnungPosition::flaeche)
                    .containsExactly(new BigDecimal("36.00"));
            assertThat(result.nutzungsobjekte().getLast().id()).isEqualTo(zweitesNutzungsobjektId);
            assertThat(result.nutzungsobjekte().getLast().positionen())
                    .extracting(AbrechnungPosition::flaeche)
                    .containsExactly(new BigDecimal("45.00"));
        }

        @Test
        void givenWiderspruch_thenCarryItIntoTheAbrechnung() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(null,
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, Nutzung.NUTZUNG_A,
                    VON, BIS, null, "Bemerkung", true, List.of(position));
            final Abrechnung abrechnung = new Abrechnung(UUID.randomUUID(), UUID.randomUUID(), 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));
            final AbrechnungEntity entity = abrechnungEntityMapper.toEntity(abrechnung, Map.of());
            entity.getNutzungsobjekte().getFirst().setId(UUID.randomUUID());

            assertThat(abrechnungEntityMapper.toAbrechnung(entity, false).widerspruch()).isNull();
            assertThat(abrechnungEntityMapper.toAbrechnung(entity, false).isWiderspruchVorhanden()).isFalse();

            final UUID widerspruchId = UUID.randomUUID();
            final WiderspruchEntity widerspruch = new WiderspruchEntity();
            widerspruch.setId(widerspruchId);
            widerspruch.setDatumEingang(LocalDate.of(2026, 4, 1));
            widerspruch.setBemerkung("Widerspruch der Eigentümerin");
            entity.setWiderspruch(widerspruch);

            final Abrechnung result = abrechnungEntityMapper.toAbrechnung(entity, false);

            assertThat(result.isWiderspruchVorhanden()).isTrue();
            assertThat(result.widerspruch().id()).isEqualTo(widerspruchId);
            assertThat(result.widerspruch().datumEingang()).isEqualTo(LocalDate.of(2026, 4, 1));
            assertThat(result.widerspruch().bemerkung()).isEqualTo("Widerspruch der Eigentümerin");
        }

        @Test
        void givenNeuereVersionVorhanden_thenCarryTheFlagIntoTheAbrechnung() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(null,
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, Nutzung.NUTZUNG_A,
                    VON, BIS, null, "Bemerkung", true, List.of(position));
            final Abrechnung abrechnung = new Abrechnung(UUID.randomUUID(), UUID.randomUUID(), 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));
            final AbrechnungEntity entity = abrechnungEntityMapper.toEntity(abrechnung, Map.of());
            entity.getNutzungsobjekte().getFirst().setId(UUID.randomUUID());

            assertThat(abrechnungEntityMapper.toAbrechnung(entity, true).neuereVersionVorhanden()).isTrue();
            assertThat(abrechnungEntityMapper.toAbrechnung(entity, false).neuereVersionVorhanden()).isFalse();
        }
    }
}
