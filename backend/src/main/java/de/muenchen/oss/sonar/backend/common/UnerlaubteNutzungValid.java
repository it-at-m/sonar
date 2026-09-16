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
@Constraint(validatedBy = UnerlaubteNutzungValid.Validator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UnerlaubteNutzungValid {

    String message() default "Die Angaben zur unerlaubten Nutzung sind nicht gültig.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<UnerlaubteNutzungValid, UnerlaubteNutzung> {

        private static final String ZEITRAUM_INCOMPLETE = "Der Zeitraum der unerlaubten Nutzung ist mit Beginn und Ende anzugeben.";
        private static final String ZEITRAUM_INVERTED = "Das Ende der unerlaubten Nutzung darf nicht vor deren Beginn liegen.";
        private static final String ZEITRAUM_AND_TAGE = "Bitte entweder den Zeitraum oder die Anzahl der Tage der unerlaubten Nutzung angeben.";

        private static final String VON = "unerlaubteNutzungVon";
        private static final String BIS = "unerlaubteNutzungBis";
        private static final String TAGE = "tageUnerlaubteNutzung";

        @Override
        public boolean isValid(final UnerlaubteNutzung unerlaubteNutzung, final ConstraintValidatorContext context) {
            context.disableDefaultConstraintViolation();

            boolean valid = true;

            if ((unerlaubteNutzung.unerlaubteNutzungVon() == null) != (unerlaubteNutzung.unerlaubteNutzungBis() == null)) {
                addViolation(context, ZEITRAUM_INCOMPLETE, unerlaubteNutzung.unerlaubteNutzungBis() == null ? BIS : VON);
                valid = false;
            }
            if (!Zeitraum.isOrdered(unerlaubteNutzung.unerlaubteNutzungVon(), unerlaubteNutzung.unerlaubteNutzungBis())) {
                addViolation(context, ZEITRAUM_INVERTED, BIS);
                valid = false;
            }
            if ((unerlaubteNutzung.unerlaubteNutzungVon() != null || unerlaubteNutzung.unerlaubteNutzungBis() != null)
                    && unerlaubteNutzung.tageUnerlaubteNutzung() != null) {
                addViolation(context, ZEITRAUM_AND_TAGE, TAGE);
                valid = false;
            }

            return valid;
        }

        private static void addViolation(final ConstraintValidatorContext context, final String message, final String property) {
            context.buildConstraintViolationWithTemplate(message).addPropertyNode(property).addConstraintViolation();
        }

    }

}
