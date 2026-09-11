package de.muenchen.oss.sonar.backend.widerspruch;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_WIDERSPRUCH_ALREADY_EXISTS;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungRepository;
import de.muenchen.oss.sonar.backend.common.ConflictException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.widerspruch.domain.Widerspruch;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class WiderspruchService {

    private final WiderspruchRepository widerspruchRepository;
    private final AbrechnungRepository abrechnungRepository;
    private final WiderspruchEntityMapper widerspruchEntityMapper;

    @Transactional(readOnly = true)
    public Optional<Widerspruch> getWiderspruchOfAbrechnung(final UUID abrechnungId) {
        return widerspruchRepository.findByAbrechnungId(abrechnungId).map(widerspruchEntityMapper::toWiderspruch);
    }

    @Transactional(readOnly = true)
    public Map<UUID, Widerspruch> getWiderspruecheOfAbrechnungen(final Collection<UUID> abrechnungIds) {
        if (abrechnungIds.isEmpty()) {
            return Map.of();
        }
        return widerspruchRepository.findByAbrechnungIdIn(abrechnungIds).stream()
                .map(widerspruchEntityMapper::toWiderspruch)
                .collect(Collectors.toMap(Widerspruch::abrechnungId, Function.identity()));
    }

    @Transactional
    public Widerspruch createWiderspruch(final Widerspruch widerspruch) {
        if (!abrechnungRepository.existsById(widerspruch.abrechnungId())) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, widerspruch.abrechnungId()));
        }
        if (getWiderspruchOfAbrechnung(widerspruch.abrechnungId()).isPresent()) {
            throw new ConflictException(String.format(MSG_WIDERSPRUCH_ALREADY_EXISTS, widerspruch.abrechnungId()));
        }
        final WiderspruchEntity widerspruchEntity = widerspruchEntityMapper.toEntity(widerspruch);
        log.debug("Create Widerspruch {}", widerspruchEntity);
        return widerspruchEntityMapper.toWiderspruch(widerspruchRepository.save(widerspruchEntity));
    }
}
