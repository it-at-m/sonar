package de.muenchen.oss.sonar.backend.widerspruch;

import de.muenchen.oss.sonar.backend.configuration.OpenAPIDocumentationConfiguration;
import de.muenchen.oss.sonar.backend.widerspruch.dto.WiderspruchDTOMapper;
import de.muenchen.oss.sonar.backend.widerspruch.dto.WiderspruchRequestDTO;
import de.muenchen.oss.sonar.backend.widerspruch.dto.WiderspruchResponseDTO;
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
@RequestMapping(value = "/abrechnung/{abrechnungId}/widerspruch", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = OpenAPIDocumentationConfiguration.SECURITY_SCHEME_NAME)
public class WiderspruchController {

    private final WiderspruchService widerspruchService;
    private final WiderspruchDTOMapper widerspruchDTOMapper;

    /**
     * Create the Widerspruch of an Abrechnung.
     * An Abrechnung carries at most one Widerspruch, so a second one is refused.
     *
     * @param abrechnungId the UUID of the Abrechnung the Widerspruch belongs to
     * @param widerspruchRequestDTO the details of the Widerspruch to create
     * @return the created Widerspruch as a DTO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "400", description = "the details of the Widerspruch are invalid", content = @Content)
    @ApiResponse(responseCode = "404", description = "the Abrechnung does not exist", content = @Content)
    @ApiResponse(responseCode = "409", description = "the Abrechnung already has a Widerspruch", content = @Content)
    public WiderspruchResponseDTO saveWiderspruch(@PathVariable("abrechnungId") final UUID abrechnungId,
            @Valid @RequestBody final WiderspruchRequestDTO widerspruchRequestDTO) {
        return widerspruchDTOMapper.toDTO(
                widerspruchService.createWiderspruch(widerspruchDTOMapper.toWiderspruch(abrechnungId, widerspruchRequestDTO)));
    }

}
