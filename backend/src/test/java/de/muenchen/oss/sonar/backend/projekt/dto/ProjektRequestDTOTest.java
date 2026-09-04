package de.muenchen.oss.sonar.backend.projekt.dto;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
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
class ProjektRequestDTOTest {

    private static final LocalDate BEGINN = LocalDate.of(2026, 1, 1);
    private static final LocalDate ENDE = LocalDate.of(2026, 3, 31);

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

    @Nested
    class Abrechnungszeitraum {
        @Test
        void givenEndeAfterBeginn_thenNoViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A, null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }

        @Test
        void givenEndeEqualToBeginn_thenNoViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A, null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, BEGINN, List.of(adresseDTO));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }

        @Test
        void givenEndeBeforeBeginn_thenViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A, null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", ENDE, BEGINN, List.of(adresseDTO));

            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(requestDTO);

            assertThat(violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet()))
                    .containsExactly("Das Ende der Abrechnung darf nicht vor deren Beginn liegen.");
        }
    }

    @Nested
    class AdressartAdresse {
        @Test
        void givenAdresseWithHausnummer_thenNoViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A, null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }

        @Test
        void givenAdresseWithoutHausnummer_thenViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", null, null, null, null, null, null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(requestDTO);

            assertThat(violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet()))
                    .containsExactly("Zu einer Adresse sind Adresse und Hausnummer von anzugeben.");
        }

        @Test
        void givenAdresseWithGemarkung_thenViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, "Sendling", null, null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(requestDTO);

            assertThat(violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet()))
                    .containsExactly("Zu einer Adresse sind weder Flurstück noch Gemarkung anzugeben.");
        }

        @Test
        void givenAdresseWithHausnummerSpan_thenNoViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", "12", null, null, null, null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }
    }

    @Nested
    class AdressartFlurstueck {
        @Test
        void givenFlurstueckWithGemarkung_thenNoViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.FLURSTUECK, null, null, null, "1234/5", "Sendling", null, null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }

        @Test
        void givenFlurstueckWithoutGemarkung_thenViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.FLURSTUECK, null, null, null, "1234/5", null, null, null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(requestDTO);

            assertThat(violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet()))
                    .containsExactly("Zu einem Flurstück sind Flurstück und Gemarkung anzugeben.");
        }

        @Test
        void givenFlurstueckWithAdresse_thenViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.FLURSTUECK, "Marienplatz", null, null, "1234/5", "Sendling", null, null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(requestDTO);

            assertThat(violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet()))
                    .containsExactly("Zu einem Flurstück sind weder Adresse noch Hausnummern anzugeben.");
        }
    }

    @Nested
    class UnerlaubteNutzung {
        @Test
        void givenCompleteZeitraum_thenNoViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A, BEGINN, ENDE, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }

        @Test
        void givenOnlyBeginn_thenViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A, BEGINN, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(requestDTO);

            assertThat(violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet()))
                    .containsExactly("Der Zeitraum der unerlaubten Nutzung ist mit Beginn und Ende anzugeben.");
        }

        @Test
        void givenOnlyEnde_thenViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A, null, ENDE, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(requestDTO);

            assertThat(violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet()))
                    .containsExactly("Der Zeitraum der unerlaubten Nutzung ist mit Beginn und Ende anzugeben.");
        }

        @Test
        void givenInvertedZeitraum_thenViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A, ENDE, BEGINN, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(requestDTO);

            assertThat(violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet()))
                    .containsExactly("Das Ende der unerlaubten Nutzung darf nicht vor deren Beginn liegen.");
        }

        @Test
        void givenInvertedZeitraum_thenViolationOnBis() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A, ENDE, BEGINN, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(requestDTO);

            assertThat(violations).singleElement()
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .isEqualTo("adressen[0].unerlaubteNutzungBis");
        }
    }

    @Nested
    class TageUnerlaubteNutzung {
        @Test
        void givenOnlyTage_thenNoViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A, null, null, 12, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }

        @Test
        void givenZeitraumAndTage_thenViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A, BEGINN, ENDE, 12, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(requestDTO);

            assertThat(violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet()))
                    .containsExactly("Bitte entweder den Zeitraum oder die Anzahl der Tage der unerlaubten Nutzung angeben.");
        }

        @Test
        void givenZeroTage_thenViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A, null, null, 0, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            assertThat(validator.validate(requestDTO)).hasSize(1);
        }

        @Test
        void givenNeitherZeitraumNorTage_thenNoViolation() {
            final ProjektAdresseRequestDTO adresseDTO = new ProjektAdresseRequestDTO(
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, Nutzung.NUTZUNG_A, null, null, null, 0, false);
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of(adresseDTO));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }
    }

    @Nested
    class Adressen {
        @Test
        void givenNoAdressen_thenViolation() {
            final ProjektRequestDTO requestDTO = new ProjektRequestDTO("2026-0001", BEGINN, ENDE, List.of());

            assertThat(validator.validate(requestDTO)).hasSize(1);
        }
    }
}
