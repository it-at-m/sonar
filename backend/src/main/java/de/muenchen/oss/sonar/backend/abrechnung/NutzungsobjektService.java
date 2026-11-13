package de.muenchen.oss.sonar.backend.abrechnung;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@SuppressWarnings("PMD.CouplingBetweenObjects")
@RequiredArgsConstructor
public class NutzungsobjektService {

    private final AbrechnungRepository abrechnungRepository;
    private final NutzungsobjektRepository nutzungsobjektRepository;
    private final ProjektService projektService;
    private final AbrechnungEntityMapper abrechnungEntityMapper;

    @Transactional(readOnly = true)
    public List<AbrechnungNutzungsobjekt> getNutzungsobjekteOfProjekt(final UUID projektId) {
        if (!projektService.existsProjekt(projektId)) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, projektId));
        }
        log.info("Get the Nutzungsobjekte of Projekt {}", projektId);
        return abrechnungRepository.findNutzungsobjekteOfFinalizedAbrechnungenByProjektId(projektId).stream()
                .map(nutzungsobjekt -> abrechnungEntityMapper.toNutzungsobjekt(nutzungsobjekt, false, List.of(), List.of()))
                .toList();
    }

    public void assignNutzungsobjekte(final AbrechnungEntity draft, final Abrechnung abrechnung) {
        final Map<UUID, NutzungsobjektEntity> nutzungsobjekteToTakeOver = selectedNutzungsobjekteToTakeOver(abrechnung);
        final Map<UUID, NutzungsobjektEntity> ownNutzungsobjekte = ownNutzungsobjekte(draft);
        final Map<UUID, AbrechnungPositionEntity> existingPositionen = draft.getPositionen().stream()
                .collect(Collectors.toMap(AbrechnungPositionEntity::getId, position -> position));
        final Map<UUID, AbrechnungMastEntity> existingMasten = draft.getMasten().stream()
                .collect(Collectors.toMap(AbrechnungMastEntity::getId, mast -> mast));
        final List<NutzungsobjektEntity> nutzungsobjekte = new ArrayList<>();
        final List<AbrechnungPositionEntity> positionen = new ArrayList<>();
        final List<AbrechnungMastEntity> masten = new ArrayList<>();
        for (final AbrechnungNutzungsobjekt nutzungsobjekt : abrechnung.nutzungsobjekte()) {
            final NutzungsobjektEntity nutzungsobjektEntity = toNutzungsobjektEntity(nutzungsobjekt, nutzungsobjekteToTakeOver,
                    ownNutzungsobjekte);
            nutzungsobjekte.add(nutzungsobjektEntity);
            for (final AbrechnungPosition position : nutzungsobjekt.positionen()) {
                final AbrechnungPositionEntity positionEntity = toPositionEntity(position, existingPositionen);
                positionEntity.setNutzungsobjekt(nutzungsobjektEntity);
                positionen.add(positionEntity);
            }
            for (final AbrechnungMast mast : nutzungsobjekt.masten()) {
                final AbrechnungMastEntity mastEntity = toMastEntity(mast, existingMasten);
                mastEntity.setNutzungsobjekt(nutzungsobjektEntity);
                masten.add(mastEntity);
            }
        }
        draft.setNutzungsobjekte(nutzungsobjekte);
        draft.setPositionen(positionen);
        draft.setMasten(masten);
    }

    public Map<UUID, NutzungsobjektEntity> ownNutzungsobjekte(final AbrechnungEntity draft) {
        if (draft.getNutzungsobjekte().isEmpty()) {
            return Map.of();
        }
        final Set<UUID> takenOverIds = abrechnungRepository.findNutzungsobjektIdsOfFinalizedAbrechnungen(
                draft.getNutzungsobjekte().stream().map(NutzungsobjektEntity::getId).toList());
        return draft.getNutzungsobjekte().stream()
                .filter(nutzungsobjekt -> !takenOverIds.contains(nutzungsobjekt.getId()))
                .collect(Collectors.toMap(NutzungsobjektEntity::getId, nutzungsobjekt -> nutzungsobjekt));
    }

    public void deleteUnbilled(final Collection<UUID> nutzungsobjektIds) {
        final List<NutzungsobjektEntity> unbilled = nutzungsobjektRepository.findUnbilledByIdIn(nutzungsobjektIds);
        log.debug("Delete the Nutzungsobjekte that no Abrechnung bills: {}", unbilled);
        nutzungsobjektRepository.deleteAll(unbilled);
    }

    private Map<UUID, NutzungsobjektEntity> selectedNutzungsobjekteToTakeOver(final Abrechnung abrechnung) {
        final Set<UUID> selectedIds = abrechnung.nutzungsobjekte().stream()
                .filter(nutzungsobjekt -> nutzungsobjekt.id() != null && !nutzungsobjekt.changeable())
                .map(AbrechnungNutzungsobjekt::id)
                .collect(Collectors.toSet());
        if (selectedIds.isEmpty()) {
            return Map.of();
        }
        final Map<UUID, NutzungsobjektEntity> selected = abrechnungRepository
                .findNutzungsobjekteOfFinalizedAbrechnungenByProjektId(abrechnung.projektId()).stream()
                .filter(nutzungsobjekt -> selectedIds.contains(nutzungsobjekt.getId()))
                .collect(Collectors.toMap(NutzungsobjektEntity::getId, nutzungsobjekt -> nutzungsobjekt));
        for (final UUID selectedId : selectedIds) {
            if (!selected.containsKey(selectedId)) {
                throw new NotFoundException(String.format(MSG_NOT_FOUND, selectedId));
            }
        }
        return selected;
    }

    private NutzungsobjektEntity toNutzungsobjektEntity(final AbrechnungNutzungsobjekt nutzungsobjekt,
            final Map<UUID, NutzungsobjektEntity> nutzungsobjekteToTakeOver,
            final Map<UUID, NutzungsobjektEntity> ownNutzungsobjekte) {
        if (nutzungsobjekt.id() == null) {
            return abrechnungEntityMapper.toEntity(nutzungsobjekt);
        }
        if (!nutzungsobjekt.changeable()) {
            return nutzungsobjekteToTakeOver.get(nutzungsobjekt.id());
        }
        final NutzungsobjektEntity nutzungsobjektEntity = Optional.ofNullable(ownNutzungsobjekte.get(nutzungsobjekt.id()))
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, nutzungsobjekt.id())));
        abrechnungEntityMapper.updateEntity(nutzungsobjekt, nutzungsobjektEntity);
        nutzungsobjektEntity.setNutzungen(nutzungsobjekt.nutzungen());
        return nutzungsobjektEntity;
    }

    private AbrechnungPositionEntity toPositionEntity(final AbrechnungPosition position,
            final Map<UUID, AbrechnungPositionEntity> existingPositionen) {
        if (position.id() == null) {
            return abrechnungEntityMapper.toEntity(position);
        }
        final AbrechnungPositionEntity positionEntity = Optional.ofNullable(existingPositionen.get(position.id()))
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, position.id())));
        abrechnungEntityMapper.updateEntity(position, positionEntity);
        return positionEntity;
    }

    private AbrechnungMastEntity toMastEntity(final AbrechnungMast mast, final Map<UUID, AbrechnungMastEntity> existingMasten) {
        if (mast.id() == null) {
            return abrechnungEntityMapper.toEntity(mast);
        }
        final AbrechnungMastEntity mastEntity = Optional.ofNullable(existingMasten.get(mast.id()))
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, mast.id())));
        abrechnungEntityMapper.updateEntity(mast, mastEntity);
        return mastEntity;
    }

}
