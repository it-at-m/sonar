package de.muenchen.oss.sonar.backend.stadtbezirk;

import java.util.Map;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param rollen the Keycloak roles whose users see the Projekte in the Stadtbezirk
 */
@ConfigurationProperties(prefix = "sonar.stadtbezirk")
public record StadtbezirkProperties(Map<Stadtbezirk, Set<String>> rollen) {

    public StadtbezirkProperties {
        rollen = Map.copyOf(rollen);
    }

}
