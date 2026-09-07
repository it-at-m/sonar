package de.muenchen.oss.sonar.backend.configuration;

import de.muenchen.oss.sonar.backend.geschaeftspartner.GeschaeftspartnerProperties;
import de.muenchen.oss.sonar.backend.geschaeftspartner.client.GeschaeftspartnerClient;
import de.muenchen.oss.sonar.backend.geschaeftspartner.client.MockGeschaeftspartnerClient;
import de.muenchen.oss.sonar.backend.geschaeftspartner.client.SoapGeschaeftspartnerClient;
import de.muenchen.oss.sonar.backend.geschaeftspartner.client.UnconfiguredGeschaeftspartnerClient;
import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerDTOMapper;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCAGPMIFBUPAREADRFCPortType;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;

@Configuration
@Slf4j
public class GeschaeftspartnerClientConfiguration {

    /** Canned data, because neither profile has a system to call. */
    @Bean
    @Profile({ "local", "test" })
    public GeschaeftspartnerClient mockGeschaeftspartnerClient() {
        return new MockGeschaeftspartnerClient();
    }

    /** Built code first from the generated port, so CXF does not fetch the contract at runtime. */
    @Bean
    @Profile("!local & !test")
    public GeschaeftspartnerClient geschaeftspartnerClient(final GeschaeftspartnerProperties properties,
            final GeschaeftspartnerDTOMapper geschaeftspartnerDTOMapper) {
        if (properties.getUrl() == null || properties.getUrl().isBlank()) {
            log.error("sonar.geschaeftspartner.client.url is not set, every lookup of a Geschaeftspartner answers with {}",
                    HttpStatus.BAD_GATEWAY);
            return new UnconfiguredGeschaeftspartnerClient();
        }

        final JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(ZFMCAGPMIFBUPAREADRFCPortType.class);
        factory.setAddress(properties.getUrl());
        final ZFMCAGPMIFBUPAREADRFCPortType port = factory.create(ZFMCAGPMIFBUPAREADRFCPortType.class);

        return new SoapGeschaeftspartnerClient(port, geschaeftspartnerDTOMapper);
    }

}
