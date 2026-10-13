package de.muenchen.oss.sonar.backend.berechnung.gebuehren;

/**
 * How long a Fläche has been standing, which is one of the two things the Gebührensatz follows.
 */
public enum Flaechenzeitindex {

    ZEITINDEX_1("Zeitindex 1", 0),
    ZEITINDEX_2("Zeitindex 2", 13),
    ZEITINDEX_3("Zeitindex 3", 52),
    ZEITINDEX_4("Zeitindex 4", 78);

    private final String bezeichnung;
    private final int abLaufenderWoche;

    Flaechenzeitindex(final String bezeichnung, final int abLaufenderWoche) {
        this.bezeichnung = bezeichnung;
        this.abLaufenderWoche = abLaufenderWoche;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public static Flaechenzeitindex from(final int laufendeWoche) {
        Flaechenzeitindex erreicht = ZEITINDEX_1;
        for (final Flaechenzeitindex flaechenzeitindex : values()) {
            if (laufendeWoche >= flaechenzeitindex.abLaufenderWoche) {
                erreicht = flaechenzeitindex;
            }
        }
        return erreicht;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }
}
