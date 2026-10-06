package de.muenchen.oss.sonar.backend.calculation;

import de.muenchen.oss.sonar.backend.calculation.dto.CalculationDTOMapper;
import de.muenchen.oss.sonar.backend.calculation.dto.CalculationResponseDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/projekt/{projektId}/abrechnung/{abrechnungId}", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = OpenAPIDocumentationConfiguration.SECURITY_SCHEME_NAME)
public class CalculationController {

    private final CalculationService calculationService;
    private final CalculationDTOMapper calculationDTOMapper;

    /**
     * Read the stored calculation of an Abrechnung.
     * An Abrechnung is calculated once, and the answer carries everything that calculation held when
     * it was stored: its Zusammenfassung, its Berechnungslog and its Bescheiddaten. Nothing is
     * calculated again, so an Abrechnung that was edited after its calculation was stored does not
     * change the answer.
     *
     * @param projektId the UUID of the Projekt the Abrechnung belongs to
     * @param abrechnungId the UUID of the Abrechnung the calculation belongs to
     * @return the stored calculation as a DTO
     */
    @GetMapping("/calculation")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "404", description = "the Projekt has no Abrechnung with that UUID, or the Abrechnung was never calculated", content = @Content)
    public CalculationResponseDTO getCalculation(@PathVariable("projektId") final UUID projektId,
            @PathVariable("abrechnungId") final UUID abrechnungId) {
        return calculationDTOMapper.toDTO(calculationService.getCalculation(projektId, abrechnungId));
    }

    /**
     * Calculate an Abrechnung.
     * The calculation runs on the stored Nutzungsobjekte and Positionen of the Abrechnung, up to the
     * given calculationDate. A Position reaching beyond that date is charged only up to it, and one
     * beginning after it is left out. The Zeitraum stored on the Abrechnung does not limit the
     * calculation, so the calculationDate may fall outside it.
     *
     * @param projektId the UUID of the Projekt the Abrechnung belongs to
     * @param abrechnungId the UUID of the Abrechnung to calculate
     * @param calculationDate the last day the calculation charges for
     * @return the result of the calculation as a DTO
     */
    @PostMapping("/calculation/preview")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "400", description = "the calculationDate is missing or is not a date", content = @Content)
    @ApiResponse(responseCode = "404", description = "the Projekt has no Abrechnung with that UUID", content = @Content)
    public CalculationResponseDTO previewCalculation(@PathVariable("projektId") final UUID projektId,
            @PathVariable("abrechnungId") final UUID abrechnungId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate calculationDate) {
        return calculationDTOMapper.toDTO(calculationService.previewCalculation(projektId, abrechnungId, calculationDate));
    }

    /**
     * Store the result of a calculation.
     * Runs the same calculation as the preview and keeps its result. An Abrechnung carries at most one
     * calculation, so a second one is refused. A further Abrechnung of the Projekt then charges only
     * what this one left over: its Zahlbetrag is the difference, while its Gesamtbetrag still covers
     * the whole Zeitraum.
     *
     * @param projektId the UUID of the Projekt the Abrechnung belongs to
     * @param abrechnungId the UUID of the Abrechnung to calculate and store
     * @param calculationDate the last day the calculation charges for
     * @return the stored result of the calculation as a DTO
     */
    @PostMapping("/calculation")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "the calculationDate is missing or is not a date", content = @Content)
    @ApiResponse(responseCode = "404", description = "the Projekt has no Abrechnung with that UUID", content = @Content)
    @ApiResponse(responseCode = "409", description = "the Abrechnung already has a calculation", content = @Content)
    public CalculationResponseDTO storeCalculation(@PathVariable("projektId") final UUID projektId,
            @PathVariable("abrechnungId") final UUID abrechnungId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate calculationDate) {
        return calculationDTOMapper.toDTO(calculationService.calculateAndStore(projektId, abrechnungId, calculationDate));
    }

}
