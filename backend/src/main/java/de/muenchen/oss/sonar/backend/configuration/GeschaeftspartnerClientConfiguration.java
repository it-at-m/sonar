package de.muenchen.oss.sonar.backend.configuration;

import de.muenchen.oss.sonar.backend.geschaeftspartner.GeschaeftspartnerProperties;
import de.muenchen.oss.sonar.backend.geschaeftspartner.client.GeschaeftspartnerClient;
import de.muenchen.oss.sonar.backend.geschaeftspartner.client.MockGeschaeftspartnerClient;
import de.muenchen.oss.sonar.backend.geschaeftspartner.client.SoapGeschaeftspartnerClient;
import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerDTOMapper;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCAGPMIFBUPAREADRFCPortType;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
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
        final JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(ZFMCAGPMIFBUPAREADRFCPortType.class);
        factory.setAddress(properties.getUrl());
        final ZFMCAGPMIFBUPAREADRFCPortType port = factory.create(ZFMCAGPMIFBUPAREADRFCPortType.class);
        final AuthorizationPolicy authorization = new AuthorizationPolicy();
        authorization.setAuthorizationType("Basic");
        authorization.setUserName(properties.getUsername());
        authorization.setPassword(properties.getPassword());
        ((HTTPConduit) ClientProxy.getClient(port).getConduit()).setAuthorization(authorization);

        return new SoapGeschaeftspartnerClient(port, geschaeftspartnerDTOMapper);
    }

}
