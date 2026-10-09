package de.muenchen.oss.sonar.backend.calculation.flaechen;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record Flaechenadresse(
        String adressbezeichnung,
        boolean isAufschlag50Prozent,
        List<Flaeche> flaechen,
        int letzteWoche) {

    public Flaechenadresse {
        flaechen = List.copyOf(flaechen);
    }

    public static Flaechenadresse from(final AbrechnungNutzungsobjekt nutzungsobjekt, final LocalDate calculationDate,
            final LocalDate beginnWochenlauf) {

        final List<AbrechnungPosition> sortedByBeginn = nutzungsobjekt.positionen().stream()
                .sorted(Comparator.comparing(AbrechnungPosition::beginn))
                .toList();

        final List<Flaeche> flaechen = new ArrayList<>();
        BigDecimal gesamtflaecheVorher = BigDecimal.ZERO;

        for (final AbrechnungPosition position : sortedByBeginn) {
            final Flaeche flaeche = Flaeche.from(position, flaechen.size() + 1, calculationDate, beginnWochenlauf, gesamtflaecheVorher);
            flaechen.add(flaeche);
            gesamtflaecheVorher = flaeche.gesamtflaecheAdresse();
        }

        final int letzteWoche = flaechen.stream().mapToInt(Flaeche::letzteWoche).max().orElse(0);

        return new Flaechenadresse(nutzungsobjekt.adressbezeichnung(), nutzungsobjekt.isAufschlag50Prozent(), flaechen, letzteWoche);
    }

    public List<Flaechenbeitrag> beitraege(final int woche) {

        final BigDecimal[] beitraegeQm = flaechen.stream().map(Flaeche::flaechenaenderung).toArray(BigDecimal[]::new);

        for (int index = 0; index < flaechen.size(); index++) {
            if (flaechen.get(index).isWirksamIn(woche) && beitraegeQm[index].signum() < 0) {
                reduziere(beitraegeQm, index);
            }
        }

        final List<Flaechenbeitrag> beitraege = new ArrayList<>(flaechen.size());
        for (int index = 0; index < flaechen.size(); index++) {
            beitraege.add(new Flaechenbeitrag(flaechen.get(index), beitraegeQm[index], woche, letzteWoche));
        }
        return beitraege;
    }

    private static void reduziere(final BigDecimal[] beitraegeQm, final int reduzierende) {

        BigDecimal rest = beitraegeQm[reduzierende].abs();
        beitraegeQm[reduzierende] = BigDecimal.ZERO;

        for (int index = 0; index < beitraegeQm.length && rest.signum() > 0; index++) {

            if (beitraegeQm[index].signum() == 0) {
                continue;
            }

            if (rest.compareTo(beitraegeQm[index]) > 0) {
                rest = rest.subtract(beitraegeQm[index]);
                beitraegeQm[index] = BigDecimal.ZERO;
            } else {
                beitraegeQm[index] = beitraegeQm[index].subtract(rest);
                rest = BigDecimal.ZERO;
            }
        }
    }

    public Optional<LocalDate> beginn() {
        return flaechen.stream().map(Flaeche::beginn).min(Comparator.naturalOrder());
    }

    public Optional<LocalDate> ende() {
        return flaechen.stream().map(Flaeche::berechnungsEnde).max(Comparator.naturalOrder());
    }

    public String zusammenfassung() {

        final LocalDate beginn = beginn().orElse(null);
        final LocalDate ende = ende().orElse(null);

        final StringBuilder zusammenfassung = new StringBuilder(adressbezeichnung).append(System.lineSeparator());

        if (beginn == null || ende.isBefore(beginn)) {
            zusammenfassung.append("   Adresse enthält keine Flächen im Abrechnungszeitraum.").append(System.lineSeparator());
        } else {
            zusammenfassung
                    .append("   50%-Aufschlag : ").append(isAufschlag50Prozent).append(System.lineSeparator())
                    .append("   Beginn : ").append(beginn).append(" (Woche : ").append(flaechen.getFirst().ersteWoche()).append(')')
                    .append(System.lineSeparator())
                    .append("   Ende : ").append(ende).append(" (Woche : ").append(letzteWoche).append(')').append(System.lineSeparator());
            flaechen.forEach(flaeche -> zusammenfassung.append(flaeche.zusammenfassung()));
        }

        return zusammenfassung.append(System.lineSeparator()).toString();
    }
}
