package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungDTOMapper;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungRequestDTO;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungResponseDTO;
import de.muenchen.oss.sonar.backend.configuration.OpenAPIDocumentationConfiguration;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/projekt/{projektId}/abrechnung", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = OpenAPIDocumentationConfiguration.SECURITY_SCHEME_NAME)
public class AbrechnungController {

    private final AbrechnungService abrechnungService;
    private final AbrechnungDTOMapper abrechnungDTOMapper;

    /**
     * Create a new Abrechnung for a Projekt.
     * Creates the Abrechnung in one call. The end of a Zeitraum must not be before its beginn.
     * An Adresse with a Hausnummer or a Flurstück with a Gemarkung is allowed, never both.
     *
     * @param projektId the UUID of the Projekt the Abrechnung belongs to
     * @param abrechnungRequestDTO the details of the Abrechnung to create
     * @return the created Abrechnung as a DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "the details of the Abrechnung are invalid", content = @Content)
    @ApiResponse(responseCode = "404", description = "the Projekt does not exist", content = @Content)
    public AbrechnungResponseDTO saveAbrechnung(@PathVariable("projektId") final UUID projektId,
            @Valid @RequestBody final AbrechnungRequestDTO abrechnungRequestDTO) {
        return abrechnungDTOMapper.toDTO(
                abrechnungService.createAbrechnung(abrechnungDTOMapper.toAbrechnung(projektId, abrechnungRequestDTO)));
    }

}
