package de.muenchen.oss.sonar.backend.projekt;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;

import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import java.time.LocalDate;
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
public class ProjektService {

    private static final ProjektSortBy DEFAULT_SORT_BY = ProjektSortBy.PROJEKTNUMMER;

    private static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.DESC;

    private static final String TIEBREAKER_ATTRIBUTE = "id";

    private final ProjektRepository projektRepository;
    private final ProjektEntityMapper projektEntityMapper;

    @Transactional(readOnly = true)
    public Page<Projekt> getAllProjekte(final int pageNumber, final int pageSize, final String projektnummer,
            final LocalDate abrechnungBeginn, final LocalDate abrechnungEnde, final ProjektSortBy sortBy,
            final Sort.Direction direction) {
        final ProjektFilter filter = new ProjektFilter(projektnummer, abrechnungBeginn, abrechnungEnde);
        final Sort sort = resolveSortWithInputOrDefaults(sortBy, direction);
        log.info("Get Projekte at Page {} with a PageSize of {} matching {} ordered by {}", pageNumber, pageSize,
                String.valueOf(filter).replace('\n', '_').replace('\r', '_'), sort);
        final Pageable pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        return projektRepository
                .findAll(ProjektSpecifications.matching(filter), pageRequest)
                .map(projektEntityMapper::toProjekt);
    }

    @Transactional(readOnly = true)
    public Projekt getProjekt(final UUID projektId) {
        log.info("Get Projekt {}", projektId);
        return projektRepository
                .findById(projektId)
                .map(projektEntityMapper::toProjekt)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, projektId)));
    }

    public boolean existsProjekt(final UUID projektId) {
        return projektRepository.existsById(projektId);
    }

    @Transactional
    public Projekt createProjekt(final Projekt projekt) {
        final ProjektEntity projektEntity = projektEntityMapper.toEntity(projekt);
        log.debug("Create Projekt {}", String.valueOf(projektEntity).replace('\n', '_').replace('\r', '_'));
        return projektEntityMapper.toProjekt(projektRepository.save(projektEntity));
    }

    private Sort resolveSortWithInputOrDefaults(final ProjektSortBy sortBy, final Sort.Direction direction) {
        final ProjektSortBy effectiveSortBy = sortBy == null ? DEFAULT_SORT_BY : sortBy;
        final Sort.Direction effectiveDirection = direction == null ? DEFAULT_DIRECTION : direction;
        return Sort.by(effectiveDirection, effectiveSortBy.getEntityAttribute(), TIEBREAKER_ATTRIBUTE);
    }
}
