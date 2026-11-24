package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungDTOMapper;
import de.muenchen.oss.sonar.backend.abrechnung.dto.AbrechnungRequestDTO;
import de.muenchen.oss.sonar.backend.berechnung.dto.BerechnungDTOMapper;
import de.muenchen.oss.sonar.backend.berechnung.dto.BerechnungResponseDTO;
import de.muenchen.oss.sonar.backend.configuration.OpenAPIDocumentationConfiguration;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/projekt/{projektId}/abrechnung", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = OpenAPIDocumentationConfiguration.SECURITY_SCHEME_NAME)
public class BerechnungController {

    private final BerechnungService berechnungService;
    private final BerechnungDTOMapper berechnungDTOMapper;
    private final AbrechnungDTOMapper abrechnungDTOMapper;

    /**
     * Calculate an Abrechnung without keeping the result.
     * The Berechnung runs on the stored Nutzungsobjekte and Positionen of the Abrechnung, up to the
     * given berechnungsdatum. A Position reaching beyond that date is charged only up to it, and one
     * beginning after it is left out. The Zeitraum stored on the Abrechnung does not limit the
     * Berechnung, so the berechnungsdatum may fall outside it.
     *
     * @param projektId the UUID of the Projekt the Abrechnung belongs to
     * @param abrechnungId the UUID of the Abrechnung to calculate
     * @param berechnungsdatum the last day the Berechnung charges for
     * @return the result of the Berechnung as a DTO
     */
    @PostMapping("/{abrechnungId}/berechnung/preview")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "400", description = "the berechnungsdatum is missing or is not a date", content = @Content)
    @ApiResponse(responseCode = "404", description = "the Projekt has no Abrechnung with that UUID", content = @Content)
    public BerechnungResponseDTO previewBerechnungOfSavedAbrechnung(@PathVariable("projektId") final UUID projektId,
            @PathVariable("abrechnungId") final UUID abrechnungId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate berechnungsdatum) {
        return berechnungDTOMapper.toDTO(berechnungService.previewBerechnungOfSavedAbrechnung(projektId, abrechnungId, berechnungsdatum));
    }

    /**
     * Calculate an unsaved Abrechnung without keeping the result.
     * The body is the same as the one that creates an Abrechnung or its next version, so the result
     * can be checked before saving. Nothing is stored.
     * The Berechnung runs on the Nutzungsobjekte, Positionen and Masten of the request, up to the
     * given berechnungsdatum. A Position reaching beyond that date is charged only up to it, and one
     * beginning after it is left out. The Zeitraum of the request does not limit the Berechnung, so
     * the berechnungsdatum may fall outside it.
     * A Nutzungsobjekt sent with the UUID of a stored one keeps its stored details, as it does when
     * saving. Only its Positionen and Masten come from the request.
     *
     * @param projektId the UUID of the Projekt the Abrechnung belongs to
     * @param berechnungsdatum the last day the Berechnung charges for
     * @param abrechnungRequestDTO the details of the Abrechnung to calculate
     * @return the result of the Berechnung as a DTO
     */
    @PostMapping("/berechnung/preview")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(
            responseCode = "400", description = "the details of the Abrechnung are invalid, or the berechnungsdatum is missing or is not a date",
            content = @Content
    )
    @ApiResponse(responseCode = "403", description = "the Projekt lies outside the Stadtbezirke of the user", content = @Content)
    @ApiResponse(responseCode = "404", description = "the Projekt does not exist, or it has no Nutzungsobjekt with one of the given UUIDs", content = @Content)
    public BerechnungResponseDTO previewBerechnungOfUnsavedAbrechnung(@PathVariable("projektId") final UUID projektId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate berechnungsdatum,
            @Valid @RequestBody final AbrechnungRequestDTO abrechnungRequestDTO) {
        return berechnungDTOMapper.toDTO(berechnungService.previewBerechnungOfUnsavedAbrechnung(
                abrechnungDTOMapper.toAbrechnung(projektId, abrechnungRequestDTO), berechnungsdatum));
    }

}
