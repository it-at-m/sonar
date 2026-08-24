package de.muenchen.oss.sonar.backend.projekt.dto;

import de.muenchen.oss.sonar.backend.common.Zeitraum;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AbrechnungszeitraumOrdered.Validator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AbrechnungszeitraumOrdered {

    String message() default "Das Ende der Abrechnung darf nicht vor deren Beginn liegen.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<AbrechnungszeitraumOrdered, ProjektRequestDTO> {

        @Override
        public boolean isValid(final ProjektRequestDTO projekt, final ConstraintValidatorContext context) {
            return Zeitraum.isOrdered(projekt.abrechnungBeginn(), projekt.abrechnungEnde());
        }

    }

}
