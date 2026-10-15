package de.muenchen.oss.sonar.backend.berechnung.ueberspannungen;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.Nutzung;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class UeberspannungsadresseTest {

    @Nested
    class From {

        @Test
        void givenMasten_thenNumberThemInTheOrderTheyWereEntered() {
            final AbrechnungMast erster = new AbrechnungMast(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
            final AbrechnungMast zweiter = new AbrechnungMast(null, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.UEBERSPANNUNG), null,
                    null, null, null, null, false, null, List.of(erster, zweiter));

            final Ueberspannungsadresse adresse = Ueberspannungsadresse.from(nutzungsobjekt);

            assertThat(adresse.masten()).extracting(Mast::nummer).containsExactly(1, 2);
        }

        @Test
        void givenMastenStartingAtDifferentDates_thenCountTheMonateFromTheEarliestMast() {
            final AbrechnungMast spaeter = new AbrechnungMast(null, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30));
            final AbrechnungMast frueher = new AbrechnungMast(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 30));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.UEBERSPANNUNG), null,
                    null, null, null, null, false, null, List.of(spaeter, frueher));

            final Ueberspannungsadresse adresse = Ueberspannungsadresse.from(nutzungsobjekt);

            assertThat(adresse.masten()).extracting(Mast::ersterMonat).containsExactly(3, 1);
            assertThat(adresse.masten()).extracting(Mast::letzterMonat).containsExactly(6, 4);
        }

        @Test
        void givenMasten_thenEndTheAdresseWithTheLastOfThem() {
            final AbrechnungMast kurz = new AbrechnungMast(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));
            final AbrechnungMast lang = new AbrechnungMast(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.UEBERSPANNUNG), null,
                    null, null, null, null, false, null, List.of(kurz, lang));

            final Ueberspannungsadresse adresse = Ueberspannungsadresse.from(nutzungsobjekt);

            assertThat(adresse.letzterMonat()).isEqualTo(6);
        }

        @Test
        void givenNutzungsobjektWithoutMasten_thenCarryNoMonat() {
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, null, null);

            final Ueberspannungsadresse adresse = Ueberspannungsadresse.from(nutzungsobjekt);

            assertThat(adresse.masten()).isEmpty();
            assertThat(adresse.letzterMonat()).isZero();
        }

        @Test
        void givenNutzungsobjekt_thenLabelTheAdresseTheWayTheBescheidPrintsIt() {
            final AbrechnungMast erfasst = new AbrechnungMast(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.UEBERSPANNUNG), null,
                    null, null, null, null, false, null, List.of(erfasst));

            final Ueberspannungsadresse adresse = Ueberspannungsadresse.from(nutzungsobjekt);

            assertThat(adresse.adressbezeichnung()).isEqualTo("Marienplatz 8");
        }
    }

    @Nested
    class EarliestStart {

        @Test
        void givenMasten_thenTakeTheEarliestOfThem() {
            final Mast spaeter = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);
            final Mast frueher = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 30), 1, 4);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(spaeter, frueher), 6);

            assertThat(adresse.earliestStart()).contains(LocalDate.of(2026, 1, 1));
        }

        @Test
        void givenNoMasten_thenGiveNothing() {
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(), 0);

            assertThat(adresse.earliestStart()).isEmpty();
        }
    }

    @Nested
    class LatestEnd {

        @Test
        void givenMasten_thenTakeTheLatestOfThem() {
            final Mast kurz = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), 1, 2);
            final Mast lang = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(kurz, lang), 6);

            assertThat(adresse.latestEnd()).contains(LocalDate.of(2026, 6, 30));
        }

        @Test
        void givenNoMasten_thenGiveNothing() {
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(), 0);

            assertThat(adresse.latestEnd()).isEmpty();
        }
    }

    @Nested
    class ActiveMastenOldestFirst {

        @Test
        void givenMastenOfDifferentAlter_thenPutTheOldestFirst() {
            final Mast juenger = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);
            final Mast aelter = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(juenger, aelter), 6);

            assertThat(adresse.activeMastenOldestFirst(4)).extracting(Mast::nummer).containsExactly(2, 1);
        }

        @Test
        void givenMastenOfTheSameAlter_thenKeepTheOrderTheyWereEnteredIn() {
            final Mast erster = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Mast zweiter = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Mast dritter = new Mast(3, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(erster, zweiter, dritter), 6);

            assertThat(adresse.activeMastenOldestFirst(4)).extracting(Mast::nummer).containsExactly(1, 2, 3);
        }

        @Test
        void givenMastThatHasNotStartedYet_thenLeaveItOut() {
            final Mast laufend = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Mast spaeter = new Mast(2, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3, 6);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(laufend, spaeter), 6);

            assertThat(adresse.activeMastenOldestFirst(2)).extracting(Mast::nummer).containsExactly(1);
        }

        @Test
        void givenMastThatHasEnded_thenLeaveItOut() {
            final Mast beendet = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), 1, 2);
            final Mast laufend = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(beendet, laufend), 6);

            assertThat(adresse.activeMastenOldestFirst(3)).extracting(Mast::nummer).containsExactly(2);
        }

        @Test
        void givenMonatBeforeTheFirstOne_thenFindNoMast() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1, 6);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(mast), 6);

            assertThat(adresse.activeMastenOldestFirst(0)).isEmpty();
        }
    }
}
