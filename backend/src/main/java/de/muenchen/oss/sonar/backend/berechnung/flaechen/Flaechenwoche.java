package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Gebuehrenstufe;
import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Zeitindex;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * One Woche of the Flächenabrechnung, worked out in full. The Flächen are charged Woche by Woche.
 */
public record Flaechenwoche(
        int woche,
        BigDecimal baustellenflaecheQm,
        Gebuehrenstufe gebuehrenstufe,
        List<Flaechenverteilung> verteilung,
        List<Gebuehrenposten> posten,
        String zwischenabrechnungGrund) {

    public Flaechenwoche {
        verteilung = List.copyOf(verteilung);
        posten = List.copyOf(posten);
    }

    public static Flaechenwoche berechne(final List<Flaechenadresse> adressen, final int woche) {

        final List<Flaechenverteilung> verteilung = new ArrayList<>();
        final StringBuilder zwischenabrechnungGrund = new StringBuilder();

        for (final Flaechenadresse adresse : adressen) {
            final List<Flaechenbeitrag> beitraege = adresse.beitraege(woche);
            verteilung.addAll(verteilungJeZeitindex(adresse, beitraege));
            zwischenabrechnungGrund.append(zwischenabrechnungGrund(adresse, beitraege));
        }

        final BigDecimal baustellenflaecheQm = verteilung.stream()
                .map(Flaechenverteilung::flaecheQm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        final Gebuehrenstufe gebuehrenstufe = Gebuehrenstufe.from(baustellenflaecheQm.setScale(0, RoundingMode.CEILING).intValueExact());

        return new Flaechenwoche(woche, baustellenflaecheQm, gebuehrenstufe, verteilung,
                postenJeGebuehrensatz(verteilung, gebuehrenstufe), zwischenabrechnungGrund.toString());
    }

    private static List<Flaechenverteilung> verteilungJeZeitindex(final Flaechenadresse adresse, final List<Flaechenbeitrag> beitraege) {

        final Map<Zeitindex, Flaechenverteilung> jeZeitindex = new EnumMap<>(Zeitindex.class);

        for (final Flaechenbeitrag beitrag : beitraege) {
            beitrag.zeitindex().ifPresent(zeitindex -> jeZeitindex.merge(
                    zeitindex,
                    new Flaechenverteilung(adresse.adressbezeichnung(), adresse.isAufschlag50Prozent(), zeitindex,
                            beitrag.flaecheQm(), beitrag.flaeche().bezeichnung()),
                    Flaechenverteilung::plus));
        }

        return List.copyOf(jeZeitindex.values());
    }

    private static String zwischenabrechnungGrund(final Flaechenadresse adresse, final List<Flaechenbeitrag> beitraege) {

        final StringBuilder grund = new StringBuilder();

        for (final Flaechenbeitrag beitrag : beitraege) {
            beitrag.zwischenabrechnungGrund().ifPresent(flaechenGrund -> grund
                    .append("    ").append(adresse.adressbezeichnung()).append(": ").append(flaechenGrund)
                    .append(adresse.isAufschlag50Prozent() ? " (50%-Aufschlag)" : "")
                    .append(System.lineSeparator()));
        }

        return grund.toString();
    }

    private static List<Gebuehrenposten> postenJeGebuehrensatz(final List<Flaechenverteilung> verteilung, final Gebuehrenstufe gebuehrenstufe) {

        final Map<Zeitindex, BigDecimal> ohneAufschlag = new EnumMap<>(Zeitindex.class);
        final Map<Zeitindex, BigDecimal> mitAufschlag = new EnumMap<>(Zeitindex.class);

        for (final Flaechenverteilung adressverteilung : verteilung) {
            final Map<Zeitindex, BigDecimal> jeZeitindex = adressverteilung.isAufschlag50Prozent() ? mitAufschlag : ohneAufschlag;
            jeZeitindex.merge(adressverteilung.zeitindex(), adressverteilung.flaecheQm(), BigDecimal::add);
        }

        final List<Gebuehrenposten> posten = new ArrayList<>();
        ohneAufschlag.forEach((zeitindex, flaecheQm) -> posten.add(new Gebuehrenposten(gebuehrenstufe, false, zeitindex, flaecheQm)));
        mitAufschlag.forEach((zeitindex, flaecheQm) -> posten.add(new Gebuehrenposten(gebuehrenstufe, true, zeitindex, flaecheQm)));
        return posten;
    }

    public BigDecimal gebuehr() {
        return posten.stream().map(Gebuehrenposten::gebuehr).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
