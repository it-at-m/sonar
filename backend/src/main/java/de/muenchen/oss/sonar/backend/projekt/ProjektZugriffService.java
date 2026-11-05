package de.muenchen.oss.sonar.backend.projekt;

import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_NOT_FOUND;
import static de.muenchen.oss.sonar.backend.common.ExceptionMessageConstants.MSG_STADTBEZIRK_FORBIDDEN;

import de.muenchen.oss.sonar.backend.common.ForbiddenException;
import de.muenchen.oss.sonar.backend.common.NotFoundException;
import de.muenchen.oss.sonar.backend.stadtbezirk.Stadtbezirk;
import de.muenchen.oss.sonar.backend.stadtbezirk.StadtbezirkService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjektZugriffService {

    private final ProjektRepository projektRepository;
    private final StadtbezirkService stadtbezirkService;

    @Transactional(readOnly = true)
    public void checkZugriffOnProjekt(final UUID projektId) {
        final ProjektStadtbezirk projekt = projektRepository.findStadtbezirkById(projektId)
                .orElseThrow(() -> new NotFoundException(String.format(MSG_NOT_FOUND, projektId)));
        checkZugriffOnStadtbezirk(projekt.stadtbezirk());
    }

    public void checkZugriffOnStadtbezirk(final Stadtbezirk stadtbezirk) {
        if (!stadtbezirkService.getStadtbezirkeOfCurrentUser().contains(stadtbezirk)) {
            log.warn("Refuse access to Stadtbezirk {}", stadtbezirk);
            throw new ForbiddenException(String.format(MSG_STADTBEZIRK_FORBIDDEN, stadtbezirk));
        }
    }
}
