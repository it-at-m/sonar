package de.muenchen.oss.sonar.backend.common;

import java.time.LocalDate;

public interface UnerlaubteNutzung {

    LocalDate unerlaubteNutzungVon();

    LocalDate unerlaubteNutzungBis();

    Integer tageUnerlaubteNutzung();

}
