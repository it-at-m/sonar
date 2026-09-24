package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.configuration.OpenAPIDocumentationConfiguration;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/projekt/{projektId}/abrechnung/{abrechnungId}/berechnung")
@SecurityRequirement(name = OpenAPIDocumentationConfiguration.SECURITY_SCHEME_NAME)
public class BerechnungController {

    private final BerechnungService berechnungService;

    /**
     * Run the Berechnung of an Abrechnung.
     * The Berechnung runs on the stored Nutzungsobjekte and Positionen of the Abrechnung, so the call
     * needs nothing beyond the two ids.
     * The result is not persisted yet and only reaches the application log.
     *
     * @param projektId the UUID of the Projekt the Abrechnung belongs to
     * @param abrechnungId the UUID of the Abrechnung to run the Berechnung of
     */
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "404", description = "the Projekt has no Abrechnung with that UUID", content = @Content)
    public void berechnungDurchfuehren(@PathVariable("projektId") final UUID projektId,
            @PathVariable("abrechnungId") final UUID abrechnungId) {
        berechnungService.berechnungDurchfuehren(projektId, abrechnungId);
    }

}
