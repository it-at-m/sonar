package de.muenchen.oss.sonar.backend.abrechnung;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;

import de.muenchen.oss.sonar.backend.abrechnung.domain.Abrechnung;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.projekt.ProjektService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AbrechnungService {

    private final AbrechnungRepository abrechnungRepository;
    private final ProjektService projektService;
    private final AbrechnungEntityMapper abrechnungEntityMapper;

    @Transactional
    public Abrechnung createAbrechnung(final Abrechnung abrechnung) {
        if (!projektService.existsProjekt(abrechnung.projektId())) {
            throw new NotFoundException(String.format(MSG_NOT_FOUND, abrechnung.projektId()));
        }
        final AbrechnungEntity abrechnungEntity = abrechnungEntityMapper.toEntity(abrechnung);
        log.debug("Create Abrechnung {}", String.valueOf(abrechnungEntity).replace('\n', '_').replace('\r', '_'));
        return abrechnungEntityMapper.toAbrechnung(abrechnungRepository.save(abrechnungEntity));
    }
}
