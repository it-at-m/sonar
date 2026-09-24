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
import java.time.LocalDate;

@Documented
@Constraint(validatedBy = ZeitraumOrdered.Validator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ZeitraumOrdered {

    String von();

    String bis();

    String message() default "Das Ende des Zeitraums darf nicht vor dessen Beginn liegen.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<ZeitraumOrdered, Record> {

        private String von;
        private String bis;

        @Override
        public void initialize(final ZeitraumOrdered zeitraumOrdered) {
            von = zeitraumOrdered.von();
            bis = zeitraumOrdered.bis();
        }

        @Override
        public boolean isValid(final Record target, final ConstraintValidatorContext context) {
            return Zeitraum.isOrdered(dateOf(target, von), dateOf(target, bis));
        }

        private static LocalDate dateOf(final Record target, final String component) {
            try {
                return (LocalDate) target.getClass().getMethod(component).invoke(target);
            } catch (final ReflectiveOperationException | ClassCastException e) {
                throw new IllegalStateException(
                        "%s has no LocalDate component %s".formatted(target.getClass().getName(), component), e);
            }
        }

    }

}
