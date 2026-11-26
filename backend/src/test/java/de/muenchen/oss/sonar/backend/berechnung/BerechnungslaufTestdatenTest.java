package de.muenchen.oss.sonar.backend.berechnung;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungsArt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnungsergebnis;
import de.muenchen.oss.sonar.backend.berechnung.domain.BerechnungsergebnisService;
import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnungslauf;
import de.muenchen.oss.sonar.backend.berechnung.domain.BerechnungslaufService;
import de.muenchen.oss.sonar.backend.berechnung.domain.Vorabrechnung;
import de.muenchen.oss.sonar.backend.berechnung.domain.VorgaengerProjekt;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.projekt.domain.LinkedProjekt;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.JsonNode;

/**
 * Runs every case from {@code src/test/resources/abrechnung-testdaten} through
 * {@link Berechnungslauf} and compares the result with the recorded values.
 */
class BerechnungslaufTestdatenTest {

    private static final UUID PROJEKT_ID = UUID.randomUUID();
    private static final UUID ABRECHNUNG_ID = UUID.randomUUID();

    private static final int NACHKOMMASTELLEN_FLAECHE = 2;

    static List<AbrechnungTestfall> testfaelle() {
        return AbrechnungTestfall.loadAll();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testfaelle")
    void givenTestfall_thenErgebnisMatchesTheRecordedValues(final AbrechnungTestfall testfall) {
        final Berechnungsergebnis ergebnis = BerechnungsergebnisService.berechne(toBerechnungslauf(testfall));

        final String ergebnisJson = AbrechnungErgebnisJson.toJson(ergebnis);

        testfall.check(ergebnisJson);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testfaelle")
    void givenTheSameAbrechnungTwice_thenTheZweiterLaufChargesTheSame(final AbrechnungTestfall testfall) {
        final Berechnungslauf berechnungslauf = toBerechnungslauf(testfall);

        final String ersterLauf = AbrechnungErgebnisJson.toJson(BerechnungsergebnisService.berechne(berechnungslauf));
        final String zweiterLauf = AbrechnungErgebnisJson.toJson(BerechnungsergebnisService.berechne(berechnungslauf));

        assertThat(zweiterLauf).isEqualTo(ersterLauf);
    }

    private static Berechnungslauf toBerechnungslauf(final AbrechnungTestfall testfall) {
        final JsonNode input = testfall.input();
        return BerechnungslaufService.berechne(toProjekt(input), toAbrechnung(input), toVorgaengerProjekteWithNutzungsobjekte(input), abrechnungsdatum(input),
                toVorabrechnung(testfall.letzteAbrechnung()), input.get("username").asString());
    }

    private static Projekt toProjekt(final JsonNode input) {
        return new Projekt(PROJEKT_ID,
                input.get("projektNummer").asString(),
                zeitraumVon(input),
                abrechnungsdatum(input),
                input.get("verwaltungsgebuehren").decimalValue(),
                List.of(),
                null,
                toVorgaengerProjekte(input), List.of(), false, false);
    }

    private static List<LinkedProjekt> toVorgaengerProjekte(final JsonNode input) {
        final List<LinkedProjekt> vorgaengerProjekte = new ArrayList<>();
        for (final JsonNode vorgaengerProjekt : input.path("vorgaengerProjekte")) {
            vorgaengerProjekte.add(new LinkedProjekt(UUID.randomUUID(), vorgaengerProjekt.get("projektNummer").asString()));
        }
        return vorgaengerProjekte;
    }

    private static List<VorgaengerProjekt> toVorgaengerProjekteWithNutzungsobjekte(final JsonNode input) {
        final List<VorgaengerProjekt> vorgaengerProjekte = new ArrayList<>();
        for (final JsonNode vorgaengerProjekt : input.path("vorgaengerProjekte")) {
            vorgaengerProjekte.add(new VorgaengerProjekt(vorgaengerProjekt.get("projektNummer").asString(), toNutzungsobjekte(vorgaengerProjekt)));
        }
        return vorgaengerProjekte;
    }

    private static Abrechnung toAbrechnung(final JsonNode input) {
        return new Abrechnung(ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null,
                zeitraumVon(input), abrechnungsdatum(input), AbrechnungsArt.ENDABRECHNUNG, null, false,
                toNutzungsobjekte(input));
    }

    private static Vorabrechnung toVorabrechnung(final JsonNode letzteAbrechnung) {
        if (letzteAbrechnung == null || letzteAbrechnung.isNull()) {
            return null;
        }
        return new Vorabrechnung(letzteAbrechnung.get("lfdNr").asInt(),
                LocalDate.parse(letzteAbrechnung.get("abrechnungszeitraumBis").asString()),
                letzteAbrechnung.get("gebuehrGesamt").decimalValue());
    }

    private static LocalDate zeitraumVon(final JsonNode input) {
        LocalDate von = abrechnungsdatum(input);
        for (final JsonNode adresse : input.get("adressen")) {
            for (final JsonNode flaeche : adresse.get("flaechen")) {
                final LocalDate gueltigVon = LocalDate.parse(flaeche.get("gueltigVon").asString());
                if (gueltigVon.isBefore(von)) {
                    von = gueltigVon;
                }
            }
        }
        return von;
    }

    private static LocalDate abrechnungsdatum(final JsonNode input) {
        return LocalDate.parse(input.get("abrechnungsdatum").asString());
    }

    private static List<AbrechnungNutzungsobjekt> toNutzungsobjekte(final JsonNode projekt) {
        final List<AbrechnungNutzungsobjekt> nutzungsobjekte = new ArrayList<>();
        for (final JsonNode adresse : projekt.path("adressen")) {
            nutzungsobjekte.add(toNutzungsobjekt(nutzungsobjektId(adresse), adresse.get("adresse").asString(),
                    adresse.get("aufschlag50prozent").asBoolean(), toPositionen(adresse), List.of()));
        }
        for (final JsonNode adresse : projekt.path("adressenUeberspannung")) {
            nutzungsobjekte.add(toNutzungsobjekt(nutzungsobjektId(adresse), adresse.get("adresse").asString(), false, List.of(),
                    toMasten(adresse)));
        }
        return nutzungsobjekte;
    }

    /**
     * The same id in the input and in a Vorgaenger stands for a Nutzungsobjekt that the input took
     * over from it.
     */
    private static UUID nutzungsobjektId(final JsonNode adresse) {
        final JsonNode nutzungsobjektId = adresse.path("nutzungsobjektId");
        return nutzungsobjektId.isMissingNode() ? UUID.randomUUID() : UUID.fromString(nutzungsobjektId.asString());
    }

    private static AbrechnungNutzungsobjekt toNutzungsobjekt(final UUID nutzungsobjektId, final String adressname, final boolean aufschlag50prozent,
            final List<AbrechnungPosition> positionen, final List<AbrechnungMast> masten) {
        final int letztesLeerzeichen = adressname.lastIndexOf(' ');
        final String strasse = letztesLeerzeichen < 0 ? adressname : adressname.substring(0, letztesLeerzeichen);
        final String hausnummer = letztesLeerzeichen < 0 ? null : adressname.substring(letztesLeerzeichen + 1);
        return new AbrechnungNutzungsobjekt(nutzungsobjektId, Adressart.ADRESSE, strasse, hausnummer,
                null, null, null, null, null, null, null, null, null, aufschlag50prozent, positionen, masten);
    }

    private static List<AbrechnungMast> toMasten(final JsonNode adresse) {
        final List<AbrechnungMast> masten = new ArrayList<>();
        for (final JsonNode mast : adresse.get("masten")) {
            masten.add(new AbrechnungMast(UUID.randomUUID(),
                    LocalDate.parse(mast.get("beginn").asString()),
                    LocalDate.parse(mast.get("ende").asString())));
        }
        return masten;
    }

    private static List<AbrechnungPosition> toPositionen(final JsonNode adresse) {
        final List<AbrechnungPosition> positionen = new ArrayList<>();
        for (final JsonNode flaeche : adresse.get("flaechen")) {
            final BigDecimal flaecheQm = flaeche.get("flaecheQm").decimalValue().setScale(NACHKOMMASTELLEN_FLAECHE);
            positionen.add(new AbrechnungPosition(UUID.randomUUID(),
                    LocalDate.parse(flaeche.get("gueltigVon").asString()),
                    LocalDate.parse(flaeche.get("gueltigBis").asString()),
                    flaecheQm,
                    BigDecimal.ONE.setScale(NACHKOMMASTELLEN_FLAECHE),
                    flaecheQm));
        }
        return positionen;
    }

}
