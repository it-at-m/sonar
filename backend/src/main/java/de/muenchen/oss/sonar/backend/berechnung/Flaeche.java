package de.muenchen.oss.sonar.backend.berechnung;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * One Position of a Nutzungsobjekt. A Fläche carries the total area of its Adresse from its Beginn
 * on, and {@link Adresse} turns that into the delta against the Fläche before it. The delta is what
 * the Gebühr is charged on, and a Flächenreduzierung lowers it again.
 */
public class Flaeche {

    /* default */ static final String KEIN_ZEITINDEX = "";

    private static final int TAGE_PRO_WOCHE = 7;

    private static final ResultZa KEINE_ZWISCHENABRECHNUNG = new ResultZa(false, "");

    private final String flaechenname;
    private final LocalDate beginn;
    private final LocalDate ende;
    private final BigDecimal gesamtflaecheAdresse;
    private final int dauerTage;
    private final int angefangeneWochen;

    private int wocheGueltigkeitVon;
    private int wocheGueltigkeitBis;
    private int adresseGueltigkeitBis;
    private BigDecimal deltaFlaeche;
    private BigDecimal deltaFlaecheAnzeige;

    public Flaeche(final String flaechenname, final LocalDate beginn, final LocalDate ende, final BigDecimal gesamtflaecheAdr) {
        this.flaechenname = flaechenname;
        // Placeholders. The weeks are counted from the Beginn der Maßnahme, which is known only once
        // every Fläche has been added, so Adresse settles them through setGueltigkeitWocheVonBis and
        // setAdresseGueltigBis.
        this.wocheGueltigkeitVon = 1;
        this.wocheGueltigkeitBis = 1;
        this.adresseGueltigkeitBis = 1;
        this.beginn = beginn;
        this.ende = ende;
        this.gesamtflaecheAdresse = gesamtflaecheAdr;
        this.deltaFlaeche = BigDecimal.ZERO;
        this.deltaFlaecheAnzeige = BigDecimal.ZERO;
        // Both boundaries count, so a Fläche from the 1st to the 7th lasts seven days.
        this.dauerTage = (int) ChronoUnit.DAYS.between(beginn, ende) + 1;
        // A started week is charged as a whole one.
        this.angefangeneWochen = (int) Math.ceil((double) dauerTage / TAGE_PRO_WOCHE);
    }

    public String getFlaechenname() {
        return flaechenname;
    }

    public LocalDate getBeginn() {
        return beginn;
    }

    public LocalDate getEnde() {
        return ende;
    }

    public BigDecimal getGesamtflaecheAdresse() {
        return gesamtflaecheAdresse;
    }

    public BigDecimal getDeltaFlaeche() {
        return deltaFlaeche;
    }

    public void setDeltaFlaeche(final BigDecimal deltaFlaeche) {
        this.deltaFlaeche = deltaFlaeche;
    }

    public void setDeltaFlaecheAnzeige(final BigDecimal deltaFlaecheAnzeige) {
        this.deltaFlaecheAnzeige = deltaFlaecheAnzeige;
    }

    public int getWocheGueltigVon() {
        return this.wocheGueltigkeitVon;
    }

    public int getWocheGueltigBis() {
        return this.wocheGueltigkeitBis;
    }

    public void setGueltigkeitWocheVonBis(final LocalDate beginnMassnahme) {
        this.wocheGueltigkeitVon = (int) ChronoUnit.WEEKS.between(beginnMassnahme, beginn) + 1;
        this.wocheGueltigkeitBis = wocheGueltigkeitVon + angefangeneWochen - 1;
    }

    public void setAdresseGueltigBis(final int woche) {
        this.adresseGueltigkeitBis = woche;
    }

    public ResultZa zwischenabrechnungNoetwendig(final int aktWoche) {

        if (!isLaengerAlsEinTag()) {
            return KEINE_ZWISCHENABRECHNUNG;
        }

        if (aktWoche == wocheGueltigkeitVon) {
            final BigDecimal delta = deltaFlaecheAnzeige;
            if (delta.compareTo(BigDecimal.ZERO) > 0) {
                return new ResultZa(true, "Flächenerweiterung +" + delta + "m² (Fläche " + flaechenname + ")");
            }
            return new ResultZa(true, "Flächenreduzierung " + delta + "m² (Fläche " + flaechenname + ")");
        }

        if (aktWoche <= 1) {
            return KEINE_ZWISCHENABRECHNUNG;
        }

        final String zeitindex = getZeitindex(aktWoche);
        final String zeitindexVorwoche = getZeitindex(aktWoche - 1);
        if (zeitindex.equals(zeitindexVorwoche)) {
            return KEINE_ZWISCHENABRECHNUNG;
        }
        if (KEIN_ZEITINDEX.equals(zeitindex) && aktWoche > adresseGueltigkeitBis) {
            return new ResultZa(true, "Fläche nicht mehr aktiv (Fläche " + flaechenname + ")");
        }
        return new ResultZa(true, "Wechsel von " + zeitindexVorwoche + " zu " + zeitindex + " (Fläche " + flaechenname + ")");
    }

    public String getZeitindex(final int woche) {

        if (!isAktiv(woche)) {
            return KEIN_ZEITINDEX;
        }

        final int laufendeWoche = woche - wocheGueltigkeitVon;
        if (laufendeWoche >= Gebuehren.ZEITGRENZE_3) {
            return Gebuehren.ZEITINDEX_4;
        }
        if (laufendeWoche >= Gebuehren.ZEITGRENZE_2) {
            return Gebuehren.ZEITINDEX_3;
        }
        if (laufendeWoche >= Gebuehren.ZEITGRENZE_1) {
            return Gebuehren.ZEITINDEX_2;
        }
        return Gebuehren.ZEITINDEX_1;
    }

    private boolean isAktiv(final int woche) {
        return woche >= wocheGueltigkeitVon
                && woche <= adresseGueltigkeitBis
                && deltaFlaeche.compareTo(BigDecimal.ZERO) != 0
                && isImAbrechnungszeitraum();
    }

    private boolean isImAbrechnungszeitraum() {
        return !beginn.isAfter(ende);
    }

    private boolean isLaengerAlsEinTag() {
        return ende.isAfter(beginn);
    }

    @Override
    public String toString() {

        if (!isImAbrechnungszeitraum()) {
            return "   - Fläche " + flaechenname + "\n"
                    + "        Gültigkeit liegt nicht im Abrechnungszeitraum.\n";
        }

        final String vorzeichen = deltaFlaeche.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return "   - Fläche " + flaechenname + "\n"
                + "        Beginn: " + beginn + "  Ende: " + ende + " (" + dauerTage + " Tage --> " + angefangeneWochen + " Wochen)\n"
                + "        Gesamtfläche Adresse: " + gesamtflaecheAdresse + "m²\n"
                + "        Flächenänderung: " + vorzeichen + deltaFlaeche + "m²\n"
                + "        Gültigkeit: Woche " + wocheGueltigkeitVon + "-" + wocheGueltigkeitBis + "\n";
    }
}
