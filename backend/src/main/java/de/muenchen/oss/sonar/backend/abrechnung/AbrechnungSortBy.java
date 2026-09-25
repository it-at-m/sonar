package de.muenchen.oss.sonar.backend.abrechnung;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AbrechnungSortBy {

    ZEITRAUM_VON("zeitraumVon"),
    ZEITRAUM_BIS("zeitraumBis"),
    ABRECHNUNGS_ART("abrechnungsArt"),
    GESCHAEFTSPARTNER_ID("geschaeftspartnerId");

    private final String entityAttribute;
}
