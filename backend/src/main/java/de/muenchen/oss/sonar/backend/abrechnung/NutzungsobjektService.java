package de.muenchen.oss.sonar.backend.abrechnung;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
    public List<AbrechnungNutzungsobjekt> getNutzungsobjekteOfProjekt(final UUID projektId) {
        if (!projektService.existsProjekt(projektId)) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, projektId));
        }
        log.info("Get the Nutzungsobjekte of Projekt {}", projektId);
        return abrechnungRepository.findNutzungsobjekteByProjektId(projektId).stream()
                .map(nutzungsobjekt -> abrechnungEntityMapper.toNutzungsobjekt(nutzungsobjekt, List.of()))
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
                .findNutzungsobjekteByProjektId(abrechnung.projektId()).stream()
                .filter(nutzungsobjekt -> selectedIds.contains(nutzungsobjekt.getId()))
                .collect(Collectors.toMap(NutzungsobjektEntity::getId, nutzungsobjekt -> nutzungsobjekt));
        for (final UUID selectedId : selectedIds) {
            if (!selected.containsKey(selectedId)) {
                throw new NotFoundException(String.format(MSG_NOT_FOUND, selectedId));
            }
        }
        return selected;
    }

}
