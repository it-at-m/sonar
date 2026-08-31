package de.muenchen.oss.sonar.backend.geschaeftspartner.dto;

import de.muenchen.oss.sonar.backend.geschaeftspartner.ws.ZFMCAGPMIFBUPAREADRFCResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface GeschaeftspartnerDTOMapper {

    @Mapping(target = "anrede", source = "PESGPDATACENTRAL.TITLEMEDI")
    @Mapping(target = "name1", source = "PESGPDATAORG.NAME1")
    @Mapping(target = "name2", source = "PESGPDATAORG.NAME2")
    @Mapping(target = "name3", source = "PESGPDATAORG.NAME3")
    @Mapping(target = "name4", source = "PESGPDATAORG.NAME4")
    @Mapping(target = "vorname", source = "PESGPDATAPERS.FIRSTNAME")
    @Mapping(target = "nachname", source = "PESGPDATAPERS.LASTNAME")
    @Mapping(target = "coName", source = "PESGPDATAADDRESS.CONAME")
    @Mapping(target = "strasse", source = "PESGPDATAADDRESS.STREET")
    @Mapping(target = "hausnummer", source = "PESGPDATAADDRESS.HOUSENO")
    @Mapping(target = "adresszusatz", source = "PESGPDATAADDRESS.STRSUPPL1")
    @Mapping(target = "postleitzahl", source = "PESGPDATAADDRESS.POSTLCOD1")
    @Mapping(target = "ort", source = "PESGPDATAADDRESS.CITY")
    @Mapping(target = "land", source = "PESGPDATAADDRESS.COUNTRY")
    @Mapping(target = "telefon", source = "PESGPDATAADDRESS.TELNO")
    @Mapping(target = "mobil", source = "PESGPDATAADDRESS.TELNOMOBIL")
    @Mapping(target = "fax", source = "PESGPDATAADDRESS.FAXNO")
    @Mapping(target = "email", source = "PESGPDATAADDRESS.EMAIL")
    @Mapping(target = "adressnotiz", source = "PESGPDATAADDRESS.ADRNOTES")
    GeschaeftspartnerResponseDTO toDTO(ZFMCAGPMIFBUPAREADRFCResponse response);

}
