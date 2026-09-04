package de.muenchen.oss.sonar.backend.geschaeftspartner.dto;

/**
 * The master data of a Geschäftspartner, read from a third party system owning it.
 *
 * @param anrede Title text
 * @param name1 Name 1 of organization
 * @param name2 Name 2 of organization
 * @param name3 Name 3 of organization
 * @param name4 Name 4 of organization
 * @param vorname First name of business partner (person)
 * @param nachname Last name of business partner (person)
 * @param coName c/o name
 * @param strasse Street
 * @param hausnummer House Number
 * @param adresszusatz Street 2
 * @param postleitzahl City postal code
 * @param ort City
 * @param land Country Key
 * @param telefon Complete number: dialling code+number+extension
 * @param mobil Complete number: dialling code+number+extension
 * @param fax Complete number: dialling code+number+extension
 * @param email E-Mail Address
 * @param adressnotiz Address notes
 */
public record GeschaeftspartnerResponseDTO(
        String anrede,
        String name1,
        String name2,
        String name3,
        String name4,
        String vorname,
        String nachname,
        String coName,
        String strasse,
        String hausnummer,
        String adresszusatz,
        String postleitzahl,
        String ort,
        String land,
        String telefon,
        String mobil,
        String fax,
        String email,
        String adressnotiz) {
}
