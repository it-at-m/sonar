package de.muenchen.oss.sonar.backend.projekt;

import de.muenchen.oss.sonar.backend.configuration.OpenAPIDocumentationConfiguration;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektDTOMapper;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektRequestDTO;
import de.muenchen.oss.sonar.backend.projekt.dto.ProjektResponseDTO;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
     * Retrieve Projekte with pagination.
     * Fetches a paginated list of Projekte based on the provided page number and size.
     * The optional search criteria are combined with AND and apply to the whole result, not just to
     * the requested page. The same holds for the order, which is applied by the database.
     *
     * @param pageNumber the number of the requested page (default: 0)
     * @param pageSize the size of the page to retrieve (default: 10, at most 100)
     * @param projektnummer part of the Projektnummer to search for, case-insensitive
     * @param abrechnungBeginn the exact beginn of the billing period to search for
     * @param abrechnungEnde the exact end of the billing period to search for
     * @param sortBy the column to order by (default: PROJEKTNUMMER)
     * @param sortDirection the direction to order in (default: DESC)
     * @return a page of Projekte represented as DTOs
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "400", description = "the pagination, search or sort parameters are invalid", content = @Content)
    public Page<ProjektResponseDTO> getProjekteByPageAndSize(@RequestParam(defaultValue = "0") @Min(0) final int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) final int pageSize,
            @RequestParam(required = false) @Size(max = 20) final String projektnummer,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate abrechnungBeginn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate abrechnungEnde,
            @RequestParam(required = false) final ProjektSortBy sortBy,
            @RequestParam(required = false) final Sort.Direction sortDirection) {
        final ProjektFilter filter = new ProjektFilter(projektnummer, abrechnungBeginn, abrechnungEnde);
        return projektService.getAllProjekte(pageNumber, pageSize, filter, sortBy, sortDirection).map(projektDTOMapper::toDTO);
    }

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
