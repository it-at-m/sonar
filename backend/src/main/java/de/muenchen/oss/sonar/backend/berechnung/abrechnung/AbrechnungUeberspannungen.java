package de.muenchen.oss.sonar.backend.berechnung.abrechnung;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Ported from the prototype and kept the way it was written there.
 * Cleaning up the naming, the method sizes and the mutability belongs to the refactoring, so PMD is
 * silenced for the whole class until then.
 */
@SuppressWarnings("PMD")
public class AbrechnungUeberspannungen {

    private final ArrayList<BescheiddatenUeberspannung> bescheiddatenUeberspannungen;
    private String adresse;
    private String berechnungslog;
    private BigDecimal gebuehrUeberspannungen;

    public AbrechnungUeberspannungen() {
        this.bescheiddatenUeberspannungen = new ArrayList<BescheiddatenUeberspannung>();
        this.adresse = "";
        this.berechnungslog = "";
        this.gebuehrUeberspannungen = BigDecimal.ZERO;
    }

    public void addBescheiddatenUeberspannung(BescheiddatenUeberspannung ueDaten) {
        bescheiddatenUeberspannungen.add(ueDaten);
    }

    public ArrayList<BescheiddatenUeberspannung> getBescheiddatenUeberspannungen() {
        return bescheiddatenUeberspannungen;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void appendToBerechnungslog(String text) {
        berechnungslog = berechnungslog.concat(text);
    }

    public String getBerechnungslog() {
        return berechnungslog;
    }

    public BigDecimal getGebuehrUeberspannungen() {
        return gebuehrUeberspannungen;
    }

    public void setGebuehrUeberspannungen(BigDecimal gebuehrUeberspannungen) {
        this.gebuehrUeberspannungen = gebuehrUeberspannungen;
    }
}
