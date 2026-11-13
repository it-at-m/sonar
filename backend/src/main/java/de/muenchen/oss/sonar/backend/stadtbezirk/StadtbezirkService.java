package de.muenchen.oss.sonar.backend.stadtbezirk;

import de.muenchen.oss.sonar.backend.security.AuthUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StadtbezirkService {

    private final StadtbezirkProperties stadtbezirkProperties;

    public Set<Stadtbezirk> getStadtbezirkeOfCurrentUser() {
        final Set<String> rollenOfCurrentUser = AuthUtils.getRoles();
        return stadtbezirkProperties.rollen().entrySet().stream()
                .filter(rollenOfStadtbezirk -> !Collections.disjoint(rollenOfStadtbezirk.getValue(), rollenOfCurrentUser))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

}
