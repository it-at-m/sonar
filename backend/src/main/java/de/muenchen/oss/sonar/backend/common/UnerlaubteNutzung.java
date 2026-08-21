package de.muenchen.oss.sonar.backend.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;

/**
 * The period of unauthorized use of a request DTO, together with the rules it has to satisfy.
 * <p>
 * Implemented by every request which carries such a period, so the three rules below are stated
 * once instead of being copied per DTO. A record implementing this interface satisfies the
 * accessors with its components.
 * </p>
 */
public interface UnerlaubteNutzung {

    LocalDate unerlaubteNutzungVon();

    LocalDate unerlaubteNutzungBis();

    Integer tageUnerlaubteNutzung();

    @JsonIgnore
    @AssertTrue(message = "Der Zeitraum der unerlaubten Nutzung ist mit Beginn und Ende anzugeben.") default boolean isUnerlaubteNutzungComplete() {
        return (unerlaubteNutzungVon() == null) == (unerlaubteNutzungBis() == null);
    }

    @JsonIgnore
    @AssertTrue(message = "Das Ende der unerlaubten Nutzung darf nicht vor deren Beginn liegen.") default boolean isUnerlaubteNutzungOrdered() {
        return unerlaubteNutzungVon() == null || unerlaubteNutzungBis() == null || !unerlaubteNutzungBis().isBefore(unerlaubteNutzungVon());
    }

    @JsonIgnore
    @AssertTrue(
            message = "Bitte entweder den Zeitraum oder die Anzahl der Tage der unerlaubten Nutzung angeben."
    )
    default boolean isUnerlaubteNutzungEitherZeitraumOrTage() {
        return unerlaubteNutzungVon() == null && unerlaubteNutzungBis() == null || tageUnerlaubteNutzung() == null;
    }

}
