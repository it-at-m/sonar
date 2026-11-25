package de.muenchen.oss.sonar.backend.projekt;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_PROJEKT_ALREADY_LINKED;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_PROJEKT_HAS_NACHFOLGER;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_PROJEKT_LINKED_TO_ITSELF;

import de.muenchen.oss.sonar.backend.common.BadRequestException;
import de.muenchen.oss.sonar.backend.common.ConflictException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.domain.LinkedProjekt;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class VorgaengerService {

    private final ProjektRepository projektRepository;
    private final ProjektEntityMapper projektEntityMapper;

    @Transactional(readOnly = true)
    public List<Projekt> getVorgaengerVorschlaege(final UUID projektId) {
        log.info("Get the Projekte that can become the Vorgaenger of Projekt {}", projektId);
        if (!projektRepository.existsById(projektId)) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, projektId));
        }
        final List<ProjektEntity> vorschlaege = projektRepository.findVorgaengerVorschlaege(Set.of(projektId)).stream()
                .map(VorgaengerVorschlag::vorgaenger)
                .distinct()
                .toList();
        return toProjekte(vorschlaege);
    }

    @Transactional(readOnly = true)
    public UUID getVorgaengerProjektId(final UUID projektId) {
        return projektRepository.findById(projektId).map(ProjektEntity::getVorgaengerProjektId).orElse(null);
    }

    @Transactional
    public Projekt linkToVorgaenger(final UUID projektId, final UUID vorgaengerProjektId) {
        if (projektId.equals(vorgaengerProjektId)) {
            throw new BadRequestException(String.format(MSG_PROJEKT_LINKED_TO_ITSELF, projektId));
        }
        final ProjektEntity projektEntity = projektRepository.findById(projektId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, projektId)));
        checkUnlinked(projektId, projektEntity);
        checkCanBeVorgaenger(vorgaengerProjektId);
        projektEntity.setVorgaengerProjektId(vorgaengerProjektId);
        projektEntity.setVerknuepfungAbgelehnt(false);
        log.debug("Link Projekt {} to its Vorgaenger {}", projektId, vorgaengerProjektId);
        return toProjekt(projektRepository.save(projektEntity));
    }

    @Transactional
    public Projekt declineVorgaenger(final UUID projektId) {
        final ProjektEntity projektEntity = projektRepository.findById(projektId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, projektId)));
        checkUnlinked(projektId, projektEntity);
        log.debug("Decide that Projekt {} gets no Vorgaenger", projektId);
        projektEntity.setVerknuepfungAbgelehnt(true);
        return toProjekt(projektRepository.save(projektEntity));
    }

    @Transactional
    public void unlinkFromVorgaenger(final UUID projektId) {
        final ProjektEntity projektEntity = projektRepository.findById(projektId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, projektId)));
        // Only a Projekt without Nachfolger lets go of its Vorgaenger, so linked Projekte shrink from their end and never break in two.
        if (projektRepository.existsByVorgaengerProjektId(projektId)) {
            throw new ConflictException(String.format(MSG_PROJEKT_HAS_NACHFOLGER, projektId));
        }
        log.debug("Unlink Projekt {} from its Vorgaenger", projektId);
        projektEntity.setVorgaengerProjektId(null);
        projektRepository.save(projektEntity);
    }

    public Projekt toProjekt(final ProjektEntity projektEntity) {
        return toProjekte(List.of(projektEntity)).getFirst();
    }

    public List<Projekt> toProjekte(final List<ProjektEntity> projektEntities) {
        final LoadedProjekte loadedProjekte = loadVorgaengerAndNachfolger(projektEntities);
        final Set<UUID> verknuepfungMoeglichIds = loadVerknuepfungMoeglichIds(projektEntities, loadedProjekte);
        return projektEntities.stream()
                .map(projektEntity -> assembleProjekt(projektEntity, loadedProjekte, verknuepfungMoeglichIds))
                .toList();
    }

    private void checkUnlinked(final UUID projektId, final ProjektEntity projektEntity) {
        if (projektEntity.getVorgaengerProjektId() != null || projektRepository.existsByVorgaengerProjektId(projektId)) {
            throw new ConflictException(String.format(MSG_PROJEKT_ALREADY_LINKED, projektId));
        }
    }

    private void checkCanBeVorgaenger(final UUID vorgaengerProjektId) {
        if (!projektRepository.existsById(vorgaengerProjektId)) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, vorgaengerProjektId));
        }
        if (projektRepository.existsByVorgaengerProjektId(vorgaengerProjektId)) {
            throw new ConflictException(String.format(MSG_PROJEKT_HAS_NACHFOLGER, vorgaengerProjektId));
        }
    }

    private LoadedProjekte loadVorgaengerAndNachfolger(final List<ProjektEntity> projektEntities) {
        final Map<UUID, ProjektEntity> byId = new HashMap<>();
        projektEntities.stream()
                .filter(projektEntity -> projektEntity.getId() != null)
                .forEach(projektEntity -> byId.put(projektEntity.getId(), projektEntity));

        Set<UUID> fehlendeVorgaenger = fehlendeVorgaengerOf(byId.values(), byId);
        while (!fehlendeVorgaenger.isEmpty()) {
            final List<ProjektEntity> vorgaenger = StreamSupport
                    .stream(projektRepository.findAllById(fehlendeVorgaenger).spliterator(), false)
                    .toList();
            vorgaenger.forEach(projektEntity -> byId.put(projektEntity.getId(), projektEntity));
            fehlendeVorgaenger = fehlendeVorgaengerOf(vorgaenger, byId);
        }

        Set<UUID> ohneBekanntenNachfolger = Set.copyOf(byId.keySet());
        while (!ohneBekanntenNachfolger.isEmpty()) {
            final List<ProjektEntity> nachfolger = projektRepository.findByVorgaengerProjektIdIn(ohneBekanntenNachfolger).stream()
                    .filter(projektEntity -> !byId.containsKey(projektEntity.getId()))
                    .toList();
            nachfolger.forEach(projektEntity -> byId.put(projektEntity.getId(), projektEntity));
            ohneBekanntenNachfolger = nachfolger.stream().map(ProjektEntity::getId).collect(Collectors.toSet());
        }

        final Map<UUID, ProjektEntity> nachfolgerByVorgaengerId = byId.values().stream()
                .filter(projektEntity -> projektEntity.getVorgaengerProjektId() != null)
                .collect(Collectors.toMap(ProjektEntity::getVorgaengerProjektId, projektEntity -> projektEntity));
        return new LoadedProjekte(byId, nachfolgerByVorgaengerId);
    }

    private static Set<UUID> fehlendeVorgaengerOf(final Collection<ProjektEntity> projektEntities,
            final Map<UUID, ProjektEntity> bekannteProjekte) {
        return projektEntities.stream()
                .map(ProjektEntity::getVorgaengerProjektId)
                .filter(vorgaengerProjektId -> vorgaengerProjektId != null && !bekannteProjekte.containsKey(vorgaengerProjektId))
                .collect(Collectors.toSet());
    }

    private Set<UUID> loadVerknuepfungMoeglichIds(final List<ProjektEntity> projektEntities, final LoadedProjekte loadedProjekte) {
        final Set<UUID> unlinkedIds = projektEntities.stream()
                .filter(loadedProjekte::isUnlinked)
                .map(ProjektEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (unlinkedIds.isEmpty()) {
            return Set.of();
        }
        return projektRepository.findVorgaengerVorschlaege(unlinkedIds).stream()
                .map(VorgaengerVorschlag::projektId)
                .collect(Collectors.toSet());
    }

    private Projekt assembleProjekt(final ProjektEntity projektEntity, final LoadedProjekte loadedProjekte, final Set<UUID> verknuepfungMoeglichIds) {
        final Set<UUID> visitedIds = new HashSet<>();
        visitedIds.add(projektEntity.getId());

        final List<LinkedProjekt> vorgaengerProjekte = new ArrayList<>();
        ProjektEntity vorgaenger = loadedProjekte.vorgaengerOf(projektEntity);
        while (vorgaenger != null && visitedIds.add(vorgaenger.getId())) {
            vorgaengerProjekte.addFirst(toLinkedProjekt(vorgaenger));
            vorgaenger = loadedProjekte.vorgaengerOf(vorgaenger);
        }

        final List<LinkedProjekt> nachfolgerProjekte = new ArrayList<>();
        ProjektEntity nachfolger = loadedProjekte.nachfolgerOf(projektEntity);
        while (nachfolger != null && visitedIds.add(nachfolger.getId())) {
            nachfolgerProjekte.add(toLinkedProjekt(nachfolger));
            nachfolger = loadedProjekte.nachfolgerOf(nachfolger);
        }

        final boolean verknuepfungMoeglich = projektEntity.getId() != null && verknuepfungMoeglichIds.contains(projektEntity.getId());
        return projektEntityMapper.toProjekt(projektEntity, vorgaengerProjekte, nachfolgerProjekte, verknuepfungMoeglich);
    }

    private static LinkedProjekt toLinkedProjekt(final ProjektEntity projektEntity) {
        return new LinkedProjekt(projektEntity.getId(), projektEntity.getProjektnummer());
    }

    private record LoadedProjekte(Map<UUID, ProjektEntity> byId, Map<UUID, ProjektEntity> nachfolgerByVorgaengerId) {

        private ProjektEntity vorgaengerOf(final ProjektEntity projektEntity) {
            return projektEntity.getVorgaengerProjektId() == null ? null : byId.get(projektEntity.getVorgaengerProjektId());
        }

        private ProjektEntity nachfolgerOf(final ProjektEntity projektEntity) {
            return projektEntity.getId() == null ? null : nachfolgerByVorgaengerId.get(projektEntity.getId());
        }

        private boolean isUnlinked(final ProjektEntity projektEntity) {
            return projektEntity.getVorgaengerProjektId() == null && nachfolgerOf(projektEntity) == null;
        }
    }
}
