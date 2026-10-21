package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechengroessenstufe;
import de.muenchen.oss.sonar.backend.berechnung.gebuehren.Flaechenzeitindex;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FlaechenwocheService {

    public static Flaechenwoche berechne(final List<Flaechenadresse> adressen, final int woche) {

        final List<Flaechenverteilung> verteilung = new ArrayList<>();
        final StringBuilder zwischenabrechnungGrund = new StringBuilder();

        for (final Flaechenadresse adresse : adressen) {
            final List<Flaechenbeitrag> beitraege = FlaechenadresseService.beitraege(adresse, woche);
            verteilung.addAll(verteilungPerFlaechenzeitindex(adresse, beitraege));
            zwischenabrechnungGrund.append(zwischenabrechnungGrund(adresse, beitraege));
        }

        final BigDecimal baustellenflaecheQm = verteilung.stream()
                .map(Flaechenverteilung::flaecheQm)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        final Flaechengroessenstufe flaechengroessenstufe = Flaechengroessenstufe
                .from(baustellenflaecheQm.setScale(0, RoundingMode.CEILING).intValueExact());

        return new Flaechenwoche(woche, baustellenflaecheQm, flaechengroessenstufe, verteilung,
                postenPerGebuehrensatz(verteilung, flaechengroessenstufe), zwischenabrechnungGrund.toString());
    }

    private static List<Flaechenverteilung> verteilungPerFlaechenzeitindex(final Flaechenadresse adresse,
            final List<Flaechenbeitrag> beitraege) {

        final Map<Flaechenzeitindex, Flaechenverteilung> perFlaechenzeitindex = new EnumMap<>(Flaechenzeitindex.class);

        for (final Flaechenbeitrag beitrag : beitraege) {
            beitrag.flaechenzeitindex().ifPresent(flaechenzeitindex -> perFlaechenzeitindex.merge(
                    flaechenzeitindex,
                    new Flaechenverteilung(adresse.adressbezeichnung(), adresse.isAufschlag50Prozent(), flaechenzeitindex,
                            beitrag.flaecheQm(), beitrag.flaeche().bezeichnung()),
                    FlaechenwocheService::plus));
        }

        return List.copyOf(perFlaechenzeitindex.values());
    }

    private static Flaechenverteilung plus(final Flaechenverteilung verteilung, final Flaechenverteilung weitere) {
        return new Flaechenverteilung(verteilung.adressbezeichnung(), verteilung.isAufschlag50Prozent(), verteilung.flaechenzeitindex(),
                verteilung.flaecheQm().add(weitere.flaecheQm()),
                verteilung.flaechennamen() + ", " + weitere.flaechennamen());
    }

    private static String zwischenabrechnungGrund(final Flaechenadresse adresse, final List<Flaechenbeitrag> beitraege) {

        final StringBuilder grund = new StringBuilder();

        for (final Flaechenbeitrag beitrag : beitraege) {
            FlaechenbeitragService.zwischenabrechnungGrund(beitrag).ifPresent(flaechenGrund -> grund
                    .append("    ").append(adresse.adressbezeichnung()).append(": ").append(flaechenGrund)
                    .append(adresse.isAufschlag50Prozent() ? " (50%-Aufschlag)" : "")
                    .append(System.lineSeparator()));
        }

        return grund.toString();
    }

    private static List<Gebuehrenposten> postenPerGebuehrensatz(final List<Flaechenverteilung> verteilung,
            final Flaechengroessenstufe flaechengroessenstufe) {

        final Map<Flaechenzeitindex, BigDecimal> ohneAufschlag = new EnumMap<>(Flaechenzeitindex.class);
        final Map<Flaechenzeitindex, BigDecimal> mitAufschlag = new EnumMap<>(Flaechenzeitindex.class);

        for (final Flaechenverteilung adressverteilung : verteilung) {
            final Map<Flaechenzeitindex, BigDecimal> perFlaechenzeitindex = adressverteilung.isAufschlag50Prozent()
                    ? mitAufschlag
                    : ohneAufschlag;
            perFlaechenzeitindex.merge(adressverteilung.flaechenzeitindex(), adressverteilung.flaecheQm(), BigDecimal::add);
        }

        final List<Gebuehrenposten> posten = new ArrayList<>();
        ohneAufschlag.forEach((flaechenzeitindex, flaecheQm) -> posten
                .add(new Gebuehrenposten(flaechengroessenstufe, false, flaechenzeitindex, flaecheQm)));
        mitAufschlag.forEach((flaechenzeitindex, flaecheQm) -> posten
                .add(new Gebuehrenposten(flaechengroessenstufe, true, flaechenzeitindex, flaecheQm)));
        return posten;
    }
}
