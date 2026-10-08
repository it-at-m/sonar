package de.muenchen.oss.sonar.backend.geschaeftspartner;

import de.muenchen.oss.sonar.backend.geschaeftspartner.client.MockGeschaeftspartnerClient;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "sonar.geschaeftspartner.client")
@Validated
@Data
public class GeschaeftspartnerProperties {

    /**
     * SOAP endpoint the client calls.
     * <p>
     * This class is bound in every profile, but the profiles "local" and "test" run against
     * {@link MockGeschaeftspartnerClient} and do not use the configured endpoint.
     * </p>
     */
    @NotBlank private String url;

    /** Username for HTTP Basic Authentication at the SOAP endpoint. */
    @NotBlank private String username;

    /** Password for HTTP Basic Authentication at the SOAP endpoint. */
    @NotBlank private String password;

}
