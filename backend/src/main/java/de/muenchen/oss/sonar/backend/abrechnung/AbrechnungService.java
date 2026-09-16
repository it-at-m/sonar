package de.muenchen.oss.sonar.backend.abrechnung;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NEWER_VERSION_ALREADY_EXISTS;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.common.ConflictException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import de.muenchen.oss.sonar.backend.widerspruch.WiderspruchService;
import de.muenchen.oss.sonar.backend.widerspruch.domain.Widerspruch;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
public class AbrechnungService {

    private static final AbrechnungSortBy DEFAULT_SORT_BY = AbrechnungSortBy.ZEITRAUM_VON;

    private static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.DESC;

    private static final String TIEBREAKER_ATTRIBUTE = "id";

    private static final int FIRST_VERSION_NUMBER = 1;

    private final AbrechnungRepository abrechnungRepository;
    private final ProjektService projektService;
    private final WiderspruchService widerspruchService;
    private final AbrechnungEntityMapper abrechnungEntityMapper;

    @Transactional(readOnly = true)
    public Page<Abrechnung> getAbrechnungenOfProjekt(final UUID projektId, final int pageNumber, final int pageSize,
            final List<AbrechnungSortBy> sortBy, final List<Sort.Direction> directions) {
        if (!projektService.existsProjekt(projektId)) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, projektId));
        }
        final Sort sort = resolveSortWithInputOrDefaults(sortBy, directions);
        log.info("Get Abrechnungen of Projekt {} at Page {} with a PageSize of {} ordered by {}", projektId, pageNumber, pageSize, sort);
        final Pageable pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        final Page<AbrechnungEntity> page = abrechnungRepository.findNewestVersionsByProjektId(projektId, pageRequest);
        final Map<UUID, Widerspruch> widerspruecheByAbrechnungId = widerspruchService.getWiderspruecheOfAbrechnungen(
                page.getContent().stream().map(AbrechnungEntity::getId).toList());
        // The page holds the newest version of each Abrechnung, so none of them has a newer one.
        return page.map(abrechnungEntity -> abrechnungEntityMapper.toAbrechnung(abrechnungEntity,
                widerspruecheByAbrechnungId.containsKey(abrechnungEntity.getId()), false));
    }

    @Transactional(readOnly = true)
    public Abrechnung getAbrechnung(final UUID projektId, final UUID abrechnungId) {
        log.info("Get Abrechnung {} of Projekt {}", abrechnungId, projektId);
        final AbrechnungEntity abrechnungEntity = abrechnungRepository.findByIdAndProjektId(abrechnungId, projektId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, abrechnungId)));
        return abrechnungEntityMapper.toAbrechnung(abrechnungEntity,
                widerspruchService.getWiderspruchOfAbrechnung(abrechnungId).isPresent(),
                abrechnungRepository.existsByVorgaengerAbrechnungId(abrechnungId));
    }

    @Transactional
    public Abrechnung createAbrechnung(final Abrechnung abrechnung) {
        if (!projektService.existsProjekt(abrechnung.projektId())) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, abrechnung.projektId()));
        }
        final AbrechnungEntity abrechnungEntity = abrechnungEntityMapper.toEntity(abrechnung);
        abrechnungEntity.setVersionsnummer(FIRST_VERSION_NUMBER);
        log.debug("Create Abrechnung {}", abrechnungEntity);
        // A Widerspruch is filed against an existing Abrechnung, so a new one never carries one.
        return abrechnungEntityMapper.toAbrechnung(abrechnungRepository.save(abrechnungEntity), false, false);
    }

    /**
     * Only the latest version can be carried forward, so a Vorgänger that already has a Nachfolger is
     * refused. That keeps the versions of an Abrechnung a chain instead of a tree.
     */
    @Transactional
    public Abrechnung createNextVersion(final UUID vorgaengerAbrechnungId, final Abrechnung abrechnung) {
        final AbrechnungEntity vorgaenger = abrechnungRepository.findByIdAndProjektId(vorgaengerAbrechnungId, abrechnung.projektId())
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, vorgaengerAbrechnungId)));
        if (abrechnungRepository.existsByVorgaengerAbrechnungId(vorgaengerAbrechnungId)) {
            throw new ConflictException(String.format(MSG_NEWER_VERSION_ALREADY_EXISTS, vorgaengerAbrechnungId));
        }
        final AbrechnungEntity nextVersion = abrechnungEntityMapper.toEntity(abrechnung);
        nextVersion.setVersionsnummer(vorgaenger.getVersionsnummer() + 1);
        nextVersion.setVorgaengerAbrechnungId(vorgaengerAbrechnungId);
        log.debug("Create next version of Abrechnung {}: {}", vorgaengerAbrechnungId, nextVersion);
        // A Widerspruch is filed against an existing Abrechnung, and the new version is the latest one.
        return abrechnungEntityMapper.toAbrechnung(abrechnungRepository.save(nextVersion), false, false);
    }

    private Sort resolveSortWithInputOrDefaults(final List<AbrechnungSortBy> sortBy, final List<Sort.Direction> directions) {
        final List<AbrechnungSortBy> requestedColumns = sortBy == null ? List.of() : sortBy;
        final Set<AbrechnungSortBy> orderedColumns = EnumSet.noneOf(AbrechnungSortBy.class);
        final List<Sort.Order> orders = new ArrayList<>();
        for (int position = 0; position < requestedColumns.size(); position++) {
            final AbrechnungSortBy column = requestedColumns.get(position);
            if (column != null && orderedColumns.add(column)) {
                orders.add(new Sort.Order(directionAtPosition(directions, position), column.getEntityAttribute()));
            }
        }
        if (orders.isEmpty()) {
            orders.add(new Sort.Order(directionAtPosition(directions, 0), DEFAULT_SORT_BY.getEntityAttribute()));
        }
        orders.add(new Sort.Order(orders.getLast().getDirection(), TIEBREAKER_ATTRIBUTE));
        return Sort.by(orders);
    }

    private Sort.Direction directionAtPosition(final List<Sort.Direction> directions, final int position) {
        if (directions == null || position >= directions.size()) {
            return DEFAULT_DIRECTION;
        }
        final Sort.Direction direction = directions.get(position);
        return direction == null ? DEFAULT_DIRECTION : direction;
    }
}
