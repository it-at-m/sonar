package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.berechnung.dto.BerechnungRequestDTO;
import de.muenchen.oss.sonar.backend.configuration.OpenAPIDocumentationConfiguration;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
     * Takes the Projektnummer and the Nutzungsobjekte the Berechnung runs on. Per Nutzungsobjekt it
     * takes the Positionen with their Zeitraum and their Fläche.
     * The end of a Zeitraum must not be before its beginn.
     *
     * @param projektId the UUID of the Projekt the Abrechnung belongs to
     * @param abrechnungId the UUID of the Abrechnung to run the Berechnung of
     * @param berechnungRequestDTO the data the Berechnung runs on
     */
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "400", description = "the data of the Berechnung is invalid", content = @Content)
    public void berechnungDurchfuehren(@PathVariable("projektId") final UUID projektId,
            @PathVariable("abrechnungId") final UUID abrechnungId,
            @Valid @RequestBody final BerechnungRequestDTO berechnungRequestDTO) {
        berechnungService.berechnungDurchfuehren(projektId, abrechnungId, berechnungRequestDTO);
    }

}
