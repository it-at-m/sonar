package de.muenchen.oss.sonar.backend.projekt;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;

import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.model.CreateProjektCommand;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektFilter;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektModelMapper;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektSortBy;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektView;
import de.muenchen.oss.sonar.backend.security.Authorities;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjektService {

    private static final ProjektSortBy DEFAULT_SORT_BY = ProjektSortBy.PROJEKTNUMMER;

    private static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.DESC;

    /**
     * None of the sortable columns is unique. Without a total order the database may return rows in
     * any order, so the same Projekt could show up on two pages or on none.
     */
    private static final String TIEBREAKER_ATTRIBUTE = "id";

    private final ProjektRepository projektRepository;
    private final ProjektModelMapper projektModelMapper;

    @PreAuthorize(Authorities.PROJEKT_GET)
    @Transactional(readOnly = true)
    public ProjektView getProjekt(final UUID projektId) {
        log.info("Get Projekt with ID {}", projektId);
        return projektModelMapper.toView(getEntityOrThrowException(projektId));
    }

    @PreAuthorize(Authorities.PROJEKT_GET_ALL)
    @Transactional(readOnly = true)
    public Page<ProjektView> getAllProjekte(final int pageNumber, final int pageSize, final ProjektFilter filter,
            final ProjektSortBy sortBy, final Sort.Direction direction) {
        final Sort sort = resolveSort(sortBy, direction);
        log.info("Get Projekte at Page {} with a PageSize of {} matching {} ordered by {}", pageNumber, pageSize, filter, sort);
        final Pageable pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        return projektRepository
                .findAll(ProjektSpecifications.matching(filter), pageRequest)
                .map(projektModelMapper::toView);
    }

    @PreAuthorize(Authorities.PROJEKT_CREATE)
    @Transactional
    public ProjektView createProjekt(final CreateProjektCommand createProjektCommand) {
        final Projekt projekt = projektModelMapper.toEntity(createProjektCommand);
        log.debug("Create Projekt {}", projekt);
        return projektModelMapper.toView(projektRepository.save(projekt));
    }

    private static Sort resolveSort(final ProjektSortBy sortBy, final Sort.Direction direction) {
        final ProjektSortBy effectiveSortBy = sortBy == null ? DEFAULT_SORT_BY : sortBy;
        final Sort.Direction effectiveDirection = direction == null ? DEFAULT_DIRECTION : direction;
        return Sort.by(effectiveDirection, effectiveSortBy.getEntityAttribute(), TIEBREAKER_ATTRIBUTE);
    }

    private Projekt getEntityOrThrowException(final UUID projektId) {
        return projektRepository
                .findById(projektId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, projektId)));
    }
}
