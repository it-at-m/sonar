package de.muenchen.oss.sonar.backend.projekt;

import de.muenchen.oss.sonar.backend.projekt.model.CreateProjektCommand;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektModelMapper;
import de.muenchen.oss.sonar.backend.projekt.model.ProjektView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjektService {

    private final ProjektRepository projektRepository;
    private final ProjektModelMapper projektModelMapper;

    @Transactional
    public ProjektView createProjekt(final CreateProjektCommand createProjektCommand) {
        final Projekt projekt = projektModelMapper.toEntity(createProjektCommand);
        log.debug("Create Projekt {}", projekt);
        return projektModelMapper.toView(projektRepository.save(projekt));
    }
}
