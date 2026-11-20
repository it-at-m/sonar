package de.muenchen.oss.sonar.backend.abrechnung.domain;

import java.time.LocalDate;

public record Abrechnungsanschluss(boolean anlegenMoeglich, LocalDate zeitraumVon) {

    public static final Abrechnungsanschluss FREI = new Abrechnungsanschluss(true, null);

    public static final Abrechnungsanschluss NICHT_MOEGLICH = new Abrechnungsanschluss(false, null);

    public static Abrechnungsanschluss beginntAm(final LocalDate zeitraumVon) {
        return new Abrechnungsanschluss(true, zeitraumVon);
    }
}
