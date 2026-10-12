package de.muenchen.oss.sonar.backend.abrechnung;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.AdressdatenEmbeddable;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import de.muenchen.oss.sonar.backend.widerspruch.WiderspruchEntity;
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
        void givenAbrechnung_thenDropTheIdAndLeaveTheChildrenToTheService() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(null,
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, List.of(Nutzung.BAUZAUN), null,
                    VON, BIS, null, "Bemerkung", true, List.of(position));
            final UUID projektId = UUID.randomUUID();
            final Abrechnung abrechnung = new Abrechnung(UUID.randomUUID(), projektId, 1, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            final AbrechnungEntity result = abrechnungEntityMapper.toEntity(abrechnung);

            assertThat(result.getId()).isNull();
            assertThat(result.getWiderspruch()).isNull();
            assertThat(result.getProjektId()).isEqualTo(projektId);
            assertThat(result.getGeschaeftspartnerId()).isEqualTo("1000000001");
            assertThat(result.getAbrechnungsArt()).isEqualTo(AbrechnungsArt.ENDABRECHNUNG);
            assertThat(result.getNutzungsobjekte()).isEmpty();
            assertThat(result.getPositionen()).isEmpty();
        }

        @Test
        void givenAbrechnung_thenLeaveThePlaceInTheChainOfVersionsUnset() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(null,
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, List.of(Nutzung.BAUZAUN), null,
                    VON, BIS, null, "Bemerkung", true, List.of(position));
            final Abrechnung abrechnung = new Abrechnung(UUID.randomUUID(), UUID.randomUUID(), 3, null, "1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));

            final AbrechnungEntity result = abrechnungEntityMapper.toEntity(abrechnung);

            assertThat(result.getVersionsnummer()).isNull();
            assertThat(result.getVorgaengerAbrechnungId()).isNull();
        }

        @Test
        void givenNutzungsobjekt_thenDropTheIdAndMoveTheAdressdatenIntoTheEmbeddable() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(),
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, List.of(Nutzung.BAUZAUN), null,
                    VON, BIS, null, "Bemerkung", true, List.of(position));

            final NutzungsobjektEntity result = abrechnungEntityMapper.toEntity(nutzungsobjekt);

            assertThat(result.getId()).isNull();
            assertThat(result.getAdressdaten().getArt()).isEqualTo(Adressart.ADRESSE);
            assertThat(result.getAdressdaten().getAdresse()).isEqualTo("Marienplatz");
            assertThat(result.getAdressdaten().getHausnummerVon()).isEqualTo("8");
            assertThat(result.getAdressdaten().getHausnummerBis()).isEqualTo("12");
            assertThat(result.getNutzungen()).containsExactly(Nutzung.BAUZAUN);
            assertThat(result.getAdressdaten().getTageUnerlaubteNutzung()).isEqualTo(90);
            assertThat(result.getBemerkung()).isEqualTo("Bemerkung");
            assertThat(result.isAufschlag50prozent()).isTrue();
        }

        @Test
        void givenPosition_thenDropTheIdAndLeaveTheNutzungsobjektToTheService() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));

            final AbrechnungPositionEntity result = abrechnungEntityMapper.toEntity(position);

            assertThat(result.getId()).isNull();
            assertThat(result.getNutzungsobjekt()).isNull();
            assertThat(result.getBeginn()).isEqualTo(VON);
            assertThat(result.getEnde()).isEqualTo(BIS);
            assertThat(result.getLaenge()).isEqualByComparingTo("12.00");
            assertThat(result.getBreite()).isEqualByComparingTo("3.00");
            assertThat(result.getFlaeche()).isEqualByComparingTo("36.00");
            assertThat(result.getAnteilAnFlaeche()).isEqualByComparingTo("30.00");
        }
    }

    @Nested
    class ToNutzungsobjekt {
        @Test
        void givenNutzungsobjektEntityAndPositionen_thenLiftTheAdressdatenUpAndKeepThePositionen() {
            final UUID nutzungsobjektId = UUID.randomUUID();
            final NutzungsobjektEntity nutzungsobjektEntity = new NutzungsobjektEntity();
            nutzungsobjektEntity.setId(nutzungsobjektId);
            nutzungsobjektEntity.setBemerkung("Bemerkung");
            nutzungsobjektEntity.setAufschlag50prozent(true);
            nutzungsobjektEntity.addNutzung(Nutzung.BAUZAUN);

            final AdressdatenEmbeddable adressdaten = nutzungsobjektEntity.getAdressdaten();
            adressdaten.setArt(Adressart.ADRESSE);
            adressdaten.setAdresse("Marienplatz");
            adressdaten.setHausnummerVon("8");
            adressdaten.setHausnummerBis("12");
            adressdaten.setUnerlaubteNutzungVon(VON);
            adressdaten.setUnerlaubteNutzungBis(BIS);
            adressdaten.setTageUnerlaubteNutzung(90);

            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));

            final AbrechnungNutzungsobjekt result = abrechnungEntityMapper.toNutzungsobjekt(nutzungsobjektEntity, List.of(position));

            assertThat(result.id()).isEqualTo(nutzungsobjektId);
            assertThat(result.art()).isEqualTo(Adressart.ADRESSE);
            assertThat(result.adresse()).isEqualTo("Marienplatz");
            assertThat(result.hausnummerVon()).isEqualTo("8");
            assertThat(result.hausnummerBis()).isEqualTo("12");
            assertThat(result.nutzungen()).containsExactly(Nutzung.BAUZAUN);
            assertThat(result.tageUnerlaubteNutzung()).isEqualTo(90);
            assertThat(result.bemerkung()).isEqualTo("Bemerkung");
            assertThat(result.aufschlag50prozent()).isTrue();
            assertThat(result.positionen()).containsExactly(position);
        }

        @Test
        void givenNutzungsobjektEntityWithoutPositionen_thenReturnItWithoutAny() {
            final NutzungsobjektEntity nutzungsobjektEntity = new NutzungsobjektEntity();
            nutzungsobjektEntity.setId(UUID.randomUUID());
            nutzungsobjektEntity.getAdressdaten().setArt(Adressart.ADRESSE);
            nutzungsobjektEntity.getAdressdaten().setAdresse("Marienplatz");

            final AbrechnungNutzungsobjekt result = abrechnungEntityMapper.toNutzungsobjekt(nutzungsobjektEntity, List.of());

            assertThat(result.adresse()).isEqualTo("Marienplatz");
            assertThat(result.positionen()).isEmpty();
        }
    }

    @Nested
    class ToAbrechnung {
        @Test
        void givenPersistedEntityAndNutzungsobjekte_thenReturnThemTogether() {
            final UUID abrechnungId = UUID.randomUUID();
            final UUID projektId = UUID.randomUUID();
            final UUID vorgaengerId = UUID.randomUUID();
            final AbrechnungEntity entity = new AbrechnungEntity();
            entity.setId(abrechnungId);
            entity.setProjektId(projektId);
            entity.setVersionsnummer(2);
            entity.setVorgaengerAbrechnungId(vorgaengerId);
            entity.setGeschaeftspartnerId("1000000001");
            entity.setZeitraumVon(VON);
            entity.setZeitraumBis(BIS);
            entity.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);

            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(),
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, List.of(Nutzung.BAUZAUN), null,
                    VON, BIS, null, "Bemerkung", true, List.of(position));

            final Abrechnung result = abrechnungEntityMapper.toAbrechnung(entity, false, List.of(nutzungsobjekt));

            assertThat(result.id()).isEqualTo(abrechnungId);
            assertThat(result.projektId()).isEqualTo(projektId);
            assertThat(result.versionsnummer()).isEqualTo(2);
            assertThat(result.vorgaengerAbrechnungId()).isEqualTo(vorgaengerId);
            assertThat(result.geschaeftspartnerId()).isEqualTo("1000000001");
            assertThat(result.abrechnungsArt()).isEqualTo(AbrechnungsArt.ENDABRECHNUNG);
            assertThat(result.nutzungsobjekte()).containsExactly(nutzungsobjekt);
        }

        @Test
        void givenWiderspruch_thenCarryItIntoTheAbrechnung() {
            final AbrechnungEntity entity = new AbrechnungEntity();
            entity.setId(UUID.randomUUID());
            entity.setProjektId(UUID.randomUUID());
            entity.setVersionsnummer(1);
            entity.setGeschaeftspartnerId("1000000001");
            entity.setZeitraumVon(VON);
            entity.setZeitraumBis(BIS);
            entity.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);

            assertThat(abrechnungEntityMapper.toAbrechnung(entity, false, List.of()).widerspruch()).isNull();
            assertThat(abrechnungEntityMapper.toAbrechnung(entity, false, List.of()).isWiderspruchVorhanden()).isFalse();

            final UUID widerspruchId = UUID.randomUUID();
            final WiderspruchEntity widerspruch = new WiderspruchEntity();
            widerspruch.setId(widerspruchId);
            widerspruch.setDatumEingang(LocalDate.of(2026, 4, 1));
            widerspruch.setBemerkung("Widerspruch der Eigentümerin");
            entity.setWiderspruch(widerspruch);

            final Abrechnung result = abrechnungEntityMapper.toAbrechnung(entity, false, List.of());

            assertThat(result.isWiderspruchVorhanden()).isTrue();
            assertThat(result.widerspruch().id()).isEqualTo(widerspruchId);
            assertThat(result.widerspruch().datumEingang()).isEqualTo(LocalDate.of(2026, 4, 1));
            assertThat(result.widerspruch().bemerkung()).isEqualTo("Widerspruch der Eigentümerin");
        }

        @Test
        void givenNeuereVersionVorhanden_thenCarryTheFlagIntoTheAbrechnung() {
            final AbrechnungEntity entity = new AbrechnungEntity();
            entity.setId(UUID.randomUUID());
            entity.setProjektId(UUID.randomUUID());
            entity.setVersionsnummer(1);
            entity.setGeschaeftspartnerId("1000000001");
            entity.setZeitraumVon(VON);
            entity.setZeitraumBis(BIS);
            entity.setAbrechnungsArt(AbrechnungsArt.ENDABRECHNUNG);

            assertThat(abrechnungEntityMapper.toAbrechnung(entity, true, List.of()).neuereVersionVorhanden()).isTrue();
            assertThat(abrechnungEntityMapper.toAbrechnung(entity, false, List.of()).neuereVersionVorhanden()).isFalse();
        }
    }
}
