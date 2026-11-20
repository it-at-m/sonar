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
import java.time.LocalDate;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjektService {

    private static final ProjektSortBy DEFAULT_SORT_BY = ProjektSortBy.PROJEKTNUMMER;

    private static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.DESC;

    private static final String TIEBREAKER_ATTRIBUTE = "id";

    private final ProjektRepository projektRepository;
    private final ProjektEntityMapper projektEntityMapper;

    @Transactional(readOnly = true)
    public Page<Projekt> getAllProjekte(final int pageNumber, final int pageSize, final String projektnummer,
            final LocalDate abrechnungBeginn, final LocalDate abrechnungEnde, final ProjektSortBy sortBy,
            final Sort.Direction direction, final UUID sharesAdresseWithProjektId) {
        final ProjektFilter filter = new ProjektFilter(projektnummer, abrechnungBeginn, abrechnungEnde, sharesAdresseWithProjektId);
        final Sort sort = resolveSortWithInputOrDefaults(sortBy, direction);
        log.info("Get Projekte at Page {} with a PageSize of {} matching {} ordered by {}", pageNumber, pageSize, filter, sort);
        final Pageable pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        final Page<ProjektEntity> projektEntities = projektRepository.findAll(ProjektSpecifications.matching(filter), pageRequest);
        final LinkedProjekte linkedProjekte = loadLinkedProjekte(projektEntities.getContent());
        final Set<UUID> verknuepfungMoeglichIds = loadVerknuepfungMoeglichIds(projektEntities.getContent(), linkedProjekte);
        return projektEntities.map(projektEntity -> toProjekt(projektEntity, linkedProjekte, verknuepfungMoeglichIds));
    }

    @Transactional(readOnly = true)
    public Projekt getProjekt(final UUID projektId) {
        log.info("Get Projekt {}", projektId);
        return toProjekt(getProjektEntityOrThrow(projektId));
    }

    public boolean existsProjekt(final UUID projektId) {
        return projektRepository.existsById(projektId);
    }

    @Transactional(readOnly = true)
    public UUID getVorgaengerProjektId(final UUID projektId) {
        return projektRepository.findById(projektId).map(ProjektEntity::getVorgaengerProjektId).orElse(null);
    }

    @Transactional
    public Projekt createProjekt(final Projekt projekt) {
        final ProjektEntity projektEntity = projektEntityMapper.toEntity(projekt);
        log.debug("Create Projekt {}", projektEntity);
        return toProjekt(projektRepository.save(projektEntity));
    }

    @Transactional
    public Projekt linkToVorgaenger(final UUID projektId, final UUID vorgaengerProjektId) {
        if (projektId.equals(vorgaengerProjektId)) {
            throw new BadRequestException(String.format(MSG_PROJEKT_LINKED_TO_ITSELF, projektId));
        }
        final ProjektEntity projektEntity = getProjektEntityOrThrow(projektId);
        checkUnlinked(projektId, projektEntity);
        checkCanBeVorgaenger(vorgaengerProjektId);
        projektEntity.setVorgaengerProjektId(vorgaengerProjektId);
        projektEntity.setVerknuepfungAbgelehnt(false);
        log.debug("Link Projekt {} to its Vorgaenger {}", projektId, vorgaengerProjektId);
        return toProjekt(projektRepository.save(projektEntity));
    }

    @Transactional
    public Projekt declineVorgaenger(final UUID projektId) {
        final ProjektEntity projektEntity = getProjektEntityOrThrow(projektId);
        checkUnlinked(projektId, projektEntity);
        log.debug("Decide that Projekt {} gets no Vorgaenger", projektId);
        projektEntity.setVerknuepfungAbgelehnt(true);
        return toProjekt(projektRepository.save(projektEntity));
    }

    @Transactional
    public void unlinkFromVorgaenger(final UUID projektId) {
        final ProjektEntity projektEntity = getProjektEntityOrThrow(projektId);
        // Only a Projekt without Nachfolger lets go of its Vorgaenger, so linked Projekte shrink from their end and never break in two.
        if (projektRepository.existsByVorgaengerProjektId(projektId)) {
            throw new ConflictException(String.format(MSG_PROJEKT_HAS_NACHFOLGER, projektId));
        }
        log.debug("Unlink Projekt {} from its Vorgaenger", projektId);
        projektEntity.setVorgaengerProjektId(null);
        projektRepository.save(projektEntity);
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

    private ProjektEntity getProjektEntityOrThrow(final UUID projektId) {
        return projektRepository
                .findById(projektId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, projektId)));
    }

    private LinkedProjekte loadLinkedProjekte(final List<ProjektEntity> projektEntities) {
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
        return new LinkedProjekte(byId, nachfolgerByVorgaengerId);
    }

    private static Set<UUID> fehlendeVorgaengerOf(final Collection<ProjektEntity> projektEntities,
            final Map<UUID, ProjektEntity> bekannteProjekte) {
        return projektEntities.stream()
                .map(ProjektEntity::getVorgaengerProjektId)
                .filter(vorgaengerProjektId -> vorgaengerProjektId != null && !bekannteProjekte.containsKey(vorgaengerProjektId))
                .collect(Collectors.toSet());
    }

    private Set<UUID> loadVerknuepfungMoeglichIds(final List<ProjektEntity> projektEntities, final LinkedProjekte linkedProjekte) {
        final Set<UUID> unlinkedIds = projektEntities.stream()
                .filter(linkedProjekte::isUnlinked)
                .map(ProjektEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (unlinkedIds.isEmpty()) {
            return Set.of();
        }
        return projektRepository.findAll(ProjektSpecifications.idIn(unlinkedIds)
                .and(ProjektSpecifications.sharesAdresseWithProjektWithoutNachfolger()))
                .stream()
                .map(ProjektEntity::getId)
                .collect(Collectors.toSet());
    }

    private Projekt toProjekt(final ProjektEntity projektEntity) {
        final List<ProjektEntity> projektEntities = List.of(projektEntity);
        final LinkedProjekte linkedProjekte = loadLinkedProjekte(projektEntities);
        return toProjekt(projektEntity, linkedProjekte, loadVerknuepfungMoeglichIds(projektEntities, linkedProjekte));
    }

    private Projekt toProjekt(final ProjektEntity projektEntity, final LinkedProjekte linkedProjekte, final Set<UUID> verknuepfungMoeglichIds) {
        final Set<UUID> visitedIds = new HashSet<>();
        visitedIds.add(projektEntity.getId());

        final List<LinkedProjekt> vorgaengerProjekte = new ArrayList<>();
        ProjektEntity vorgaenger = linkedProjekte.vorgaengerOf(projektEntity);
        while (vorgaenger != null && visitedIds.add(vorgaenger.getId())) {
            vorgaengerProjekte.addFirst(toLinkedProjekt(vorgaenger));
            vorgaenger = linkedProjekte.vorgaengerOf(vorgaenger);
        }

        final List<LinkedProjekt> nachfolgerProjekte = new ArrayList<>();
        ProjektEntity nachfolger = linkedProjekte.nachfolgerOf(projektEntity);
        while (nachfolger != null && visitedIds.add(nachfolger.getId())) {
            nachfolgerProjekte.add(toLinkedProjekt(nachfolger));
            nachfolger = linkedProjekte.nachfolgerOf(nachfolger);
        }

        final boolean verknuepfungMoeglich = projektEntity.getId() != null && verknuepfungMoeglichIds.contains(projektEntity.getId());
        return projektEntityMapper.toProjekt(projektEntity, vorgaengerProjekte, nachfolgerProjekte, verknuepfungMoeglich);
    }

    private static LinkedProjekt toLinkedProjekt(final ProjektEntity projektEntity) {
        return new LinkedProjekt(projektEntity.getId(), projektEntity.getProjektnummer());
    }

    private Sort resolveSortWithInputOrDefaults(final ProjektSortBy sortBy, final Sort.Direction direction) {
        final ProjektSortBy effectiveSortBy = sortBy == null ? DEFAULT_SORT_BY : sortBy;
        final Sort.Direction effectiveDirection = direction == null ? DEFAULT_DIRECTION : direction;
        return Sort.by(effectiveDirection, effectiveSortBy.getEntityAttribute(), TIEBREAKER_ATTRIBUTE);
    }

    private record LinkedProjekte(Map<UUID, ProjektEntity> byId, Map<UUID, ProjektEntity> nachfolgerByVorgaengerId) {

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
