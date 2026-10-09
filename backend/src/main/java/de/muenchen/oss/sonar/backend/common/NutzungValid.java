package de.muenchen.oss.sonar.backend.common;

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
@Constraint(validatedBy = NutzungValid.Validator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NutzungValid {

    String message() default "Die Angaben zur Nutzung sind nicht gültig.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<NutzungValid, Nutzungsangabe> {

        private static final String SONSTIGES_LEFTOVER = "Eine Beschreibung der Nutzung ist nur zu Sonstiges anzugeben.";

        private static final String SONSTIGES = "nutzungSonstiges";

        @Override
        public boolean isValid(final Nutzungsangabe nutzungsangabe, final ConstraintValidatorContext context) {
            context.disableDefaultConstraintViolation();

            if (!nutzungsangabe.nutzungen().contains(Nutzung.SONSTIGES) && nutzungsangabe.nutzungSonstiges() != null) {
                context.buildConstraintViolationWithTemplate(SONSTIGES_LEFTOVER).addPropertyNode(SONSTIGES).addConstraintViolation();
                return false;
            }

            return true;
        }

    }

}
