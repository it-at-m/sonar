package de.muenchen.oss.sonar.backend.common;

public interface Adresse {

    Adressart art();

    String adresse();

    String hausnummerVon();

    String hausnummerBis();

    String flurstueck();

    String gemarkung();

}
