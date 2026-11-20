package de.muenchen.oss.sonar.backend.abrechnung;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NEWER_VERSION_ALREADY_EXISTS;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_VORGAENGER_WITHOUT_ABRECHNUNG;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_ZEITRAUM_VON_NOT_AFTER_VORGAENGER;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungMast;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungNutzungsobjekt;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungPosition;
import de.muenchen.oss.sonar.backend.abrechnung.domain.AbrechnungVersion;
import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnungsanschluss;
import de.muenchen.oss.sonar.backend.common.BadRequestException;
import de.muenchen.oss.sonar.backend.common.ConflictException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
@SuppressWarnings("PMD.CouplingBetweenObjects")
@RequiredArgsConstructor
public class AbrechnungService {

    private static final AbrechnungSortBy DEFAULT_SORT_BY = AbrechnungSortBy.ZEITRAUM_VON;

    private static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.DESC;

    private static final String TIEBREAKER_ATTRIBUTE = "id";

    private static final int FIRST_VERSION_NUMBER = 1;

    private final AbrechnungRepository abrechnungRepository;
    private final ProjektService projektService;
    private final AbrechnungEntityMapper abrechnungEntityMapper;
    private final NutzungsobjektService nutzungsobjektService;

    @Transactional(readOnly = true)
    public Page<Abrechnung> getAbrechnungenOfProjekt(final UUID projektId, final int pageNumber, final int pageSize,
            final List<AbrechnungSortBy> sortBy, final List<Sort.Direction> directions) {
        if (!projektService.existsProjekt(projektId)) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, projektId));
        }
        final Sort sort = resolveSortWithInputOrDefaults(sortBy, directions);
        log.info("Get Abrechnungen of Projekt {} at Page {} with a PageSize of {} ordered by {}", projektId, pageNumber, pageSize, sort);
        final Pageable pageRequest = PageRequest.of(pageNumber, pageSize, sort);
        // The page holds the newest version of each Abrechnung, so none of them has a newer one.
        return abrechnungRepository.findNewestVersionsByProjektId(projektId, pageRequest)
                .map(abrechnungEntity -> toAbrechnung(abrechnungEntity, false));
    }

    @Transactional(readOnly = true)
    public List<Abrechnung> getNewestAbrechnungenOfProjekt(final UUID projektId) {
        log.info("Get the newest Abrechnungen of Projekt {}", projektId);
        final Sort sort = Sort.by(Sort.Direction.ASC, AbrechnungSortBy.ZEITRAUM_VON.getEntityAttribute(), TIEBREAKER_ATTRIBUTE);
        return abrechnungRepository.findNewestVersionsByProjektId(projektId, Pageable.unpaged(sort))
                .map(abrechnungEntity -> toAbrechnung(abrechnungEntity, false))
                .getContent();
    }

    @Transactional(readOnly = true)
    public Abrechnung getAbrechnung(final UUID projektId, final UUID abrechnungId) {
        log.info("Get Abrechnung {} of Projekt {}", abrechnungId, projektId);
        final AbrechnungEntity abrechnungEntity = abrechnungRepository.findByIdAndProjektId(abrechnungId, projektId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, abrechnungId)));
        return toAbrechnung(abrechnungEntity, abrechnungRepository.existsByVorgaengerAbrechnungId(abrechnungId));
    }

    @Transactional(readOnly = true)
    public List<AbrechnungVersion> getVersionenOfAbrechnung(final UUID projektId, final UUID abrechnungId) {
        log.info("Get the versions of Abrechnung {} of Projekt {}", abrechnungId, projektId);
        final List<AbrechnungVersion> versionenOfProjekt = abrechnungRepository.findByProjektId(projektId);
        final AbrechnungVersion angefragteVersion = versionenOfProjekt.stream()
                .filter(version -> version.id().equals(abrechnungId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, abrechnungId)));

        final Deque<AbrechnungVersion> kette = new ArrayDeque<>();
        kette.add(angefragteVersion);

        AbrechnungVersion aeltere = vorgaengerOf(angefragteVersion, versionenOfProjekt);
        while (aeltere != null) {
            kette.addLast(aeltere);
            aeltere = vorgaengerOf(aeltere, versionenOfProjekt);
        }

        AbrechnungVersion neuere = nachfolgerOf(angefragteVersion, versionenOfProjekt);
        while (neuere != null) {
            kette.addFirst(neuere);
            neuere = nachfolgerOf(neuere, versionenOfProjekt);
        }

        return List.copyOf(kette);
    }

    private AbrechnungVersion vorgaengerOf(final AbrechnungVersion version, final List<AbrechnungVersion> versionen) {
        return versionen.stream()
                .filter(kandidat -> kandidat.id().equals(version.vorgaengerAbrechnungId()))
                .findFirst()
                .orElse(null);
    }

    private AbrechnungVersion nachfolgerOf(final AbrechnungVersion version, final List<AbrechnungVersion> versionen) {
        return versionen.stream()
                .filter(kandidat -> version.id().equals(kandidat.vorgaengerAbrechnungId()))
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Abrechnungsanschluss getAbrechnungsanschluss(final UUID projektId) {
        if (!projektService.existsProjekt(projektId)) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, projektId));
        }
        log.info("Get the Abrechnungsanschluss of Projekt {}", projektId);
        return abrechnungsanschlussOf(projektId);
    }

    private Abrechnungsanschluss abrechnungsanschlussOf(final UUID projektId) {
        final UUID vorgaengerProjektId = projektService.getVorgaengerProjektId(projektId);
        // Only the first Abrechnung of a Projekt continues where its Vorgaenger stops. Later ones and new versions start on any day.
        if (vorgaengerProjektId == null || abrechnungRepository.existsByProjektId(projektId)) {
            return Abrechnungsanschluss.FREI;
        }
        final Pageable latestZeitraumBis = PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, AbrechnungSortBy.ZEITRAUM_BIS.getEntityAttribute()));
        return abrechnungRepository.findNewestVersionsByProjektId(vorgaengerProjektId, latestZeitraumBis).stream()
                .findFirst()
                .map(abrechnung -> Abrechnungsanschluss.beginntAm(abrechnung.getZeitraumBis().plusDays(1)))
                .orElse(Abrechnungsanschluss.NICHT_MOEGLICH);
    }

    @Transactional
    public Abrechnung createAbrechnung(final Abrechnung abrechnung) {
        if (!projektService.existsProjekt(abrechnung.projektId())) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, abrechnung.projektId()));
        }
        checkAbrechnungsanschluss(abrechnung);
        final AbrechnungEntity abrechnungEntity = toAbrechnungEntity(abrechnung);
        abrechnungEntity.setVersionsnummer(FIRST_VERSION_NUMBER);
        log.debug("Create Abrechnung {}", abrechnungEntity);
        return toAbrechnung(abrechnungRepository.save(abrechnungEntity), false);
    }

    private void checkAbrechnungsanschluss(final Abrechnung abrechnung) {
        final Abrechnungsanschluss abrechnungsanschluss = abrechnungsanschlussOf(abrechnung.projektId());
        if (!abrechnungsanschluss.anlegenMoeglich()) {
            throw new ConflictException(String.format(MSG_VORGAENGER_WITHOUT_ABRECHNUNG, abrechnung.projektId()));
        }
        if (abrechnungsanschluss.zeitraumVon() != null && !abrechnungsanschluss.zeitraumVon().equals(abrechnung.zeitraumVon())) {
            throw new BadRequestException(
                    String.format(MSG_ZEITRAUM_VON_NOT_AFTER_VORGAENGER, abrechnung.projektId(), abrechnungsanschluss.zeitraumVon()));
        }
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
        final AbrechnungEntity nextVersion = toAbrechnungEntity(abrechnung);
        nextVersion.setVersionsnummer(vorgaenger.getVersionsnummer() + 1);
        nextVersion.setVorgaengerAbrechnungId(vorgaengerAbrechnungId);
        log.debug("Create next version of Abrechnung {}: {}", vorgaengerAbrechnungId, nextVersion);
        return toAbrechnung(abrechnungRepository.save(nextVersion), false);
    }

    private AbrechnungEntity toAbrechnungEntity(final Abrechnung abrechnung) {
        final Map<UUID, NutzungsobjektEntity> selectedPreexistingNutzungsobjekte = nutzungsobjektService
                .selectedPreexistingNutzungsobjekte(abrechnung);
        final AbrechnungEntity abrechnungEntity = abrechnungEntityMapper.toEntity(abrechnung);
        for (final AbrechnungNutzungsobjekt nutzungsobjekt : abrechnung.nutzungsobjekte()) {
            final NutzungsobjektEntity nutzungsobjektEntity = nutzungsobjekt.id() == null
                    ? abrechnungEntityMapper.toEntity(nutzungsobjekt)
                    : selectedPreexistingNutzungsobjekte.get(nutzungsobjekt.id());
            abrechnungEntity.addNutzungsobjekt(nutzungsobjektEntity);
            for (final AbrechnungPosition position : nutzungsobjekt.positionen()) {
                final AbrechnungPositionEntity positionEntity = abrechnungEntityMapper.toEntity(position);
                positionEntity.setNutzungsobjekt(nutzungsobjektEntity);
                abrechnungEntity.addPosition(positionEntity);
            }
            for (final AbrechnungMast mast : nutzungsobjekt.masten()) {
                final AbrechnungMastEntity mastEntity = abrechnungEntityMapper.toEntity(mast);
                mastEntity.setNutzungsobjekt(nutzungsobjektEntity);
                abrechnungEntity.addMast(mastEntity);
            }
        }
        return abrechnungEntity;
    }

    private Abrechnung toAbrechnung(final AbrechnungEntity abrechnungEntity, final boolean neuereVersionVorhanden) {
        return abrechnungEntityMapper.toAbrechnung(abrechnungEntity, neuereVersionVorhanden, toNutzungsobjekte(abrechnungEntity));
    }

    /**
     * Filters the Positionen and the Masten of this Abrechnung below the Nutzungsobjekt they name.
     * Several Abrechnungen bill the same Nutzungsobjekt, and each of them shows only its own.
     */
    private List<AbrechnungNutzungsobjekt> toNutzungsobjekte(final AbrechnungEntity abrechnungEntity) {
        final Map<UUID, List<AbrechnungPosition>> positionenJeNutzungsobjekt = abrechnungEntity.getPositionen().stream()
                .collect(Collectors.groupingBy(position -> position.getNutzungsobjekt().getId(),
                        Collectors.mapping(abrechnungEntityMapper::toPosition, Collectors.toList())));
        final Map<UUID, List<AbrechnungMast>> mastenJeNutzungsobjekt = abrechnungEntity.getMasten().stream()
                .collect(Collectors.groupingBy(mast -> mast.getNutzungsobjekt().getId(),
                        Collectors.mapping(abrechnungEntityMapper::toMast, Collectors.toList())));
        return abrechnungEntity.getNutzungsobjekte().stream()
                .map(nutzungsobjekt -> abrechnungEntityMapper.toNutzungsobjekt(nutzungsobjekt,
                        positionenJeNutzungsobjekt.getOrDefault(nutzungsobjekt.getId(), List.of()),
                        mastenJeNutzungsobjekt.getOrDefault(nutzungsobjekt.getId(), List.of())))
                .toList();
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
