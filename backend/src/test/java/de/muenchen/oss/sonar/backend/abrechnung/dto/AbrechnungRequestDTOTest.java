package de.muenchen.oss.sonar.backend.abrechnung.dto;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungsArt;
import de.muenchen.oss.sonar.backend.abrechnung.ZustellungsbevollmaechtigterTyp;
import de.muenchen.oss.sonar.backend.common.Adressart;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Verifies the constraints of the request DTOs, above all the cross-field ones which cannot be
 * expressed on a single field.
 */
class AbrechnungRequestDTOTest {

    private static final LocalDate VON = LocalDate.of(2026, 1, 1);
    private static final LocalDate BIS = LocalDate.of(2026, 3, 31);

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void initValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    private static Set<String> messagesOf(final AbrechnungRequestDTO requestDTO) {
        return validator.validate(requestDTO).stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }

    @Nested
    class Zeitraum {
        @Test
        void givenBisAfterVon_thenNoViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }

        @Test
        void givenBisBeforeVon_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, BIS, VON,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(messagesOf(requestDTO)).containsExactly("Das Ende des Zeitraums darf nicht vor dessen Beginn liegen.");
        }
    }

    @Nested
    class Zustellungsbevollmaechtigter {
        @Test
        void givenGenutztWithIdAndTyp_thenNoViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", true, "2000000002",
                    ZustellungsbevollmaechtigterTyp.VORMUND, VON, BIS, AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }

        @Test
        void givenGenutztWithoutIdAndTyp_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", true, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(messagesOf(requestDTO)).containsExactly("Zu einem Zustellungsbevollmächtigten sind ID und Typ anzugeben.");
        }

        @Test
        void givenLeftoverIdWithoutGenutzt_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, "2000000002", null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(messagesOf(requestDTO)).containsExactly("Ohne Zustellungsbevollmächtigten sind weder ID noch Typ anzugeben.");
        }
    }

    @Nested
    class AdressartAdresse {
        @Test
        void givenAdresseWithHausnummer_thenNoViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }

        @Test
        void givenAdresseWithoutHausnummer_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", null, null, null, null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(messagesOf(requestDTO)).containsExactly("Zu einer Adresse sind Adresse und Hausnummer von anzugeben.");
        }

        @Test
        void givenAdresseWithGemarkung_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, "Sendling", null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(messagesOf(requestDTO)).containsExactly("Zu einer Adresse sind weder Flurstück noch Gemarkung anzugeben.");
        }

        @Test
        void givenAdresseWithHausnummerSpan_thenNoViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }
    }

    @Nested
    class AdressartFlurstueck {
        @Test
        void givenFlurstueckWithGemarkung_thenNoViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.FLURSTUECK, null, null, null, "1234/5", "Sendling", null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }

        @Test
        void givenFlurstueckWithoutGemarkung_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.FLURSTUECK, null, null, null, "1234/5", null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(messagesOf(requestDTO)).containsExactly("Zu einem Flurstück sind Flurstück und Gemarkung anzugeben.");
        }

        @Test
        void givenFlurstueckWithAdresse_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.FLURSTUECK, "Marienplatz", null, null, "1234/5", "Sendling", null, null, null, null, null,
                    List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(messagesOf(requestDTO)).containsExactly("Zu einem Flurstück sind weder Adresse noch Hausnummern anzugeben.");
        }
    }

    @Nested
    class UnerlaubteNutzung {
        @Test
        void givenOnlyBeginn_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, VON, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(messagesOf(requestDTO)).containsExactly("Der Zeitraum der unerlaubten Nutzung ist mit Beginn und Ende anzugeben.");
        }

        @Test
        void givenZeitraumAndTage_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, VON, BIS, 12, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(messagesOf(requestDTO))
                    .containsExactly("Bitte entweder den Zeitraum oder die Anzahl der Tage der unerlaubten Nutzung angeben.");
        }
    }

    @Nested
    class Positionen {
        @Test
        void givenNoPositionen_thenViolation() {
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of());
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(validator.validate(requestDTO)).hasSize(1);
        }

        @Test
        void givenInvertedPositionZeitraum_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(BIS, VON, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(messagesOf(requestDTO)).containsExactly("Das Ende einer Position darf nicht vor deren Beginn liegen.");
        }

        @Test
        void givenLaengeOfZero_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, BigDecimal.ZERO,
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(validator.validate(requestDTO)).hasSize(1);
        }

        @Test
        void givenFlaecheOfZero_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), BigDecimal.ZERO, false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(validator.validate(requestDTO)).hasSize(1);
        }

        @Test
        void givenAnteilAnFlaecheOfZero_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("12.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, BigDecimal.ZERO);
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(validator.validate(requestDTO)).hasSize(1);
        }

        @Test
        void givenLaengeAboveTheColumn_thenViolation() {
            final AbrechnungPositionRequestDTO position = new AbrechnungPositionRequestDTO(VON, BIS, new BigDecimal("10000000000.00"),
                    new BigDecimal("3.00"), new BigDecimal("36.00"), false, new BigDecimal("30.00"));
            final AbrechnungNutzungsobjektRequestDTO nutzungsobjekt = new AbrechnungNutzungsobjektRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of(nutzungsobjekt));

            assertThat(validator.validate(requestDTO)).hasSize(1);
        }
    }

    @Nested
    class Nutzungsobjekte {
        @Test
        void givenNoNutzungsobjekte_thenViolation() {
            final AbrechnungRequestDTO requestDTO = new AbrechnungRequestDTO("1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, List.of());

            assertThat(validator.validate(requestDTO)).hasSize(1);
        }
    }
}
