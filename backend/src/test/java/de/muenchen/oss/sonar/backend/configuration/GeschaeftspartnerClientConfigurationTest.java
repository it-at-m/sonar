package de.muenchen.oss.sonar.backend.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import de.muenchen.oss.sonar.backend.geschaeftspartner.GeschaeftspartnerProperties;
import de.muenchen.oss.sonar.backend.geschaeftspartner.client.SoapGeschaeftspartnerClient;
import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerDTOMapper;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCAGPMIFBUPAREADRFCPortType;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class GeschaeftspartnerClientConfigurationTest {

    private final GeschaeftspartnerClientConfiguration unitUnderTest = new GeschaeftspartnerClientConfiguration();

    private final GeschaeftspartnerDTOMapper geschaeftspartnerDTOMapper = Mappers.getMapper(GeschaeftspartnerDTOMapper.class);

    @Nested
    class GeschaeftspartnerClientBean {
        @Test
        void givenUrl_thenCreateTheSoapClient() {
            final GeschaeftspartnerProperties properties = new GeschaeftspartnerProperties();
            properties.setUrl("https://example.muenchen.de/v1/geschaeftspartner");
            properties.setUsername("username");
            properties.setPassword("password");

            final SoapGeschaeftspartnerClient client = (SoapGeschaeftspartnerClient) unitUnderTest
                    .geschaeftspartnerClient(properties, geschaeftspartnerDTOMapper);

            assertThat(client).isInstanceOf(SoapGeschaeftspartnerClient.class);
            final ZFMCAGPMIFBUPAREADRFCPortType port = org.assertj.core.util.introspection.FieldSupport.extraction()
                    .fieldValue("geschaeftspartnerPort", ZFMCAGPMIFBUPAREADRFCPortType.class, client);
            final AuthorizationPolicy authorization = ((HTTPConduit) ClientProxy.getClient(port).getConduit()).getAuthorization();
            assertThat(authorization.getAuthorizationType()).isEqualTo("Basic");
            assertThat(authorization.getUserName()).isEqualTo("username");
            assertThat(authorization.getPassword()).isEqualTo("password");
        }
    }
}
