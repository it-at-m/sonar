package de.muenchen.oss.sonar.backend.projekt.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A linked Projekt never has verknuepfungMoeglich, because only an unlinked Projekt can get a
 * Vorgaenger.
 *
 * @param vorgaengerProjekte all Projekte before this one, from the first one to the direct
 *            Vorgaenger
 * @param nachfolgerProjekte all Projekte after this one, from the direct Nachfolger to the last one
 * @param verknuepfungAbgelehnt whether someone decided that this Projekt gets no Vorgaenger
 * @param verknuepfungMoeglich whether a Projekt sharing an Adresse can become its Vorgaenger
 */
public record ProjektResponseDTO(
        UUID id,
        String projektnummer,
        LocalDate abrechnungBeginn,
        LocalDate abrechnungEnde,
        List<ProjektAdresseResponseDTO> adressen,
        UUID vorgaengerProjektId,
        List<LinkedProjektResponseDTO> vorgaengerProjekte,
        List<LinkedProjektResponseDTO> nachfolgerProjekte,
        boolean verknuepfungAbgelehnt,
        boolean verknuepfungMoeglich) {

    /**
     * Copies the addresses and the linked Projekte in, so that the response stays immutable
     * however the caller treats the lists it passed. A missing list becomes an empty one: MapStruct
     * maps a null collection to null, and {@link List#copyOf} would reject it.
     */
    public ProjektResponseDTO {
        adressen = adressen == null ? List.of() : List.copyOf(adressen);
        vorgaengerProjekte = vorgaengerProjekte == null ? List.of() : List.copyOf(vorgaengerProjekte);
        nachfolgerProjekte = nachfolgerProjekte == null ? List.of() : List.copyOf(nachfolgerProjekte);
    }
}
