package de.muenchen.oss.sonar.backend.projekt.dto;

import static org.assertj.core.api.Assertions.assertThat;

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

    private static ProjektAdresseRequestDTO adresse(final LocalDate von, final LocalDate bis) {
        return adresse(von, bis, null);
    }

    private static ProjektAdresseRequestDTO adresse(final LocalDate von, final LocalDate bis, final Integer tage) {
        return new ProjektAdresseRequestDTO("Marienplatz 8", "Gastronomie", von, bis, tage, 0, false);
    }

    private static ProjektRequestDTO projekt(final LocalDate beginn, final LocalDate ende, final ProjektAdresseRequestDTO adresse) {
        return new ProjektRequestDTO("2026-0001", beginn, ende, List.of(adresse));
    }

    private static Set<String> messagesOf(final Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }

    @Nested
    class Abrechnungszeitraum {
        @Test
        void givenEndeAfterBeginn_thenNoViolation() {
            assertThat(validator.validate(projekt(BEGINN, ENDE, adresse(null, null)))).isEmpty();
        }

        @Test
        void givenEndeEqualToBeginn_thenNoViolation() {
            assertThat(validator.validate(projekt(BEGINN, BEGINN, adresse(null, null)))).isEmpty();
        }

        @Test
        void givenEndeBeforeBeginn_thenViolation() {
            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(projekt(ENDE, BEGINN, adresse(null, null)));

            assertThat(messagesOf(violations)).containsExactly("Das Ende der Abrechnung darf nicht vor deren Beginn liegen.");
        }
    }

    @Nested
    class UnerlaubteNutzung {
        @Test
        void givenCompleteZeitraum_thenNoViolation() {
            assertThat(validator.validate(projekt(BEGINN, ENDE, adresse(BEGINN, ENDE)))).isEmpty();
        }

        @Test
        void givenOnlyBeginn_thenViolation() {
            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(projekt(BEGINN, ENDE, adresse(BEGINN, null)));

            assertThat(messagesOf(violations)).containsExactly("Der Zeitraum der unerlaubten Nutzung ist mit Beginn und Ende anzugeben.");
        }

        @Test
        void givenOnlyEnde_thenViolation() {
            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(projekt(BEGINN, ENDE, adresse(null, ENDE)));

            assertThat(messagesOf(violations)).containsExactly("Der Zeitraum der unerlaubten Nutzung ist mit Beginn und Ende anzugeben.");
        }

        @Test
        void givenInvertedZeitraum_thenViolation() {
            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(projekt(BEGINN, ENDE, adresse(ENDE, BEGINN)));

            assertThat(messagesOf(violations)).containsExactly("Das Ende der unerlaubten Nutzung darf nicht vor deren Beginn liegen.");
        }

        @Test
        void givenInvertedZeitraum_thenViolationOnBis() {
            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(projekt(BEGINN, ENDE, adresse(ENDE, BEGINN)));

            assertThat(violations).singleElement()
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .isEqualTo("adressen[0].unerlaubteNutzungBis");
        }
    }

    @Nested
    class TageUnerlaubteNutzung {
        @Test
        void givenOnlyTage_thenNoViolation() {
            assertThat(validator.validate(projekt(BEGINN, ENDE, adresse(null, null, 12)))).isEmpty();
        }

        @Test
        void givenZeitraumAndTage_thenViolation() {
            final Set<ConstraintViolation<ProjektRequestDTO>> violations = validator.validate(
                    projekt(BEGINN, ENDE, adresse(BEGINN, ENDE, 12)));

            assertThat(messagesOf(violations))
                    .containsExactly("Bitte entweder den Zeitraum oder die Anzahl der Tage der unerlaubten Nutzung angeben.");
        }

        @Test
        void givenZeroTage_thenViolation() {
            assertThat(validator.validate(projekt(BEGINN, ENDE, adresse(null, null, 0)))).hasSize(1);
        }

        @Test
        void givenNeitherZeitraumNorTage_thenNoViolation() {
            assertThat(validator.validate(projekt(BEGINN, ENDE, adresse(null, null, null)))).isEmpty();
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
