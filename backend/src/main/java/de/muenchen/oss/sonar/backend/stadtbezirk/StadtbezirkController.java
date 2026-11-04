package de.muenchen.oss.sonar.backend.stadtbezirk;

import de.muenchen.oss.sonar.backend.configuration.OpenAPIDocumentationConfiguration;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/stadtbezirk", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = OpenAPIDocumentationConfiguration.SECURITY_SCHEME_NAME)
public class StadtbezirkController {

    private final StadtbezirkService stadtbezirkService;

    /**
     * Retrieve the Stadtbezirke of the user.
     * These are the Stadtbezirke assigned to one of the roles of the user. The user may access a
     * Projekt only in one of them, and may create a Projekt only in one of them.
     * The order is by the number of the Stadtbezirk.
     *
     * @return the Stadtbezirke of the user, empty if none is assigned to any of the roles
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Stadtbezirk> getStadtbezirkeOfCurrentUser() {
        return stadtbezirkService.getStadtbezirkeOfCurrentUser().stream()
                .sorted()
                .toList();
    }

}
