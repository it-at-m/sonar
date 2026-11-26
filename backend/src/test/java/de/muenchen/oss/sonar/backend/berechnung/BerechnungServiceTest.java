package de.muenchen.oss.sonar.backend.berechnung;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungService;
import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungsArt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnungsergebnis;
import de.muenchen.oss.sonar.backend.berechnung.flaechen.BescheiddatenFlaeche;
import de.muenchen.oss.sonar.backend.common.Adressart;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import de.muenchen.oss.sonar.backend.projekt.domain.LinkedProjekt;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import de.muenchen.oss.sonar.backend.security.AuthUtils;
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

    private static final LocalDate BEGINN = LocalDate.of(2025, 1, 6);
    private static final LocalDate ENDE = LocalDate.of(2025, 1, 26);
    private static final LocalDate BERECHNUNGSDATUM = LocalDate.of(2025, 12, 31);

    @Mock
    private ProjektService projektService;

    @Mock
    private AbrechnungService abrechnungService;

    @InjectMocks
    private BerechnungService unitUnderTest;

    @Nested
    class PreviewBerechnung {

        @Test
        void givenOneFlaecheOverThreeWochen_thenChargeTheRecordedGebuehr() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), BEGINN, ENDE,
                    new BigDecimal("50.00"), new BigDecimal("1.00"), new BigDecimal("50.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Musterstrasse", "1", null, null, null, null, null, null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null,
                    BEGINN, ENDE, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID))
                    .thenReturn(new Projekt(PROJEKT_ID, "P-0001", BEGINN, ENDE, BigDecimal.ZERO, List.of(), null, List.of(), List.of(), false, false));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Berechnungsergebnis ergebnis = unitUnderTest.previewBerechnung(PROJEKT_ID, ABRECHNUNG_ID, BERECHNUNGSDATUM);

            assertThat(ergebnis.gebuehrFlaechen()).isEqualByComparingTo("225");
            assertThat(ergebnis.gebuehrUeberspannungen()).isEqualByComparingTo("0");
            assertThat(ergebnis.gebuehrVerwaltung()).isEqualByComparingTo("0");
            assertThat(ergebnis.gebuehrGesamt()).isEqualByComparingTo("225");
            assertThat(ergebnis.abrechnungszeitraumVon()).isEqualTo(BEGINN);
            assertThat(ergebnis.abrechnungszeitraumBis()).isEqualTo(ENDE);
            assertThat(ergebnis.bescheiddatenFlaechen()).singleElement().satisfies(bescheiddaten -> {
                assertThat(bescheiddaten.wocheBeginn()).isEqualTo(1);
                assertThat(bescheiddaten.wocheEnde()).isEqualTo(3);
                assertThat(bescheiddaten.wocheDauer()).isEqualTo(3);
                assertThat(bescheiddaten.gebuehr()).isEqualByComparingTo("225");
            });
            assertThat(ergebnis.abrechnungenUeberspannungen()).isEmpty();
        }

        @Test
        void givenNoVorabrechnung_thenStartAtLfdNrOneAndChargeTheWholeBetrag() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), BEGINN, ENDE,
                    new BigDecimal("50.00"), new BigDecimal("1.00"), new BigDecimal("50.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Musterstrasse", "1", null, null, null, null, null, null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null,
                    BEGINN, ENDE, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID))
                    .thenReturn(new Projekt(PROJEKT_ID, "P-0001", BEGINN, ENDE, new BigDecimal("42"), List.of(), null, List.of(), List.of(), false, false));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Berechnungsergebnis ergebnis = unitUnderTest.previewBerechnung(PROJEKT_ID, ABRECHNUNG_ID, BERECHNUNGSDATUM);

            assertThat(ergebnis.lfdNr()).isEqualTo(1);
            assertThat(ergebnis.gebuehrVerwaltung()).isEqualByComparingTo("42");
            assertThat(ergebnis.gebuehrGesamt()).isEqualByComparingTo("267");
            assertThat(ergebnis.gebuehrZahlung()).isEqualByComparingTo(ergebnis.gebuehrGesamt());
        }

        @Test
        void givenAPositionBeginningAfterTheBerechnungsdatum_thenChargeNothing() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(),
                    LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 22),
                    new BigDecimal("50.00"), new BigDecimal("1.00"), new BigDecimal("50.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Musterstrasse", "1", null, null, null, null, null, null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null,
                    LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 22), AbrechnungsArt.ENDABRECHNUNG, null, false,
                    List.of(nutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(
                    new Projekt(PROJEKT_ID, "P-0001", LocalDate.of(2026, 3, 2), LocalDate.of(2026, 3, 22), BigDecimal.ZERO, List.of(), null, List.of(),
                            List.of(), false, false));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Berechnungsergebnis ergebnis = unitUnderTest.previewBerechnung(PROJEKT_ID, ABRECHNUNG_ID, LocalDate.of(2026, 1, 31));

            assertThat(ergebnis.gebuehrFlaechen()).isEqualByComparingTo("0");
            assertThat(ergebnis.gebuehrGesamt()).isEqualByComparingTo("0");
        }

        @Test
        void givenAPositionRunningBeyondTheBerechnungsdatum_thenChargeOnlyUpToIt() {
            final LocalDate berechnungsdatumWaehrendDerPosition = LocalDate.of(2025, 1, 26);
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), BEGINN, LocalDate.of(2025, 3, 30),
                    new BigDecimal("50.00"), new BigDecimal("1.00"), new BigDecimal("50.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Musterstrasse", "1", null, null, null, null, null, null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null,
                    BEGINN, LocalDate.of(2025, 3, 30), AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(
                    new Projekt(PROJEKT_ID, "P-0001", BEGINN, LocalDate.of(2025, 3, 30), BigDecimal.ZERO, List.of(), null, List.of(), List.of(), false, false));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Berechnungsergebnis ergebnis = unitUnderTest.previewBerechnung(PROJEKT_ID, ABRECHNUNG_ID,
                    berechnungsdatumWaehrendDerPosition);

            assertThat(ergebnis.gebuehrFlaechen()).isEqualByComparingTo("225");
            assertThat(ergebnis.gebuehrGesamt()).isEqualByComparingTo("225");
            assertThat(ergebnis.abrechnungszeitraumBis()).isEqualTo(berechnungsdatumWaehrendDerPosition);
            assertThat(ergebnis.bescheiddatenFlaechen()).singleElement().satisfies(bescheiddaten -> {
                assertThat(bescheiddaten.wocheEnde()).isEqualTo(3);
                assertThat(bescheiddaten.wocheDauer()).isEqualTo(3);
            });
        }

        @Test
        void givenAnAbrechnung_thenTheZusammenfassungNamesTheProjektAndTheUser() {
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), BEGINN, ENDE,
                    new BigDecimal("50.00"), new BigDecimal("1.00"), new BigDecimal("50.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Musterstrasse", "1", null, null, null, null, null, null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null,
                    BEGINN, ENDE, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID))
                    .thenReturn(new Projekt(PROJEKT_ID, "P-0001", BEGINN, ENDE, BigDecimal.ZERO, List.of(), null, List.of(), List.of(), false, false));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Berechnungsergebnis ergebnis = unitUnderTest.previewBerechnung(PROJEKT_ID, ABRECHNUNG_ID, BERECHNUNGSDATUM);

            assertThat(ergebnis.zusammenfassung())
                    .contains("Abrechnung 1 von Bauprojekt P-0001")
                    .contains("Ersteller Abrechnung : " + AuthUtils.NAME_UNAUTHENTICATED_USER)
                    .contains("Musterstrasse 1");
            assertThat(ergebnis.berechnungslog()).contains("====>>>> Die Gesamtgebühren betragen 225,00€");
        }

        @Test
        void givenVorgaengerWithSeveralAbrechnungen_thenCountTheFlaechenOfAllOfThem() {
            final UUID vorgaengerProjektId = UUID.randomUUID();
            final AbrechnungPosition eigenePosition = new AbrechnungPosition(UUID.randomUUID(), BEGINN, ENDE,
                    new BigDecimal("8.00"), new BigDecimal("10.00"), new BigDecimal("80.00"));
            final AbrechnungNutzungsobjekt eigenesNutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Musterstrasse", "1", null, null, null, null, null, null, null, null, null, false, List.of(eigenePosition), List.of());
            final Abrechnung abrechnung = new Abrechnung(ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null,
                    BEGINN, ENDE, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(eigenesNutzungsobjekt));
            final AbrechnungPosition positionAmMarienplatz = new AbrechnungPosition(UUID.randomUUID(), BEGINN, ENDE,
                    new BigDecimal("6.00"), new BigDecimal("10.00"), new BigDecimal("60.00"));
            final AbrechnungNutzungsobjekt nutzungsobjektAmMarienplatz = new AbrechnungNutzungsobjekt(UUID.randomUUID(),
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, null, false,
                    List.of(positionAmMarienplatz), List.of());
            final Abrechnung abrechnungAmMarienplatz = new Abrechnung(UUID.randomUUID(), vorgaengerProjektId, 1, null, "1000000002",
                    false, null, null, BEGINN, ENDE, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjektAmMarienplatz));
            final AbrechnungPosition positionAmRindermarkt = new AbrechnungPosition(UUID.randomUUID(), BEGINN, ENDE,
                    new BigDecimal("6.00"), new BigDecimal("10.00"), new BigDecimal("60.00"));
            final AbrechnungNutzungsobjekt nutzungsobjektAmRindermarkt = new AbrechnungNutzungsobjekt(UUID.randomUUID(),
                    Adressart.ADRESSE, "Rindermarkt", "3", null, null, null, null, null, null, null, null, null, false,
                    List.of(positionAmRindermarkt), List.of());
            final Abrechnung abrechnungAmRindermarkt = new Abrechnung(UUID.randomUUID(), vorgaengerProjektId, 1, null, "1000000003",
                    false, null, null, BEGINN, ENDE, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjektAmRindermarkt));
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "P-0001", BEGINN, ENDE, BigDecimal.ZERO,
                    List.of(), vorgaengerProjektId, List.of(new LinkedProjekt(vorgaengerProjektId, "2026-0001")), List.of(), false, false));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);
            when(abrechnungService.getNewestAbrechnungenOfProjekt(vorgaengerProjektId))
                    .thenReturn(List.of(abrechnungAmMarienplatz, abrechnungAmRindermarkt));

            final Berechnungsergebnis ergebnis = unitUnderTest.previewBerechnung(PROJEKT_ID, ABRECHNUNG_ID, BERECHNUNGSDATUM);

            assertThat(ergebnis.bescheiddatenFlaechen()).singleElement()
                    .extracting(BescheiddatenFlaeche::gebuehrenstufe)
                    .isEqualTo("200m² --> Gebührenstufe 3" + System.lineSeparator());
        }

        @Test
        void givenSeveralVorgaenger_thenMergeTheNewestAbrechnungenOfEachOfThem() {
            final UUID vorgaengerProjekt0001Id = UUID.randomUUID();
            final UUID vorgaengerProjekt0003Id = UUID.randomUUID();
            final AbrechnungPosition eigenePosition = new AbrechnungPosition(UUID.randomUUID(), BEGINN, ENDE,
                    new BigDecimal("8.00"), new BigDecimal("10.00"), new BigDecimal("80.00"));
            final AbrechnungNutzungsobjekt eigenesNutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Musterstrasse", "1", null, null, null, null, null, null, null, null, null, false, List.of(eigenePosition), List.of());
            final Abrechnung abrechnung = new Abrechnung(ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null,
                    BEGINN, ENDE, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(eigenesNutzungsobjekt));
            final AbrechnungPosition positionAmMarienplatz = new AbrechnungPosition(UUID.randomUUID(), BEGINN, ENDE,
                    new BigDecimal("6.00"), new BigDecimal("10.00"), new BigDecimal("60.00"));
            final AbrechnungNutzungsobjekt nutzungsobjektAmMarienplatz = new AbrechnungNutzungsobjekt(UUID.randomUUID(),
                    Adressart.ADRESSE, "Marienplatz", "8", null, null, null, null, null, null, null, null, null, false,
                    List.of(positionAmMarienplatz), List.of());
            final Abrechnung abrechnungAmMarienplatz = new Abrechnung(UUID.randomUUID(), vorgaengerProjekt0001Id, 1, null, "1000000002",
                    false, null, null, BEGINN, ENDE, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjektAmMarienplatz));
            final AbrechnungPosition positionAmRindermarkt = new AbrechnungPosition(UUID.randomUUID(), BEGINN, ENDE,
                    new BigDecimal("6.00"), new BigDecimal("10.00"), new BigDecimal("60.00"));
            final AbrechnungNutzungsobjekt nutzungsobjektAmRindermarkt = new AbrechnungNutzungsobjekt(UUID.randomUUID(),
                    Adressart.ADRESSE, "Rindermarkt", "3", null, null, null, null, null, null, null, null, null, false,
                    List.of(positionAmRindermarkt), List.of());
            final Abrechnung abrechnungAmRindermarkt = new Abrechnung(UUID.randomUUID(), vorgaengerProjekt0003Id, 1, null, "1000000003",
                    false, null, null, BEGINN, ENDE, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjektAmRindermarkt));
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "P-0001", BEGINN, ENDE, BigDecimal.ZERO,
                    List.of(), vorgaengerProjekt0003Id, List.of(new LinkedProjekt(vorgaengerProjekt0001Id, "2026-0001"),
                            new LinkedProjekt(vorgaengerProjekt0003Id, "2026-0003")),
                    List.of(), false, false));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);
            when(abrechnungService.getNewestAbrechnungenOfProjekt(vorgaengerProjekt0001Id)).thenReturn(List.of(abrechnungAmMarienplatz));
            when(abrechnungService.getNewestAbrechnungenOfProjekt(vorgaengerProjekt0003Id)).thenReturn(List.of(abrechnungAmRindermarkt));

            final Berechnungsergebnis ergebnis = unitUnderTest.previewBerechnung(PROJEKT_ID, ABRECHNUNG_ID, BERECHNUNGSDATUM);

            assertThat(ergebnis.bescheiddatenFlaechen()).singleElement()
                    .extracting(BescheiddatenFlaeche::gebuehrenstufe)
                    .isEqualTo("200m² --> Gebührenstufe 3" + System.lineSeparator());
            assertThat(ergebnis.zusammenfassung())
                    .contains("Projekt 2026-0001, Marienplatz 8")
                    .contains("Projekt 2026-0003, Rindermarkt 3");
        }

        @Test
        void givenNachfolger_thenLoadNoOtherAbrechnung() {
            final UUID nachfolgerProjektId = UUID.randomUUID();
            final AbrechnungPosition position = new AbrechnungPosition(UUID.randomUUID(), BEGINN, ENDE,
                    new BigDecimal("8.00"), new BigDecimal("10.00"), new BigDecimal("80.00"));
            final AbrechnungNutzungsobjekt nutzungsobjekt = new AbrechnungNutzungsobjekt(UUID.randomUUID(), Adressart.ADRESSE,
                    "Musterstrasse", "1", null, null, null, null, null, null, null, null, null, false, List.of(position), List.of());
            final Abrechnung abrechnung = new Abrechnung(ABRECHNUNG_ID, PROJEKT_ID, 1, null, "1000000001", false, null, null,
                    BEGINN, ENDE, AbrechnungsArt.ENDABRECHNUNG, null, false, List.of(nutzungsobjekt));
            when(projektService.getProjekt(PROJEKT_ID)).thenReturn(new Projekt(PROJEKT_ID, "P-0001", BEGINN, ENDE, BigDecimal.ZERO,
                    List.of(), null, List.of(), List.of(new LinkedProjekt(nachfolgerProjektId, "2026-0002")), false, false));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenReturn(abrechnung);

            final Berechnungsergebnis ergebnis = unitUnderTest.previewBerechnung(PROJEKT_ID, ABRECHNUNG_ID, BERECHNUNGSDATUM);

            verify(abrechnungService, never()).getNewestAbrechnungenOfProjekt(any());
            assertThat(ergebnis.bescheiddatenFlaechen()).singleElement()
                    .extracting(BescheiddatenFlaeche::gebuehrenstufe)
                    .isEqualTo("80m² --> Gebührenstufe 2" + System.lineSeparator());
            assertThat(ergebnis.zusammenfassung()).doesNotContain("2026-0002");
        }

        @Test
        void givenAnUnknownAbrechnung_thenThrowNotFound() {
            when(projektService.getProjekt(PROJEKT_ID))
                    .thenReturn(new Projekt(PROJEKT_ID, "P-0001", BEGINN, ENDE, BigDecimal.ZERO, List.of(), null, List.of(), List.of(), false, false));
            when(abrechnungService.getAbrechnung(PROJEKT_ID, ABRECHNUNG_ID)).thenThrow(new NotFoundException("not found"));

            assertThatThrownBy(() -> unitUnderTest.previewBerechnung(PROJEKT_ID, ABRECHNUNG_ID, BERECHNUNGSDATUM))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void givenAnUnknownProjekt_thenThrowNotFound() {
            when(projektService.getProjekt(PROJEKT_ID)).thenThrow(new NotFoundException("not found"));

            assertThatThrownBy(() -> unitUnderTest.previewBerechnung(PROJEKT_ID, ABRECHNUNG_ID, BERECHNUNGSDATUM))
                    .isInstanceOf(NotFoundException.class);
        }
    }
}
