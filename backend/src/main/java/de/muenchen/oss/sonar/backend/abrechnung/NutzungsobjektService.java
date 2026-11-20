package de.muenchen.oss.sonar.backend.abrechnung;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.NutzungsobjektSuggestion;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import de.muenchen.oss.sonar.backend.projekt.domain.LinkedProjekt;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NutzungsobjektService {

    private final AbrechnungRepository abrechnungRepository;
    private final ProjektService projektService;
    private final AbrechnungEntityMapper abrechnungEntityMapper;

    @Transactional(readOnly = true)
    public List<NutzungsobjektSuggestion> getNutzungsobjekteOfProjekt(final UUID projektId) {
        final Projekt projekt = projektService.getProjekt(projektId);
        log.info("Get the Nutzungsobjekte of Projekt {} and of its Vorgaenger", projektId);
        final Map<NutzungsobjektEntity, Set<UUID>> projektIdsJeNutzungsobjekt = abrechnungRepository
                .findNutzungsobjekteImProjektByProjektIdIn(projektIdsOf(projekt)).stream()
                .collect(Collectors.groupingBy(NutzungsobjektImProjekt::nutzungsobjekt, LinkedHashMap::new,
                        Collectors.mapping(NutzungsobjektImProjekt::projektId, Collectors.toSet())));
        return projektIdsJeNutzungsobjekt.entrySet().stream()
                .map(entry -> new NutzungsobjektSuggestion(
                        abrechnungEntityMapper.toNutzungsobjekt(entry.getKey(), List.of(), List.of()),
                        vorgaengerProjektnummer(projekt, entry.getValue())))
                .toList();
    }

    public Map<UUID, NutzungsobjektEntity> selectedPreexistingNutzungsobjekte(final Abrechnung abrechnung) {
        final Set<UUID> selectedIds = abrechnung.nutzungsobjekte().stream()
                .map(AbrechnungNutzungsobjekt::id)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (selectedIds.isEmpty()) {
            return Map.of();
        }
        final Map<UUID, NutzungsobjektEntity> selected = abrechnungRepository
                .findNutzungsobjekteImProjektByProjektIdIn(projektIdsOf(projektService.getProjekt(abrechnung.projektId()))).stream()
                .map(NutzungsobjektImProjekt::nutzungsobjekt)
                .distinct()
                .filter(nutzungsobjekt -> selectedIds.contains(nutzungsobjekt.getId()))
                .collect(Collectors.toMap(NutzungsobjektEntity::getId, nutzungsobjekt -> nutzungsobjekt));
        for (final UUID selectedId : selectedIds) {
            if (!selected.containsKey(selectedId)) {
                throw new NotFoundException(String.format(MSG_NOT_FOUND, selectedId));
            }
        }
        return selected;
    }

    private static List<UUID> projektIdsOf(final Projekt projekt) {
        return Stream.concat(Stream.of(projekt.id()), projekt.vorgaengerProjekte().stream().map(LinkedProjekt::id)).toList();
    }

    private static String vorgaengerProjektnummer(final Projekt projekt, final Set<UUID> projektIdsOfNutzungsobjekt) {
        if (projektIdsOfNutzungsobjekt.contains(projekt.id())) {
            return null;
        }
        return projekt.vorgaengerProjekte().reversed().stream()
                .filter(vorgaengerProjekt -> projektIdsOfNutzungsobjekt.contains(vorgaengerProjekt.id()))
                .map(LinkedProjekt::projektnummer)
                .findFirst()
                .orElse(null);
    }

}
