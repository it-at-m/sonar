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
     * {@link FakeGeschaeftspartnerClient}. A blank value therefore fails when the SOAP client is built,
     * which happens in exactly those profiles that need it.
     * </p>
     */
    private String url;

}
