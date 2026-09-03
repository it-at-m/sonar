package de.muenchen.oss.sonar.backend.abrechnung.dto;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungsArt;
import de.muenchen.oss.sonar.backend.abrechnung.ZustellungsbevollmaechtigterTyp;
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

class AbrechnungDTOMapperTest {

    private static final LocalDate VON = LocalDate.of(2026, 1, 1);
    private static final LocalDate BIS = LocalDate.of(2026, 3, 31);

    private final AbrechnungDTOMapper abrechnungDTOMapper = Mappers.getMapper(AbrechnungDTOMapper.class);

    @Nested
    class ToDTO {
        @Test
        void givenAbrechnung_thenReturnsCorrectDTO() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), true, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(),
                    Adressart.FLURSTUECK, null, null, null, "1234/5", "Sendling", Nutzung.NUTZUNG_B,
                    VON, BIS, null, "Bemerkung", List.of(position));
            final Abrechnung abrechnung = new Abrechnung(UUID.randomUUID(), UUID.randomUUID(), "1000000001", true,
                    "2000000002", ZustellungsbevollmaechtigterTyp.VORMUND, VON, BIS, AbrechnungsArt.ZWISCHENABRECHNUNG,
                    List.of(nutzungsobjekt));

            final AbrechnungResponseDTO result = abrechnungDTOMapper.toDTO(abrechnung);

            assertThat(result.id()).isEqualTo(abrechnung.id());
            assertThat(result.projektId()).isEqualTo(abrechnung.projektId());
            assertThat(result.geschaeftspartnerId()).isEqualTo("1000000001");
            assertThat(result.zustellungsbevollmaechtigterGenutzt()).isTrue();
            assertThat(result.zustellungsbevollmaechtigterId()).isEqualTo("2000000002");
            assertThat(result.zustellungsbevollmaechtigterTyp()).isEqualTo(ZustellungsbevollmaechtigterTyp.VORMUND);
            assertThat(result.zeitraumVon()).isEqualTo(VON);
            assertThat(result.zeitraumBis()).isEqualTo(BIS);
            assertThat(result.abrechnungsArt()).isEqualTo(AbrechnungsArt.ZWISCHENABRECHNUNG);
            assertThat(result.nutzungsobjekte()).hasSize(1);

            final AbrechnungNutzungsobjektResponseDTO nutzungsobjektDTO = result.nutzungsobjekte().getFirst();
            assertThat(nutzungsobjektDTO.id()).isEqualTo(nutzungsobjekt.id());
            assertThat(nutzungsobjektDTO.art()).isEqualTo(Adressart.FLURSTUECK);
            assertThat(nutzungsobjektDTO.flurstueck()).isEqualTo("1234/5");
            assertThat(nutzungsobjektDTO.gemarkung()).isEqualTo("Sendling");
            assertThat(nutzungsobjektDTO.nutzung()).isEqualTo(Nutzung.NUTZUNG_B);
            assertThat(nutzungsobjektDTO.tageUnerlaubteNutzung()).isEqualTo(90);
            assertThat(nutzungsobjektDTO.bemerkung()).isEqualTo("Bemerkung");
            assertThat(nutzungsobjektDTO.positionen()).hasSize(1);

            final AbrechnungPositionResponseDTO positionDTO = nutzungsobjektDTO.positionen().getFirst();
            assertThat(positionDTO.id()).isEqualTo(position.id());
            assertThat(positionDTO.laenge()).isEqualByComparingTo("12.00");
            assertThat(positionDTO.breite()).isEqualByComparingTo("3.00");
            assertThat(positionDTO.flaeche()).isEqualByComparingTo("36.00");
            assertThat(positionDTO.haelfte()).isTrue();
            assertThat(positionDTO.anteilAnFlaeche()).isEqualByComparingTo("30.00");
        }
    }

    @Nested
    class ToAbrechnung {
        @Test
        void givenRequestDTO_thenTakeTheProjektFromThePath() {
            final UUID projektId = UUID.randomUUID();
            final AbrechnungPositionRequestDTO positionDTO = new AbrechnungPositionRequestDTO(
                    VON, BIS, new BigDecimal("12.00"), new BigDecimal("3.00"), new BigDecimal("36.00"), false,
                    new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjektDTO = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, Nutzung.NUTZUNG_A,
                    null, null, 12, null, List.of(positionDTO));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null,
                    VON, BIS, AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjektDTO));

            final Abrechnung result = abrechnungDTOMapper.toAbrechnung(projektId, requestDTO);

            assertThat(result.id()).isNull();
            assertThat(result.projektId()).isEqualTo(projektId);
            assertThat(result.geschaeftspartnerId()).isEqualTo("1000000001");
            assertThat(result.nutzungsobjekte()).hasSize(1);

            final AbrechnungNutzungsobjekt nutzungsobjekt = result.nutzungsobjekte().getFirst();
            assertThat(nutzungsobjekt.id()).isNull();
            assertThat(nutzungsobjekt.adresse()).isEqualTo("Marienplatz");
            assertThat(nutzungsobjekt.hausnummerVon()).isEqualTo("8");
            assertThat(nutzungsobjekt.hausnummerBis()).isEqualTo("12");
            assertThat(nutzungsobjekt.tageUnerlaubteNutzung()).isEqualTo(12);
            assertThat(nutzungsobjekt.positionen()).hasSize(1);
        }

        @Test
        void givenPosition_thenKeepTheFlaecheAsGiven() {
            final AbrechnungPositionRequestDTO positionDTO = new AbrechnungPositionRequestDTO(
                    VON, BIS, new BigDecimal("1.15"), new BigDecimal("0.10"), new BigDecimal("5.00"), false,
                    BigDecimal.ZERO);

            final AbrechnungPosition result = abrechnungDTOMapper.toAbrechnungPosition(positionDTO);

            assertThat(result.flaeche()).isEqualByComparingTo("5.00");
        }
    }
}
