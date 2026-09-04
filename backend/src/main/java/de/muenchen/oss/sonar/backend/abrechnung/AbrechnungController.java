package de.muenchen.oss.sonar.backend.abrechnung;

import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungDTOMapper;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungRequestDTO;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungResponseDTO;
import de.muenchen.oss.sonar.backend.configuration.OpenAPIDocumentationConfiguration;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
     * Retrieve the Abrechnungen of a Projekt with pagination.
     * Fetches a paginated list of all Abrechnungen belonging to the Projekt.
     *
     * @param projektId the UUID of the Projekt the Abrechnungen belong to
     * @param pageNumber the number of the requested page (default: 0)
     * @param pageSize the size of the page to retrieve (default: 10, at most 100)
     * @param sortBy the column to order by (default: ZEITRAUM_VON)
     * @param sortDirection the direction to order in (default: DESC)
     * @return a page of Abrechnungen represented as DTOs
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "400", description = "the pagination or sort parameters are invalid", content = @Content)
    @ApiResponse(responseCode = "404", description = "the Projekt does not exist", content = @Content)
    public Page<AbrechnungResponseDTO> getAbrechnungenByPageAndSize(@PathVariable("projektId") final UUID projektId,
            @RequestParam(defaultValue = "0") @Min(0) final int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) final int pageSize,
            @RequestParam(defaultValue = "ZEITRAUM_VON") final AbrechnungSortBy sortBy,
            @RequestParam(defaultValue = "DESC") final Sort.Direction sortDirection) {
        return abrechnungService.getAbrechnungenOfProjekt(projektId, pageNumber, pageSize, sortBy, sortDirection)
                .map(abrechnungDTOMapper::toDTO);
    }

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
