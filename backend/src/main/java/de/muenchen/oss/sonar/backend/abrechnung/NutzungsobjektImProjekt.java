package de.muenchen.oss.sonar.backend.abrechnung;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.umd.cs.findbugs.annotations.SuppressMatchType;
import java.util.UUID;

@SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "Spring Data fills the record with a managed entity from a query. The caller needs that same entity, not a copy.",
        matchType = SuppressMatchType.EXACT
)
public record NutzungsobjektImProjekt(UUID projektId, NutzungsobjektEntity nutzungsobjekt) {
}
