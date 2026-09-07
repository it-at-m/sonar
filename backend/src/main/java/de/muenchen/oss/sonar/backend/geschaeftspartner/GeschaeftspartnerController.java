package de.muenchen.oss.sonar.backend.geschaeftspartner;

import de.muenchen.oss.sonar.backend.configuration.OpenAPIDocumentationConfiguration;
import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerResponseDTO;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/geschaeftspartner", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = OpenAPIDocumentationConfiguration.SECURITY_SCHEME_NAME)
public class GeschaeftspartnerController {

    private final GeschaeftspartnerService geschaeftspartnerService;

    /**
     * Retrieve the master data of a Geschäftspartner.
     * Retrieved data come from a third party system owning them.
     *
     * @param geschaeftspartnerId the id of the requested Geschäftspartner
     * @return the master data of the Geschäftspartner as a DTO
     */
    @GetMapping("{geschaeftspartnerId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "400", description = "the id is longer than the owning system allows", content = @Content)
    @ApiResponse(responseCode = "404", description = "the owning system does not know the id", content = @Content)
    @ApiResponse(responseCode = "502", description = "the owning system could not be reached", content = @Content)
    public GeschaeftspartnerResponseDTO getGeschaeftspartner(
            @PathVariable("geschaeftspartnerId") @Size(max = 10) final String geschaeftspartnerId) {
        return geschaeftspartnerService.getGeschaeftspartner(geschaeftspartnerId);
    }

}
