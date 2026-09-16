package de.muenchen.oss.sonar.backend.widerspruch.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Verifies the constraints of the request DTO. */
class WiderspruchRequestDTOTest {

    private static final LocalDate EINGANG = LocalDate.of(2026, 4, 1);

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
    class DatumEingang {
        @Test
        void givenDatumEingang_thenNoViolation() {
            final WiderspruchRequestDTO requestDTO = new WiderspruchRequestDTO(EINGANG, null, null, null, null, false,
                    false, null);

            assertThat(validator.validate(requestDTO)).isEmpty();
        }

        @Test
        void givenNoDatumEingang_thenViolation() {
            final WiderspruchRequestDTO requestDTO = new WiderspruchRequestDTO(null, null, null, null, null, false,
                    false, null);

            assertThat(validator.validate(requestDTO)).hasSize(1);
        }
    }

    @Nested
    class OptionalFields {
        @Test
        void givenEveryFieldFilled_thenNoViolation() {
            final WiderspruchRequestDTO requestDTO = new WiderspruchRequestDTO(EINGANG, LocalDate.of(2026, 4, 15),
                    LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 1), "Abrechnung wird durchgeführt", true, true,
                    "Bemerkung");

            assertThat(validator.validate(requestDTO)).isEmpty();
        }

        @Test
        void givenEntscheidungAboveTheColumn_thenViolation() {
            final WiderspruchRequestDTO requestDTO = new WiderspruchRequestDTO(EINGANG, null, null, null,
                    "x".repeat(256), false, false, null);

            assertThat(validator.validate(requestDTO)).hasSize(1);
        }

        @Test
        void givenBemerkungAboveTheColumn_thenViolation() {
            final WiderspruchRequestDTO requestDTO = new WiderspruchRequestDTO(EINGANG, null, null, null, null, false,
                    false, "x".repeat(10_001));

            assertThat(validator.validate(requestDTO)).hasSize(1);
        }

        @Test
        void givenBlankEntscheidung_thenViolation() {
            final WiderspruchRequestDTO requestDTO = new WiderspruchRequestDTO(EINGANG, null, null, null, "", false,
                    false, null);

            assertThat(validator.validate(requestDTO)).hasSize(1);
        }
    }
}
