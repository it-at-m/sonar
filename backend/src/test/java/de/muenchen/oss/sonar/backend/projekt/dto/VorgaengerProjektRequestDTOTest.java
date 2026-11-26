package de.muenchen.oss.sonar.backend.projekt.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class VorgaengerProjektRequestDTOTest {

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
    class VorgaengerProjektId {
        @Test
        void givenVorgaengerProjektId_thenNoViolation() {
            final VorgaengerProjektRequestDTO requestDTO = new VorgaengerProjektRequestDTO(
                    UUID.fromString("123e4567-e89b-12d3-a456-426614174001"));

            assertThat(validator.validate(requestDTO)).isEmpty();
        }

        @Test
        void givenNoVorgaengerProjektId_thenViolation() {
            final VorgaengerProjektRequestDTO requestDTO = new VorgaengerProjektRequestDTO(null);

            assertThat(validator.validate(requestDTO)).hasSize(1);
        }
    }
}
