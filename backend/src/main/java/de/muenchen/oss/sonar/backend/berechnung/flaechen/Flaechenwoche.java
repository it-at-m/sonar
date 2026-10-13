package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechengroessenstufe;
import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechenzeitindex;
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
        Flaechengroessenstufe flaechengroessenstufe,
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
            verteilung.addAll(verteilungJeFlaechenzeitindex(adresse, beitraege));
            zwischenabrechnungGrund.append(zwischenabrechnungGrund(adresse, beitraege));
        }

        final BigDecimal baustellenflaecheQm = verteilung.stream()
                .map(Flaechenverteilung::flaecheQm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        final Flaechengroessenstufe flaechengroessenstufe = Flaechengroessenstufe
                .from(baustellenflaecheQm.setScale(0, RoundingMode.CEILING).intValueExact());

        return new Flaechenwoche(woche, baustellenflaecheQm, flaechengroessenstufe, verteilung,
                postenJeGebuehrensatz(verteilung, flaechengroessenstufe), zwischenabrechnungGrund.toString());
    }

    private static List<Flaechenverteilung> verteilungJeFlaechenzeitindex(final Flaechenadresse adresse,
            final List<Flaechenbeitrag> beitraege) {

        final Map<Flaechenzeitindex, Flaechenverteilung> jeFlaechenzeitindex = new EnumMap<>(Flaechenzeitindex.class);

        for (final Flaechenbeitrag beitrag : beitraege) {
            beitrag.flaechenzeitindex().ifPresent(flaechenzeitindex -> jeFlaechenzeitindex.merge(
                    flaechenzeitindex,
                    new Flaechenverteilung(adresse.adressbezeichnung(), adresse.isAufschlag50Prozent(), flaechenzeitindex,
                            beitrag.flaecheQm(), beitrag.flaeche().bezeichnung()),
                    Flaechenverteilung::plus));
        }

        return List.copyOf(jeFlaechenzeitindex.values());
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

    private static List<Gebuehrenposten> postenJeGebuehrensatz(final List<Flaechenverteilung> verteilung,
            final Flaechengroessenstufe flaechengroessenstufe) {

        final Map<Flaechenzeitindex, BigDecimal> ohneAufschlag = new EnumMap<>(Flaechenzeitindex.class);
        final Map<Flaechenzeitindex, BigDecimal> mitAufschlag = new EnumMap<>(Flaechenzeitindex.class);

        for (final Flaechenverteilung adressverteilung : verteilung) {
            final Map<Flaechenzeitindex, BigDecimal> jeFlaechenzeitindex = adressverteilung.isAufschlag50Prozent()
                    ? mitAufschlag
                    : ohneAufschlag;
            jeFlaechenzeitindex.merge(adressverteilung.flaechenzeitindex(), adressverteilung.flaecheQm(), BigDecimal::add);
        }

        final List<Gebuehrenposten> posten = new ArrayList<>();
        ohneAufschlag.forEach((flaechenzeitindex, flaecheQm) -> posten
                .add(new Gebuehrenposten(flaechengroessenstufe, false, flaechenzeitindex, flaecheQm)));
        mitAufschlag.forEach((flaechenzeitindex, flaecheQm) -> posten
                .add(new Gebuehrenposten(flaechengroessenstufe, true, flaechenzeitindex, flaecheQm)));
        return posten;
    }

    public BigDecimal gebuehr() {
        return posten.stream().map(Gebuehrenposten::gebuehr).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
