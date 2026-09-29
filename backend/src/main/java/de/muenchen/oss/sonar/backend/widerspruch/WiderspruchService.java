package de.muenchen.oss.sonar.backend.widerspruch;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_WIDERSPRUCH_ALREADY_EXISTS;

import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungEntity;
import de.muenchen.oss.sonar.backend.abrechnung.AbrechnungRepository;
import de.muenchen.oss.sonar.backend.common.ConflictException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.widerspruch.domain.Widerspruch;
import java.util.UUID;
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

    @Transactional
    public Widerspruch createWiderspruch(final UUID abrechnungId, final Widerspruch widerspruch) {
        final AbrechnungEntity abrechnung = abrechnungRepository.findById(abrechnungId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, abrechnungId)));
        if (abrechnung.getWiderspruch() != null) {
            throw new ConflictException(String.format(MSG_WIDERSPRUCH_ALREADY_EXISTS, abrechnungId));
        }
        final WiderspruchEntity widerspruchEntity = widerspruchEntityMapper.toEntity(widerspruch);
        log.debug("Create Widerspruch {}", widerspruchEntity);
        final WiderspruchEntity savedWiderspruch = widerspruchRepository.save(widerspruchEntity);
        abrechnung.setWiderspruch(savedWiderspruch);
        abrechnungRepository.save(abrechnung);
        return widerspruchEntityMapper.toWiderspruch(savedWiderspruch);
    }
}
