package de.muenchen.oss.sonar.backend.projekt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjektSortBy {

    PROJEKTNUMMER("projektnummer"),
    ABRECHNUNG_BEGINN("abrechnungBeginn"),
    ABRECHNUNG_ENDE("abrechnungEnde");

    private final String entityAttribute;
}
