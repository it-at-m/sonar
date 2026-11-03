package de.muenchen.oss.sonar.backend.berechnung;

import de.muenchen.oss.sonar.backend.berechnung.dto.BerechnungDTOMapper;
import de.muenchen.oss.sonar.backend.berechnung.dto.BerechnungResponseDTO;
import de.muenchen.oss.sonar.backend.configuration.OpenAPIDocumentationConfiguration;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/projekt/{projektId}/abrechnung/{abrechnungId}/berechnung", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = OpenAPIDocumentationConfiguration.SECURITY_SCHEME_NAME)
public class BerechnungController {

    private final BerechnungService berechnungService;
    private final BerechnungDTOMapper berechnungDTOMapper;

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
    @PostMapping("/preview")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "400", description = "the berechnungsdatum is missing or is not a date", content = @Content)
    @ApiResponse(responseCode = "404", description = "the Projekt has no Abrechnung with that UUID", content = @Content)
    public BerechnungResponseDTO previewBerechnung(@PathVariable("projektId") final UUID projektId,
            @PathVariable("abrechnungId") final UUID abrechnungId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate berechnungsdatum) {
        return berechnungDTOMapper.toDTO(berechnungService.previewBerechnung(projektId, abrechnungId, berechnungsdatum));
    }

}
