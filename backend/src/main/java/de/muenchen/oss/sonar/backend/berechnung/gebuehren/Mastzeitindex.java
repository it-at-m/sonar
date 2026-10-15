package de.muenchen.oss.sonar.backend.berechnung.gebuehren;

public enum Mastzeitindex {

    ZEITINDEX_1("Altersstufe 1", 1),
    ZEITINDEX_2("Altersstufe 2", 13);

    private final String bezeichnung;
    private final int abMonat;

    Mastzeitindex(final String bezeichnung, final int abMonat) {
        this.bezeichnung = bezeichnung;
        this.abMonat = abMonat;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public static Mastzeitindex from(final int alterInMonaten) {
        Mastzeitindex erreicht = ZEITINDEX_1;
        for (final Mastzeitindex mastzeitindex : values()) {
            if (alterInMonaten >= mastzeitindex.abMonat) {
                erreicht = mastzeitindex;
            }
        }
        return erreicht;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }
}
