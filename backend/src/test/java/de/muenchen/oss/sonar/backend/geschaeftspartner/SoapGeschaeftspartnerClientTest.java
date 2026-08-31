package de.muenchen.oss.sonar.backend.geschaeftspartner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerDTOMapper;
import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerResponseDTO;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.BAPIRET2;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCAGPMIFBUPAREADRFC;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCAGPMIFBUPAREADRFCPortType;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCAGPMIFBUPAREADRFCResponse;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCASGPMIFBPADDRESS;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCASGPMIFBPCENTRAL;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCASGPMIFBPORG;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCASGPMIFBPPERS;
import jakarta.xml.ws.WebServiceException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class SoapGeschaeftspartnerClientTest {

    @Mock
    private ZFMCAGPMIFBUPAREADRFCPortType geschaeftspartnerPort;

    @Spy
    private final GeschaeftspartnerDTOMapper geschaeftspartnerDTOMapper = Mappers.getMapper(GeschaeftspartnerDTOMapper.class);

    @InjectMocks
    private SoapGeschaeftspartnerClient unitUnderTest;

    private final ListAppender<ILoggingEvent> loggedEvents = new ListAppender<>();

    private static Logger clientLogger() {
        return (Logger) LoggerFactory.getLogger(SoapGeschaeftspartnerClient.class);
    }

    @BeforeEach
    void setUp() {
        loggedEvents.start();
        clientLogger().addAppender(loggedEvents);
    }

    @AfterEach
    void tearDown() {
        clientLogger().detachAppender(loggedEvents);
    }

    @Nested
    class FindById {
        @Test
        void givenOrganisation_thenMapTheResponseToTheDTO() {
            final ZFMCASGPMIFBPORG org = new ZFMCASGPMIFBPORG();
            org.setNAME1("Musterfirma");
            org.setNAME2("Zweigstelle Süd");
            final ZFMCASGPMIFBPCENTRAL central = new ZFMCASGPMIFBPCENTRAL();
            central.setTITLEMEDI("Firma");
            final ZFMCASGPMIFBPADDRESS address = new ZFMCASGPMIFBPADDRESS();
            address.setCONAME("c/o Hausverwaltung");
            address.setSTREET("Musterstraße");
            address.setHOUSENO("1");
            address.setSTRSUPPL1("Rückgebäude");
            address.setPOSTLCOD1("80331");
            address.setCITY("München");
            address.setCOUNTRY("DE");
            address.setTELNO("+49 89 233-00");
            address.setTELNOMOBIL("+49 170 1234567");
            address.setFAXNO("+49 89 233-99");
            address.setEMAIL("kontakt@musterfirma.example");
            address.setADRNOTES("Zustellung nur werktags");
            final ZFMCAGPMIFBUPAREADRFCResponse response = new ZFMCAGPMIFBUPAREADRFCResponse();
            response.setPESGPDATACENTRAL(central);
            response.setPESGPDATAORG(org);
            response.setPESGPDATAADDRESS(address);
            when(geschaeftspartnerPort.zFMCAGPMIFBUPAREADRFC(any())).thenReturn(response);

            final Optional<GeschaeftspartnerResponseDTO> result = unitUnderTest.findById("4711");

            assertThat(result).contains(new GeschaeftspartnerResponseDTO(
                    "Firma", "Musterfirma", "Zweigstelle Süd", null, null, null, null,
                    "c/o Hausverwaltung", "Musterstraße", "1", "Rückgebäude", "80331", "München", "DE",
                    "+49 89 233-00", "+49 170 1234567", "+49 89 233-99", "kontakt@musterfirma.example",
                    "Zustellung nur werktags"));
        }

        @Test
        void givenPerson_thenMapTheNameFromThePersonStructure() {
            final ZFMCASGPMIFBPPERS person = new ZFMCASGPMIFBPPERS();
            person.setFIRSTNAME("Erika");
            person.setLASTNAME("Mustermann");
            final ZFMCAGPMIFBUPAREADRFCResponse response = new ZFMCAGPMIFBUPAREADRFCResponse();
            response.setPESGPDATAPERS(person);
            when(geschaeftspartnerPort.zFMCAGPMIFBUPAREADRFC(any())).thenReturn(response);

            final Optional<GeschaeftspartnerResponseDTO> result = unitUnderTest.findById("4711");

            assertThat(result).isPresent();
            assertThat(result.get().vorname()).isEqualTo("Erika");
            assertThat(result.get().nachname()).isEqualTo("Mustermann");
            assertThat(result.get().name1()).isNull();
        }

        @Test
        void givenId_thenSendItAsThePartnerNumber() {
            when(geschaeftspartnerPort.zFMCAGPMIFBUPAREADRFC(any())).thenReturn(new ZFMCAGPMIFBUPAREADRFCResponse());

            unitUnderTest.findById("4711");

            final ArgumentCaptor<ZFMCAGPMIFBUPAREADRFC> request = ArgumentCaptor.forClass(ZFMCAGPMIFBUPAREADRFC.class);
            verify(geschaeftspartnerPort).zFMCAGPMIFBUPAREADRFC(request.capture());
            assertThat(request.getValue().getPIGPART()).isEqualTo("4711");
            assertThat(request.getValue().getPIADRNR()).isNull();
        }

        @Test
        void givenEmptyResponse_thenReturnEmpty() {
            when(geschaeftspartnerPort.zFMCAGPMIFBUPAREADRFC(any())).thenReturn(new ZFMCAGPMIFBUPAREADRFCResponse());

            assertThat(unitUnderTest.findById("0000000000")).isEmpty();
        }

        @Test
        void givenBlankStructures_thenReturnEmpty() {
            final ZFMCAGPMIFBUPAREADRFCResponse response = new ZFMCAGPMIFBUPAREADRFCResponse();
            response.setPESGPDATAORG(new ZFMCASGPMIFBPORG());
            response.setPESGPDATAADDRESS(new ZFMCASGPMIFBPADDRESS());
            when(geschaeftspartnerPort.zFMCAGPMIFBUPAREADRFC(any())).thenReturn(response);

            assertThat(unitUnderTest.findById("0000000000")).isEmpty();
        }

        @Test
        void givenFailureMessage_thenLogIt() {
            final BAPIRET2 message = new BAPIRET2();
            message.setTYPE("W");
            message.setID("ZFMCA_GPM");
            message.setNUMBER("012");
            message.setMESSAGE("Adresse ist nicht die Standardadresse");
            final ZFMCAGPMIFBUPAREADRFCResponse response = new ZFMCAGPMIFBUPAREADRFCResponse();
            response.setPETRETURN(new ZFMCAGPMIFBUPAREADRFCResponse.PETRETURN());
            response.getPETRETURN().getItem().add(message);
            when(geschaeftspartnerPort.zFMCAGPMIFBUPAREADRFC(any())).thenReturn(response);

            unitUnderTest.findById("4711");

            assertThat(loggedEvents.list).singleElement().satisfies(event -> {
                assertThat(event.getLevel()).isEqualTo(Level.WARN);
                assertThat(event.getFormattedMessage()).contains("Adresse ist nicht die Standardadresse");
            });
        }

        @Test
        void givenSuccessMessage_thenDoNotLogIt() {
            final ZFMCASGPMIFBPORG org = new ZFMCASGPMIFBPORG();
            org.setNAME1("Musterfirma");
            final BAPIRET2 message = new BAPIRET2();
            message.setTYPE("S");
            message.setID("ZFMCA_GPM");
            message.setNUMBER("012");
            message.setMESSAGE("Geschäftspartner gelesen");
            final ZFMCAGPMIFBUPAREADRFCResponse response = new ZFMCAGPMIFBUPAREADRFCResponse();
            response.setPESGPDATAORG(org);
            response.setPETRETURN(new ZFMCAGPMIFBUPAREADRFCResponse.PETRETURN());
            response.getPETRETURN().getItem().add(message);
            when(geschaeftspartnerPort.zFMCAGPMIFBUPAREADRFC(any())).thenReturn(response);

            assertThat(unitUnderTest.findById("4711")).isPresent();
            assertThat(loggedEvents.list).isEmpty();
        }

        @Test
        void givenTransportFailure_thenThrowGatewayError() {
            when(geschaeftspartnerPort.zFMCAGPMIFBUPAREADRFC(any())).thenThrow(new WebServiceException("connection refused"));

            assertThatThrownBy(() -> unitUnderTest.findById("4711"))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                            exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY))
                    .hasRootCauseInstanceOf(WebServiceException.class);
        }
    }
}
