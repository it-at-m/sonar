package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungService;
import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnungsergebnis;
import de.muenchen.oss.sonar.backend.berechnung.domain.BerechnungsergebnisService;
import de.muenchen.oss.sonar.backend.berechnung.domain.Berechnungslauf;
import de.muenchen.oss.sonar.backend.berechnung.domain.BerechnungslaufService;
import de.muenchen.oss.sonar.backend.berechnung.domain.VorgaengerProjekt;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import de.muenchen.oss.sonar.backend.security.AuthUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BerechnungService {

    private final ProjektService projektService;
    private final AbrechnungService abrechnungService;

    @Transactional(readOnly = true)
    public Berechnungsergebnis previewBerechnung(final UUID projektId, final UUID abrechnungId, final LocalDate berechnungsdatum) {

        final Projekt projekt = projektService.getProjekt(projektId);
        final Abrechnung abrechnung = abrechnungService.getAbrechnung(projektId, abrechnungId);

        final Berechnungslauf berechnungslauf = BerechnungslaufService.berechne(
                projekt,
                abrechnung,
                vorgaengerProjekteOf(projekt),
                berechnungsdatum,
                null,
                AuthUtils.getUsername());

        final Berechnungsergebnis ergebnis = BerechnungsergebnisService.berechne(berechnungslauf);

        log.info("Preview Berechnung of Abrechnung {} of Projekt {}: Flaechen {}, Ueberspannungen {}, Verwaltung {}, Gesamt {}",
                abrechnungId, projektId, ergebnis.gebuehrFlaechen(), ergebnis.gebuehrUeberspannungen(),
                ergebnis.gebuehrVerwaltung(), ergebnis.gebuehrGesamt());
        return ergebnis;
    }

    private List<VorgaengerProjekt> vorgaengerProjekteOf(final Projekt projekt) {
        return projekt.vorgaengerProjekte().stream()
                .map(vorgaengerProjekt -> new VorgaengerProjekt(vorgaengerProjekt.projektnummer(),
                        nutzungsobjekteOfNewestAbrechnungen(vorgaengerProjekt.id())))
                .toList();
    }

    private List<AbrechnungNutzungsobjekt> nutzungsobjekteOfNewestAbrechnungen(final UUID projektId) {
        return abrechnungService.getNewestAbrechnungenOfProjekt(projektId).stream()
                .flatMap(abrechnung -> abrechnung.nutzungsobjekte().stream())
                .toList();
    }
}
