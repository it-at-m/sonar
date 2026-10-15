package de.muenchen.oss.sonar.backend.berechnung.flaechen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.common.Adressart;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class FlaechenadresseTest {

    @Nested
    class From {

        @Test
        void givenPositionenOutOfOrder_thenNumberTheFlaechenByTheirBeginn() {
            final AbrechnungPosition spaetere = new AbrechnungPosition(null, LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 31),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("150"), BigDecimal.ZERO);
            final AbrechnungPosition fruehere = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("100"), BigDecimal.ZERO);
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(spaetere, fruehere), List.of());

            final Flaechenadresse adresse = Flaechenadresse.from(nutzungsobjekt, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1));

            assertThat(adresse.flaechen()).extracting(Flaeche::bezeichnung, Flaeche::beginn)
                    .containsExactly(
                            tuple("Position 1", LocalDate.of(2026, 1, 1)),
                            tuple("Position 2", LocalDate.of(2026, 1, 15)));
        }

        @Test
        void givenSeveralPositionen_thenReadEveryFlaecheAsTheGesamtflaecheOfTheAdresse() {
            final AbrechnungPosition erste = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("100"), BigDecimal.ZERO);
            final AbrechnungPosition zweite = new AbrechnungPosition(null, LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 31),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("150"), BigDecimal.ZERO);
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(erste, zweite), List.of());

            final Flaechenadresse adresse = Flaechenadresse.from(nutzungsobjekt, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1));

            assertThat(adresse.flaechen()).extracting(flaeche -> flaeche.flaechenaenderung().toString())
                    .containsExactly("100", "50");
        }

        @Test
        void givenPositionen_thenEndInTheLatestWocheOfAllFlaechen() {
            final AbrechnungPosition kuerzere = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("100"), BigDecimal.ZERO);
            final AbrechnungPosition laengere = new AbrechnungPosition(null, LocalDate.of(2026, 1, 8), LocalDate.of(2026, 1, 31),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("150"), BigDecimal.ZERO);
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(kuerzere, laengere), List.of());

            final Flaechenadresse adresse = Flaechenadresse.from(nutzungsobjekt, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1));

            assertThat(adresse.letzteWoche()).isEqualTo(5);
        }

        @Test
        void givenNutzungsobjekt_thenTakeItsBezeichnungAndItsAufschlag() {
            final AbrechnungPosition position = new AbrechnungPosition(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31),
                    BigDecimal.TEN, BigDecimal.TEN, new BigDecimal("100"), BigDecimal.ZERO);
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, true, List.of(position), List.of());

            final Flaechenadresse adresse = Flaechenadresse.from(nutzungsobjekt, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1));

            assertThat(adresse.adressbezeichnung()).isEqualTo("Marienplatz 8");
            assertThat(adresse.isAufschlag50Prozent()).isTrue();
        }

        @Test
        void givenNoPositionen_thenHoldNoFlaecheAndNoWoche() {
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, List.of(), List.of());

            final Flaechenadresse adresse = Flaechenadresse.from(nutzungsobjekt, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1));

            assertThat(adresse.flaechen()).isEmpty();
            assertThat(adresse.letzteWoche()).isZero();
        }
    }

    @Nested
    class Beitraege {

        @Test
        void givenSingleFlaeche_thenContributeItsFlaechenaenderung() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 1, 5,
                    new BigDecimal("100"), new BigDecimal("100"));
            final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(flaeche), 5);

            assertThat(adresse.beitraege(1)).extracting(beitrag -> beitrag.flaecheQm().toString())
                    .containsExactly("100");
        }

        @Test
        void givenFlaechenreduzierung_thenTakeTheQuadratmeterFromTheFruehereFlaeche() {
            final Flaeche fruehere = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1, 13,
                    new BigDecimal("100"), new BigDecimal("100"));
            final Flaeche reduzierende = new Flaeche("Position 2", LocalDate.of(2026, 1, 29), LocalDate.of(2026, 3, 31), 5, 13,
                    new BigDecimal("60"), new BigDecimal("-40"));
            final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(fruehere, reduzierende), 13);

            assertThat(adresse.beitraege(5)).extracting(beitrag -> beitrag.flaecheQm().toString())
                    .containsExactly("60", "0");
        }

        @Test
        void givenFlaechenreduzierungBeyondTheErsteFlaeche_thenTakeTheRestFromTheNextOne() {
            final Flaeche erste = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1, 13,
                    new BigDecimal("40"), new BigDecimal("40"));
            final Flaeche zweite = new Flaeche("Position 2", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 3, 31), 3, 13,
                    new BigDecimal("100"), new BigDecimal("60"));
            final Flaeche reduzierende = new Flaeche("Position 3", LocalDate.of(2026, 1, 29), LocalDate.of(2026, 3, 31), 5, 13,
                    new BigDecimal("20"), new BigDecimal("-80"));
            final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(erste, zweite, reduzierende), 13);

            assertThat(adresse.beitraege(5)).extracting(beitrag -> beitrag.flaecheQm().toString())
                    .containsExactly("0", "20", "0");
        }

        @Test
        void givenWocheBeforeTheFlaechenreduzierung_thenLeaveTheFruehereFlaecheWhole() {
            final Flaeche fruehere = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), 1, 13,
                    new BigDecimal("100"), new BigDecimal("100"));
            final Flaeche reduzierende = new Flaeche("Position 2", LocalDate.of(2026, 1, 29), LocalDate.of(2026, 3, 31), 5, 13,
                    new BigDecimal("60"), new BigDecimal("-40"));
            final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(fruehere, reduzierende), 13);

            assertThat(adresse.beitraege(4)).extracting(beitrag -> beitrag.flaecheQm().toString())
                    .containsExactly("100", "-40");
        }

        @Test
        void givenWoche_thenPassItAndTheLetzteWocheOfTheAdresseOn() {
            final Flaeche flaeche = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 1, 5,
                    new BigDecimal("100"), new BigDecimal("100"));
            final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(flaeche), 5);

            assertThat(adresse.beitraege(3)).extracting(Flaechenbeitrag::woche, Flaechenbeitrag::letzteWocheAdresse)
                    .containsExactly(tuple(3, 5));
        }
    }

    @Nested
    class Beginn {

        @Test
        void givenFlaechen_thenReturnTheEarliestBeginn() {
            final Flaeche fruehere = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 1, 5,
                    new BigDecimal("100"), new BigDecimal("100"));
            final Flaeche spaetere = new Flaeche("Position 2", LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 31), 3, 5,
                    new BigDecimal("150"), new BigDecimal("50"));
            final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(fruehere, spaetere), 5);

            assertThat(adresse.beginn()).contains(LocalDate.of(2026, 1, 1));
        }

        @Test
        void givenNoFlaeche_thenReturnEmpty() {
            final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(), 0);

            assertThat(adresse.beginn()).isEmpty();
        }
    }

    @Nested
    class Ende {

        @Test
        void givenFlaechen_thenReturnTheLatestBerechnungsEnde() {
            final Flaeche kuerzere = new Flaeche("Position 1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15), 1, 3,
                    new BigDecimal("100"), new BigDecimal("100"));
            final Flaeche laengere = new Flaeche("Position 2", LocalDate.of(2026, 1, 8), LocalDate.of(2026, 1, 31), 2, 5,
                    new BigDecimal("150"), new BigDecimal("50"));
            final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(kuerzere, laengere), 5);

            assertThat(adresse.ende()).contains(LocalDate.of(2026, 1, 31));
        }

        @Test
        void givenNoFlaeche_thenReturnEmpty() {
            final Flaechenadresse adresse = new Flaechenadresse("Marienplatz 8", false, List.of(), 0);

            assertThat(adresse.ende()).isEmpty();
        }
    }
}
