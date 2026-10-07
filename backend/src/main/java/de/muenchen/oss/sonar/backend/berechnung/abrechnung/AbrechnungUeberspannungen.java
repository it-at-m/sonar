package de.muenchen.oss.sonar.backend.berechnung.abrechnung;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder(builderClassName = "Builder")
public record AbrechnungUeberspannungen(
        String adresse,
        String berechnungslog,
        BigDecimal gebuehrUeberspannungen,
        List<BescheiddatenUeberspannung> bescheiddatenUeberspannungen) {

    public AbrechnungUeberspannungen {
        adresse = adresse == null ? "" : adresse;
        berechnungslog = berechnungslog == null ? "" : berechnungslog;
        gebuehrUeberspannungen = gebuehrUeberspannungen == null ? BigDecimal.ZERO : gebuehrUeberspannungen;
        bescheiddatenUeberspannungen = bescheiddatenUeberspannungen == null ? List.of() : List.copyOf(bescheiddatenUeberspannungen);
    }
}
