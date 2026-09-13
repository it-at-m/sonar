package de.muenchen.oss.sonar.backend.abrechnung;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
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

    private final AbrechnungRepository abrechnungRepository;
    private final ProjektService projektService;
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
        return abrechnungRepository
                .findByProjektId(projektId, pageRequest)
                .map(abrechnungEntityMapper::toAbrechnung);
    }

    @Transactional
    public Abrechnung createAbrechnung(final Abrechnung abrechnung) {
        if (!projektService.existsProjekt(abrechnung.projektId())) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, abrechnung.projektId()));
        }
        final AbrechnungEntity abrechnungEntity = abrechnungEntityMapper.toEntity(abrechnung);
        log.debug("Create Abrechnung {}", abrechnungEntity);
        return abrechnungEntityMapper.toAbrechnung(abrechnungRepository.save(abrechnungEntity));
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
