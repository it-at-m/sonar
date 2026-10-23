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

class UeberspannungsadresseServiceTest {

    private static final LocalDate BERECHNUNGSDATUM = LocalDate.of(2026, 12, 31);

    @Nested
    class Berechne {

        @Test
        void givenMasten_thenNumberThemInTheOrderTheyWereEntered() {
            final AbrechnungMast erster = new AbrechnungMast(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
            final AbrechnungMast zweiter = new AbrechnungMast(null, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.UEBERSPANNUNG), null,
                    null, null, null, null, false, null, List.of(erster, zweiter));

            final Ueberspannungsadresse adresse = UeberspannungsadresseService.berechne(nutzungsobjekt, BERECHNUNGSDATUM);

            assertThat(adresse.masten()).extracting(Mast::nummer).containsExactly(1, 2);
        }

        @Test
        void givenMastenStartingAtDifferentDates_thenCountTheMonateFromTheEarliestMast() {
            final AbrechnungMast spaeter = new AbrechnungMast(null, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30));
            final AbrechnungMast frueher = new AbrechnungMast(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 30));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.UEBERSPANNUNG), null,
                    null, null, null, null, false, null, List.of(spaeter, frueher));

            final Ueberspannungsadresse adresse = UeberspannungsadresseService.berechne(nutzungsobjekt, BERECHNUNGSDATUM);

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

            final Ueberspannungsadresse adresse = UeberspannungsadresseService.berechne(nutzungsobjekt, BERECHNUNGSDATUM);

            assertThat(adresse.letzterMonat()).isEqualTo(6);
        }

        @Test
        void givenNutzungsobjektWithoutMasten_thenCarryNoMonat() {
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null,
                    null, null, null, null, false, null, null);

            final Ueberspannungsadresse adresse = UeberspannungsadresseService.berechne(nutzungsobjekt, BERECHNUNGSDATUM);

            assertThat(adresse.masten()).isEmpty();
            assertThat(adresse.letzterMonat()).isZero();
        }

        @Test
        void givenNutzungsobjekt_thenLabelTheAdresseTheWayTheBescheidPrintsIt() {
            final AbrechnungMast erfasst = new AbrechnungMast(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.UEBERSPANNUNG), null,
                    null, null, null, null, false, null, List.of(erfasst));

            final Ueberspannungsadresse adresse = UeberspannungsadresseService.berechne(nutzungsobjekt, BERECHNUNGSDATUM);

            assertThat(adresse.adressbezeichnung()).isEqualTo("Marienplatz 8");
        }

        @Test
        void givenMastStartingAfterTheBerechnungsdatum_thenCarryNoMonatForIt() {
            final AbrechnungMast kuenftig = new AbrechnungMast(null, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 9, 30));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.UEBERSPANNUNG), null,
                    null, null, null, null, false, null, List.of(kuenftig));

            final Ueberspannungsadresse adresse = UeberspannungsadresseService.berechne(nutzungsobjekt, LocalDate.of(2026, 3, 31));

            assertThat(adresse.letzterMonat()).isZero();
        }

        @Test
        void givenOneMastStartedAndOneNotYet_thenEndTheAdresseWithTheStartedOne() {
            final AbrechnungMast laufend = new AbrechnungMast(null, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));
            final AbrechnungMast kuenftig = new AbrechnungMast(null, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 9, 30));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(
                    null, Adressart.ADRESSE, "Marienplatz", "8", null, null, null, List.of(Nutzung.UEBERSPANNUNG), null,
                    null, null, null, null, false, null, List.of(laufend, kuenftig));

            final Ueberspannungsadresse adresse = UeberspannungsadresseService.berechne(nutzungsobjekt, LocalDate.of(2026, 3, 31));

            assertThat(adresse.letzterMonat()).isEqualTo(2);
            assertThat(adresse.masten()).extracting(Mast::nummer).containsExactly(1, 2);
        }
    }

    @Nested
    class ActiveMastenOldestFirst {

        @Test
        void givenMastenOfDifferentAlter_thenPutTheOldestFirst() {
            final Mast juenger = new Mast(1, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);
            final Mast aelter = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(juenger, aelter), 6);

            assertThat(UeberspannungsadresseService.activeMastenOldestFirst(adresse, 4)).extracting(Mast::nummer)
                    .containsExactly(2, 1);
        }

        @Test
        void givenMastenOfTheSameAlter_thenKeepTheOrderTheyWereEnteredIn() {
            final Mast erster = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1);
            final Mast zweiter = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1);
            final Mast dritter = new Mast(3, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(erster, zweiter, dritter), 6);

            assertThat(UeberspannungsadresseService.activeMastenOldestFirst(adresse, 4)).extracting(Mast::nummer)
                    .containsExactly(1, 2, 3);
        }

        @Test
        void givenMastThatHasNotStartedYet_thenLeaveItOut() {
            final Mast laufend = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1);
            final Mast spaeter = new Mast(2, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), 3);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(laufend, spaeter), 6);

            assertThat(UeberspannungsadresseService.activeMastenOldestFirst(adresse, 2)).extracting(Mast::nummer)
                    .containsExactly(1);
        }

        @Test
        void givenMastThatHasEnded_thenLeaveItOut() {
            final Mast beendet = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28), 1);
            final Mast laufend = new Mast(2, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(beendet, laufend), 6);

            assertThat(UeberspannungsadresseService.activeMastenOldestFirst(adresse, 3)).extracting(Mast::nummer)
                    .containsExactly(2);
        }

        @Test
        void givenMonatBeforeTheFirstOne_thenFindNoMast() {
            final Mast mast = new Mast(1, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 1);
            final Ueberspannungsadresse adresse = new Ueberspannungsadresse("Marienplatz 8", List.of(mast), 6);

            assertThat(UeberspannungsadresseService.activeMastenOldestFirst(adresse, 0)).isEmpty();
        }
    }
}
