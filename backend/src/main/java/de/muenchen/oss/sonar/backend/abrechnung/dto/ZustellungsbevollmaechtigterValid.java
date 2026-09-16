package de.muenchen.oss.sonar.backend.abrechnung.dto;

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
@Constraint(validatedBy = ZustellungsbevollmaechtigterValid.Validator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ZustellungsbevollmaechtigterValid {

    String message() default "Die Angaben zum Zustellungsbevollmächtigten sind nicht gültig.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ZustellungsbevollmaechtigterValid, AbrechnungRequestDTO> {

        private static final String INCOMPLETE = "Zu einem Zustellungsbevollmächtigten sind ID und Typ anzugeben.";
        private static final String LEFTOVER = "Ohne Zustellungsbevollmächtigten sind weder ID noch Typ anzugeben.";

        private static final String ID = "zustellungsbevollmaechtigterId";
        private static final String TYP = "zustellungsbevollmaechtigterTyp";

        @Override
        public boolean isValid(final AbrechnungRequestDTO abrechnung, final ConstraintValidatorContext context) {
            context.disableDefaultConstraintViolation();

            boolean valid = true;

            if (abrechnung.zustellungsbevollmaechtigterGenutzt()) {
                if (abrechnung.zustellungsbevollmaechtigterId() == null) {
                    addViolation(context, INCOMPLETE, ID);
                    valid = false;
                }
                if (abrechnung.zustellungsbevollmaechtigterTyp() == null) {
                    addViolation(context, INCOMPLETE, TYP);
                    valid = false;
                }
                return valid;
            }

            if (abrechnung.zustellungsbevollmaechtigterId() != null) {
                addViolation(context, LEFTOVER, ID);
                valid = false;
            }
            if (abrechnung.zustellungsbevollmaechtigterTyp() != null) {
                addViolation(context, LEFTOVER, TYP);
                valid = false;
            }

            return valid;
        }

        private static void addViolation(final ConstraintValidatorContext context, final String message, final String property) {
            context.buildConstraintViolationWithTemplate(message).addPropertyNode(property).addConstraintViolation();
        }

    }

}
