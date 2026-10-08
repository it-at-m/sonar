package de.muenchen.oss.sonar.backend.geschaeftspartner;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

class GeschaeftspartnerPropertiesTest {

    private final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

    private final Validator validator = validatorFactory.getValidator();

    @Test
    void givenBlankProperties_thenRejectConfiguration() {
        final GeschaeftspartnerProperties properties = new GeschaeftspartnerProperties();
        properties.setUrl(" ");
        properties.setUsername(" ");
        properties.setPassword(" ");

        assertThat(validator.validate(properties))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("url", "username", "password");
    }

}
