package de.muenchen.oss.sonar.backend.geschaeftspartner.client;

import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerDTOMapper;
import de.muenchen.oss.sonar.backend.geschaeftspartner.dto.GeschaeftspartnerResponseDTO;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.BAPIRET2;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCAGPMIFBUPAREADRFC;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCAGPMIFBUPAREADRFCPortType;
import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCAGPMIFBUPAREADRFCResponse;
import jakarta.xml.ws.WebServiceException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * The port is the interface the cxf-codegen-plugin generates from
 * {@code src/main/resources/wsdl/Z_FMCA_GPM_IF_BUPA_READ_RFC.wsdl.xml}.
 */
@Slf4j
@RequiredArgsConstructor
public class SoapGeschaeftspartnerClient implements GeschaeftspartnerClient {

    /** The SAP message class for a plain success. */
    private static final String SUCCESS_TYPE = "S";

    private final ZFMCAGPMIFBUPAREADRFCPortType geschaeftspartnerPort;
    private final GeschaeftspartnerDTOMapper geschaeftspartnerDTOMapper;

    @Override
    public Optional<GeschaeftspartnerResponseDTO> findById(final String geschaeftspartnerId) {
        final ZFMCAGPMIFBUPAREADRFC request = new ZFMCAGPMIFBUPAREADRFC();
        request.setPIGPART(geschaeftspartnerId);

        final ZFMCAGPMIFBUPAREADRFCResponse response;
        try {
            response = geschaeftspartnerPort.zFMCAGPMIFBUPAREADRFC(request);
        } catch (final WebServiceException exception) {
            log.warn("Could not read Geschaeftspartner {} from the third party system", geschaeftspartnerId, exception);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, MSG_NOT_REACHABLE, exception);
        }

        if (response == null) {
            return Optional.empty();
        }
        logReturnMessages(geschaeftspartnerId, response);

        final GeschaeftspartnerResponseDTO geschaeftspartner = geschaeftspartnerDTOMapper.toDTO(response);
        return isEmpty(geschaeftspartner) ? Optional.empty() : Optional.of(geschaeftspartner);
    }

    private static void logReturnMessages(final String geschaeftspartnerId, final ZFMCAGPMIFBUPAREADRFCResponse response) {
        if (response.getPETRETURN() == null) {
            return;
        }
        final List<BAPIRET2> messages = response.getPETRETURN().getItem();
        for (final BAPIRET2 message : messages) {
            if (SUCCESS_TYPE.equals(message.getTYPE())) {
                continue;
            }
            log.warn("Reading Geschaeftspartner {} reported {} {}{}: {}",
                    geschaeftspartnerId, message.getTYPE(), message.getID(), message.getNUMBER(), message.getMESSAGE());
        }
    }

    private static boolean isEmpty(final GeschaeftspartnerResponseDTO geschaeftspartner) {
        return Stream.of(
                geschaeftspartner.anrede(), geschaeftspartner.name1(), geschaeftspartner.name2(),
                geschaeftspartner.name3(), geschaeftspartner.name4(), geschaeftspartner.vorname(),
                geschaeftspartner.nachname(), geschaeftspartner.coName(), geschaeftspartner.strasse(),
                geschaeftspartner.hausnummer(), geschaeftspartner.adresszusatz(), geschaeftspartner.postleitzahl(),
                geschaeftspartner.ort(), geschaeftspartner.land(), geschaeftspartner.telefon(),
                geschaeftspartner.mobil(), geschaeftspartner.fax(), geschaeftspartner.email(),
                geschaeftspartner.adressnotiz())
                .allMatch(value -> value == null || value.isBlank());
    }

}
