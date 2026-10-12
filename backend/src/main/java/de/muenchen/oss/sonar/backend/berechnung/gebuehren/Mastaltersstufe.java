package de.muenchen.oss.sonar.backend.berechnung.gebuehren;

public enum Mastaltersstufe {

    ALTERSSTUFE_1("Altersstufe 1"),
    ALTERSSTUFE_2("Altersstufe 2");

    private static final int ALTERSGRENZE_MONATE = 12;

    private final String bezeichnung;

    Mastaltersstufe(final String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public static Mastaltersstufe from(final int alterInMonaten) {
        return alterInMonaten > ALTERSGRENZE_MONATE ? ALTERSSTUFE_2 : ALTERSSTUFE_1;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }
}
