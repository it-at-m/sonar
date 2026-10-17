package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UeberspannungsmonatTest {

    @Nested
    class Berechne {

        @Test
        void givenSeveralMasten_thenRankThemOldestFirst() {
            final Mast juenger = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);
            final Mast aelter = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(juenger, aelter), 6);

            final Ueberspannungsmonat monat = Ueberspannungsmonat.berechne(adresse, 4);

            assertThat(monat.posten()).extracting(Mastposten::rang).containsExactly(1, 2);
            assertThat(monat.posten()).extracting(posten -> posten.mast().nummer()).containsExactly(2, 1);
        }

        @Test
        void givenMasten_thenAgeEachOfThemFromItsOwnBeginn() {
            final Mast juenger = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);
            final Mast aelter = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(juenger, aelter), 6);

            final Ueberspannungsmonat monat = Ueberspannungsmonat.berechne(adresse, 4);

            assertThat(monat.posten()).extracting(Mastposten::alterInMonaten).containsExactly(4, 2);
        }

        @Test
        void givenMastThatIsNotAktiv_thenLeaveItOutOfTheMonat() {
            final Mast beendet = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), 1, 2);
            final Mast laufend = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(beendet, laufend), 6);

            final Ueberspannungsmonat monat = Ueberspannungsmonat.berechne(adresse, 3);

            assertThat(monat.mastenCount()).isEqualTo(1);
        }

        @Test
        void givenFirstMonat_thenNameTheChangedAnzahlAsGrund() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(mast), 6);

            final Ueberspannungsmonat monat = Ueberspannungsmonat.berechne(adresse, 1);

            assertThat(monat.zwischenabrechnungGrund())
                    .isEqualTo("    Anzahl der Masten hat sich geändert auf 1" + System.lineSeparator());
        }

        @Test
        void givenMonatWithoutAChange_thenGiveNoGrund() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(mast), 6);

            final Ueberspannungsmonat monat = Ueberspannungsmonat.berechne(adresse, 2);

            assertThat(monat.zwischenabrechnungGrund()).isEmpty();
        }

        @Test
        void givenMastJoiningInThisMonat_thenNameTheNewAnzahlAsGrund() {
            final Mast laufend = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Mast neu = new Mast(2, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(laufend, neu), 6);

            final Ueberspannungsmonat monat = Ueberspannungsmonat.berechne(adresse, 3);

            assertThat(monat.zwischenabrechnungGrund())
                    .isEqualTo("    Anzahl der Masten hat sich geändert auf 2" + System.lineSeparator());
        }

        @Test
        void givenMastReachingTheSecondAltersstufe_thenNameThatMastAsGrund() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1, 14);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(mast), 14);

            final Ueberspannungsmonat monat = Ueberspannungsmonat.berechne(adresse, 13);

            assertThat(monat.zwischenabrechnungGrund())
                    .isEqualTo("    Mast 1 wechselt in Altersstufe 2" + System.lineSeparator());
        }

        @Test
        void givenSeveralMastenReachingTheSecondAltersstufe_thenNameThemInTheOrderTheyWereEntered() {
            final Mast erster = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1, 14);
            final Mast zweiter = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1, 14);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(erster, zweiter), 14);

            final Ueberspannungsmonat monat = Ueberspannungsmonat.berechne(adresse, 13);

            assertThat(monat.zwischenabrechnungGrund()).isEqualTo(
                    "    Mast 1 wechselt in Altersstufe 2" + System.lineSeparator()
                            + "    Mast 2 wechselt in Altersstufe 2" + System.lineSeparator());
        }
    }

    @Nested
    class Gebuehr {

        @Test
        void givenThreeMastenInTheirFirstAltersstufe_thenAddUpTheSaetzeOfTheirRaenge() {
            final Mast erster = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1, 14);
            final Mast zweiter = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1, 14);
            final Mast dritter = new Mast(3, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1, 14);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(erster, zweiter, dritter), 14);

            final Ueberspannungsmonat monat = Ueberspannungsmonat.berechne(adresse, 1);

            assertThat(monat.gebuehr()).isEqualTo(new BigDecimal("65.00"));
        }

        @Test
        void givenThreeMastenInTheirSecondAltersstufe_thenChargeTheHoehererSatzForTheWeitererMast() {
            final Mast erster = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1, 14);
            final Mast zweiter = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1, 14);
            final Mast dritter = new Mast(3, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 2, 28), 1, 14);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(erster, zweiter, dritter), 14);

            final Ueberspannungsmonat monat = Ueberspannungsmonat.berechne(adresse, 13);

            assertThat(monat.gebuehr()).isEqualTo(new BigDecimal("72.50"));
        }

        @Test
        void givenNoMast_thenChargeNothing() {
            final Ueberspannungsmonat monat = new Ueberspannungsmonat(1, List.of(), "");

            assertThat(monat.gebuehr()).isEqualTo(BigDecimal.ZERO);
        }
    }
}
