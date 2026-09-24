package de.muenchen.oss.sonar.backend.berechnung;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungService;
import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungsArt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.berechnung.abrechnung.Abrechnung;
import de.muenchen.oss.sonar.backend.berechnung.abrechnung.BescheiddatenFlaeche;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BerechnungServiceTest {

    private static final UUID PROJEKT_ID = UUID.randomUUID();
    private static final UUID ABRECHNUNG_ID = UUID.randomUUID();

    private static final LocalDate VON = LocalDate.of(2026, 1, 1);
    private static final LocalDate BIS = LocalDate.of(2026, 3, 31);

    /** Kept in step with the placeholder in the service, so the expected totals stay readable. */
    private static final BigDecimal VERWALTUNGSGEBUEHR = new BigDecimal("25");

    @Mock
    private ProjektService projektService;

    @Mock
    private AbrechnungService abrechnungService;

    @InjectMocks
    private BerechnungService unitUnderTest;

    @Nested
    class BerechnungDurchfuehren {

        @Test
        void givenOnePositionOfOneWeek_thenChargeTheFlaecheOnce() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), false, new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung abrechnung = new de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung(
                    ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, false, false, List.of(nutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0001", VON, BIS, List.of()));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Abrechnung ergebnis = unitUnderTest.berechnungDurchfuehren(PROJEKT_ID, ABRECHNUNG_ID);

            // Gebührenstufe 1 (20 m²) and Zeitindex 1 charge 1,50 € per m² for the single week.
            assertThat(ergebnis.getGebuehrFlaechen()).isEqualByComparingTo("30.00");
            assertThat(ergebnis.getGebuehrUeberspannungen()).isEqualByComparingTo("0");
            assertThat(ergebnis.getGebuehrVerwaltung()).isEqualByComparingTo(VERWALTUNGSGEBUEHR);
            assertThat(ergebnis.getGebuehrGesamt()).isEqualByComparingTo("55.00");
        }

        @Test
        void givenNoVorgaenger_thenStartAtLfdNrOneAndChargeTheWholeBetrag() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), false, new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung abrechnung = new de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung(
                    ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, false, false, List.of(nutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0001", VON, BIS, List.of()));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Abrechnung ergebnis = unitUnderTest.berechnungDurchfuehren(PROJEKT_ID, ABRECHNUNG_ID);

            assertThat(ergebnis.getLfdNr()).isEqualTo(1);
            assertThat(ergebnis.getGebuehrZahlung()).isEqualByComparingTo(ergebnis.getGebuehrGesamt());
        }

        @Test
        void givenPositionen_thenTakeTheZeitraumFromThemAndNotFromTheAbrechnung() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), false, new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung abrechnung = new de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung(
                    ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, false, false, List.of(nutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0001", VON, BIS, List.of()));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Abrechnung ergebnis = unitUnderTest.berechnungDurchfuehren(PROJEKT_ID, ABRECHNUNG_ID);

            assertThat(ergebnis.getAbrechnungszeitraumVon()).isEqualTo(LocalDate.of(2026, 2, 1));
            assertThat(ergebnis.getAbrechnungszeitraumBis()).isEqualTo(LocalDate.of(2026, 2, 7));
        }

        @Test
        void givenSeveralPositionenOfOneNutzungsobjekt_thenChargeEachPositionWithItsOwnFlaeche() {
            final AbrechnungPosition erstePosition = new AbrechnungPosition(UUID.randomUUID(), VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), false, new BigDecimal("20.00"));
            final AbrechnungPosition zweitePosition = new AbrechnungPosition(UUID.randomUUID(), LocalDate.of(2026, 1, 8), LocalDate.of(2026, 1, 14),
                    new BigDecimal("10.00"), new BigDecimal("3.50"), new BigDecimal("35.00"), false, new BigDecimal("35.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(erstePosition, zweitePosition));
            final de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung abrechnung = new de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung(
                    ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, false, false, List.of(nutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0001", VON, BIS, List.of()));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Abrechnung ergebnis = unitUnderTest.berechnungDurchfuehren(PROJEKT_ID, ABRECHNUNG_ID);

            // Week 1 charges 20 m² at Gebührenstufe 1, week 2 charges 55 m² at Gebührenstufe 2, both at
            // Zeitindex 1 and therefore at 1,50 € per m².
            assertThat(ergebnis.getGebuehrFlaechen()).isEqualByComparingTo("112.50");
        }

        @Test
        void givenSeveralNutzungsobjekte_thenTheGebuehrenstufeFollowsFromTheirSum() {
            final AbrechnungPosition erstePosition = new AbrechnungPosition(UUID.randomUUID(), VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("4.00"), new BigDecimal("40.00"), false, new BigDecimal("40.00"));
            final AbrechnungPosition zweitePosition = new AbrechnungPosition(UUID.randomUUID(), VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), false, new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt erstesNutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(erstePosition));
            final AbrechnungNutzungsobjekt zweitesNutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.FLURSTUECK,
                    null, null, null, "123/4", "Sendling", null, null, null, null, null, List.of(zweitePosition));
            final de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung abrechnung = new de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung(
                    ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, false, false, List.of(erstesNutzungsobjekt, zweitesNutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0001", VON, BIS, List.of()));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Abrechnung ergebnis = unitUnderTest.berechnungDurchfuehren(PROJEKT_ID, ABRECHNUNG_ID);

            // 40 m² plus 20 m² reach Gebührenstufe 2, which still charges 1,50 € per m² at Zeitindex 1.
            assertThat(ergebnis.getGebuehrFlaechen()).isEqualByComparingTo("90.00");
            assertThat(ergebnis.getZusammenfassung())
                    .contains("Marienplatz 8")
                    .contains("Flurstück 123/4, Sendling");
        }

        @Test
        void givenPositionEndingAfterTheAbrechnungszeitraum_thenStopAtTheEndeOfTheZeitraum() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, BIS,
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), false, new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung abrechnung = new de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung(
                    ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null, VON, LocalDate.of(2026, 1, 14),
                    AbrechnungsArt.ENDABRECHNUNG, false, false, List.of(nutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0001", VON, BIS, List.of()));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Abrechnung ergebnis = unitUnderTest.berechnungDurchfuehren(PROJEKT_ID, ABRECHNUNG_ID);

            // Two weeks of 20 m² at 1,50 € instead of the thirteen weeks the Position itself covers.
            assertThat(ergebnis.getAbrechnungszeitraumBis()).isEqualTo(LocalDate.of(2026, 1, 14));
            assertThat(ergebnis.getGebuehrFlaechen()).isEqualByComparingTo("60.00");
        }

        @Test
        void givenAufschlagOnTheFirstPosition_thenChargeTheWholeNutzungsobjektWithIt() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, LocalDate.of(2026, 1, 7),
                    new BigDecimal("10.00"), new BigDecimal("2.00"), new BigDecimal("20.00"), true, new BigDecimal("20.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung abrechnung = new de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung(
                    ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null, VON, BIS,
                    AbrechnungsArt.ENDABRECHNUNG, false, false, List.of(nutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0001", VON, BIS, List.of()));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Abrechnung ergebnis = unitUnderTest.berechnungDurchfuehren(PROJEKT_ID, ABRECHNUNG_ID);

            // 2,25 € per m² instead of 1,50 €.
            assertThat(ergebnis.getGebuehrFlaechen()).isEqualByComparingTo("45.00");
        }

        @Test
        void givenAZeitindexChange_thenSplitTheBescheiddatenAtThatWeek() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), VON, LocalDate.of(2026, 4, 15),
                    new BigDecimal("20.00"), new BigDecimal("10.00"), new BigDecimal("200.00"), false, new BigDecimal("200.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Marienplatz", "8", null, null, null, null, null, null, null, null, List.of(position));
            final de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung abrechnung = new de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung(
                    ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null, VON, LocalDate.of(2026, 4, 15),
                    AbrechnungsArt.ENDABRECHNUNG, false, false, List.of(nutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0001", VON, BIS, List.of()));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Abrechnung ergebnis = unitUnderTest.berechnungDurchfuehren(PROJEKT_ID, ABRECHNUNG_ID);

            // 200 m² reach Gebührenstufe 3. Weeks 1 to 13 run at Zeitindex 1 and 1,50 € per m², weeks 14
            // and 15 at Zeitindex 2 and 2,50 € per m².
            final List<BescheiddatenFlaeche> bescheiddaten = ergebnis.getBescheiddatenFlaechen();
            assertThat(bescheiddaten).hasSize(2);
            assertThat(bescheiddaten.get(0).getWocheBeginn()).isEqualTo(1);
            assertThat(bescheiddaten.get(0).getWocheEnde()).isEqualTo(13);
            assertThat(bescheiddaten.get(0).getGebuehr()).isEqualByComparingTo("3900.00");
            assertThat(bescheiddaten.get(1).getWocheBeginn()).isEqualTo(14);
            assertThat(bescheiddaten.get(1).getWocheEnde()).isEqualTo(15);
            assertThat(bescheiddaten.get(1).getGebuehr()).isEqualByComparingTo("1000.00");
            assertThat(ergebnis.getGebuehrFlaechen()).isEqualByComparingTo("4900.00");
        }

        @Test
        void givenUnknownAbrechnung_thenThrowNotFoundException() {
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "2026-0001", VON, BIS, List.of()));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenThrow(new NotFoundException("not found"));

            assertThatThrownBy(() -> unitUnderTest.berechnungDurchfuehren(PROJEKT_ID, ABRECHNUNG_ID))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void givenUnknownProjekt_thenThrowNotFoundException() {
            when(projektService.getProjekt(PROJEKT_ID)).thenThrow(new NotFoundException("not found"));

            assertThatThrownBy(() -> unitUnderTest.berechnungDurchfuehren(PROJEKT_ID, ABRECHNUNG_ID))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
