package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnungsergebnis;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Turns an Abrechnung into the JSON the recorded expectations are written against, so a
 * recording stays comparable with the one of the prototype.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class AbrechnungErgebnisJson {

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();

    /**
     * The expectations are written against the response of the prototype. Its DTO carried other names
     * for the Lfd. Nr., the Berechnungslog and the Überspannungen of an Abrechnung, and for the
     * Adresse, the Gebühr and the Abschnitte of every Überspannungsadresse.
     */
    static String toJson(final Berechnungsergebnis abrechnung) {
        final ObjectNode ergebnis = MAPPER.valueToTree(abrechnung);
        ergebnis.remove(List.of("lfdNr", "berechnungslog"));
        ergebnis.put("abrechnungLfdNr", abrechnung.lfdNr());
        ergebnis.put("logAbrechnung", abrechnung.berechnungslog());
        final JsonNode ueberspannungen = ergebnis.remove("abrechnungenUeberspannungen");
        ueberspannungen.forEach(adresse -> rename((ObjectNode) adresse));
        ergebnis.set("bescheiddatenUeberspannungen", ueberspannungen);
        return MAPPER.writeValueAsString(ergebnis);
    }

    private static void rename(final ObjectNode ueberspannungsadresse) {
        ueberspannungsadresse.set("adresse", ueberspannungsadresse.remove("adressbezeichnung"));
        ueberspannungsadresse.set("gebuehrUeberspannungen", ueberspannungsadresse.remove("gebuehr"));
        ueberspannungsadresse.set("bescheiddatenUeberspannungen", ueberspannungsadresse.remove("abschnitte"));
    }

}
