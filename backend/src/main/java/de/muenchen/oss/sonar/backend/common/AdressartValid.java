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
@Constraint(validatedBy = AdressartValid.Validator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdressartValid {

    String message() default "Die Angaben zur Adresse oder zum Flurstück sind nicht gültig.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<AdressartValid, Adresse> {

        private static final String ADRESSE_REQUIRED = "Zu einer Adresse sind Adresse und Hausnummer von anzugeben.";
        private static final String ADRESSE_LEFTOVER = "Zu einer Adresse sind weder Flurstück noch Gemarkung anzugeben.";
        private static final String FLURSTUECK_REQUIRED = "Zu einem Flurstück sind Flurstück und Gemarkung anzugeben.";
        private static final String FLURSTUECK_LEFTOVER = "Zu einem Flurstück sind weder Adresse noch Hausnummern anzugeben.";

        private static final String ADRESSE = "adresse";
        private static final String HAUSNUMMER_VON = "hausnummerVon";
        private static final String HAUSNUMMER_BIS = "hausnummerBis";
        private static final String FLURSTUECK = "flurstueck";
        private static final String GEMARKUNG = "gemarkung";

        @Override
        public boolean isValid(final Adresse adresse, final ConstraintValidatorContext context) {
            context.disableDefaultConstraintViolation();

            // A missing Art is reported by its own @NotNull, and without it neither group applies.
            if (adresse.art() == null) {
                return true;
            }

            return adresse.art() == Adressart.ADRESSE
                    ? isValidAdresse(adresse, context)
                    : isValidFlurstueck(adresse, context);
        }

        private static boolean isValidAdresse(final Adresse adresse, final ConstraintValidatorContext context) {
            boolean valid = true;

            if (adresse.adresse() == null) {
                addViolation(context, ADRESSE_REQUIRED, ADRESSE);
                valid = false;
            }
            if (adresse.hausnummerVon() == null) {
                addViolation(context, ADRESSE_REQUIRED, HAUSNUMMER_VON);
                valid = false;
            }
            if (adresse.flurstueck() != null) {
                addViolation(context, ADRESSE_LEFTOVER, FLURSTUECK);
                valid = false;
            }
            if (adresse.gemarkung() != null) {
                addViolation(context, ADRESSE_LEFTOVER, GEMARKUNG);
                valid = false;
            }

            return valid;
        }

        private static boolean isValidFlurstueck(final Adresse adresse, final ConstraintValidatorContext context) {
            boolean valid = true;

            if (adresse.flurstueck() == null) {
                addViolation(context, FLURSTUECK_REQUIRED, FLURSTUECK);
                valid = false;
            }
            if (adresse.gemarkung() == null) {
                addViolation(context, FLURSTUECK_REQUIRED, GEMARKUNG);
                valid = false;
            }
            if (adresse.adresse() != null) {
                addViolation(context, FLURSTUECK_LEFTOVER, ADRESSE);
                valid = false;
            }
            if (adresse.hausnummerVon() != null) {
                addViolation(context, FLURSTUECK_LEFTOVER, HAUSNUMMER_VON);
                valid = false;
            }
            if (adresse.hausnummerBis() != null) {
                addViolation(context, FLURSTUECK_LEFTOVER, HAUSNUMMER_BIS);
                valid = false;
            }

            return valid;
        }

        private static void addViolation(final ConstraintValidatorContext context, final String message, final String property) {
            context.buildConstraintViolationWithTemplate(message).addPropertyNode(property).addConstraintViolation();
        }

    }

}
