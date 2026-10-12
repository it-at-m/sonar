package de.muenchen.oss.sonar.backend.common;

import java.util.List;

public interface Nutzungsangabe {

    List<Nutzung> nutzungen();

    String nutzungSonstiges();

}
