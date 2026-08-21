package de.muenchen.oss.sonar.backend.projekt.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * An enum instead of a free-form property name: an unknown name would reach the persistence
 * layer as an invalid property and be answered with a 500, and a valid one would let a client
 * order by any attribute of the entity, including those the API does not expose. Spring rejects
 * anything outside this set before the controller is entered.
 * <p>
 * The count of addresses is deliberately absent. It is derived from the association instead of
 * being stored, so ordering by it would need an aggregating query, not a plain sort property.
 * </p>
 */
@Getter
@RequiredArgsConstructor
public enum ProjektSortBy {

    PROJEKTNUMMER("projektnummer"),
    ABRECHNUNG_BEGINN("abrechnungBeginn"),
    ABRECHNUNG_ENDE("abrechnungEnde");

    private final String entityAttribute;
}
