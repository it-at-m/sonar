package de.muenchen.oss.sonar.backend.geschaeftspartner;

import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerDTOMapper;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCAGPMIFBUPAREADRFCPortType;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class GeschaeftspartnerClientConfiguration {

    /** Canned data, because neither profile has a system to call. */
    @Bean
    @Profile({ "local", "test" })
    public GeschaeftspartnerClient fakeGeschaeftspartnerClient() {
        return new FakeGeschaeftspartnerClient();
    }

    /** Built code first from the generated port, so CXF does not fetch the contract at runtime. */
    @Bean
    @Profile("!local & !test")
    public GeschaeftspartnerClient soapGeschaeftspartnerClient(final GeschaeftspartnerProperties properties,
            final GeschaeftspartnerDTOMapper geschaeftspartnerDTOMapper) {
        if (properties.getUrl() == null || properties.getUrl().isBlank()) {
            throw new IllegalStateException(
                    "sonar.geschaeftspartner.client.url must be set outside the profiles local and test");
        }

        final JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(ZFMCAGPMIFBUPAREADRFCPortType.class);
        factory.setAddress(properties.getUrl());
        final ZFMCAGPMIFBUPAREADRFCPortType port = factory.create(ZFMCAGPMIFBUPAREADRFCPortType.class);

        return new SoapGeschaeftspartnerClient(port, geschaeftspartnerDTOMapper);
    }

}
