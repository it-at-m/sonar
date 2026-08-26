package de.muenchen.oss.sonar.backend.projekt;

import de.muenchen.oss.sonar.backend.configuration.OpenAPIDocumentationConfiguration;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektDTOMapper;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektRequestDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektResponseDTO;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/projekt", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = OpenAPIDocumentationConfiguration.SECURITY_SCHEME_NAME)
public class ProjektController {

    private final ProjektService projektService;
    private final ProjektDTOMapper projektDTOMapper;

    /**
     * Create a new Projekt.
     * Creates a new Projekt together with its Adressen in one call.
     * Abrechnungsende must not be before Abrechnungsbeginn.
     * Per Adresse, the period of unerlaubte Nutzung needs both dates or neither.
     * Either that period or tageUnerlaubteNutzung is given, never both.
     *
     * @param projektRequestDTO the details of the Projekt to create
     * @return the created Projekt as a DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "the details of the Projekt are invalid", content = @Content)
    public ProjektResponseDTO saveProjekt(@Valid @RequestBody final ProjektRequestDTO projektRequestDTO) {
        return projektDTOMapper.toDTO(projektService.createProjekt(projektDTOMapper.toProjekt(projektRequestDTO)));
    }

}
