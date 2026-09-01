package de.muenchen.oss.sonar.backend.geschaeftspartner;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sonar.geschaeftspartner.client")
@Data
public class GeschaeftspartnerProperties {

    /**
     * SOAP endpoint the client calls.
     * <p>
     * This class is bound in every profile, but the profiles "local" and "test" run against
     * {@link MockGeschaeftspartnerClient} and need no endpoint. In every other profile a blank value
     * wires in {@link UnconfiguredGeschaeftspartnerClient}.
     * </p>
     */
    private String url;

}
