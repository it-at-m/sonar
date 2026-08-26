package de.muenchen.oss.sonar.backend.projekt;

import de.muenchen.oss.sonar.backend.projekt.domain.Projekt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjektService {

    private final ProjektRepository projektRepository;
    private final ProjektEntityMapper projektEntityMapper;

    @Transactional
    public Projekt createProjekt(final Projekt projekt) {
        final ProjektEntity projektEntity = projektEntityMapper.toEntity(projekt);
        log.debug("Create Projekt {}", projektEntity);
        return projektEntityMapper.toProjekt(projektRepository.save(projektEntity));
    }
}
