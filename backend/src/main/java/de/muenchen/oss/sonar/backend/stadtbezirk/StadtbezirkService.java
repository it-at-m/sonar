package de.muenchen.oss.sonar.backend.stadtbezirk;

import de.muenchen.oss.sonar.backend.security.AuthUtils;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StadtbezirkService {

    private final StadtbezirkRolleRepository stadtbezirkRolleRepository;

    public Set<Stadtbezirk> getStadtbezirkeOfCurrentUser() {
        return stadtbezirkRolleRepository.findByRolleIn(AuthUtils.getRoles()).stream()
                .map(StadtbezirkRolleEntity::getStadtbezirk)
                .collect(Collectors.toSet());
    }

}
