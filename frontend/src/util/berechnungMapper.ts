import type {
  AbrechnungNutzungsobjektResponseDTO,
  AbrechnungPositionResponseDTO,
  AbrechnungResponseDTO,
  BerechnungNutzungsobjektRequestDTO,
  BerechnungPositionRequestDTO,
  BerechnungRequestDTO,
  ProjektResponseDTO,
} from "@/api/generated/sonar-backend";

import { adressbezeichnung } from "@/util/adresseLabel";

export function toBerechnungRequestDTO(
  abrechnung: AbrechnungResponseDTO,
  projekt: ProjektResponseDTO
): BerechnungRequestDTO {
  if (projekt.projektnummer === undefined) {
    throw new Error("Die Projektnummer fehlt.");
  }

  return {
    projektnummer: projekt.projektnummer,
    nutzungsobjekte: (abrechnung.nutzungsobjekte ?? []).map(
      toNutzungsobjektRequestDTO
    ),
  };
}

function toNutzungsobjektRequestDTO(
  nutzungsobjekt: AbrechnungNutzungsobjektResponseDTO
): BerechnungNutzungsobjektRequestDTO {
  if (nutzungsobjekt.id === undefined) {
    throw new Error("Die ID eines Nutzungsobjekts fehlt.");
  }

  return {
    nutzungsobjektId: nutzungsobjekt.id,
    adressbezeichnung: adressbezeichnung(nutzungsobjekt),
    positionen: (nutzungsobjekt.positionen ?? []).map(toPositionRequestDTO),
  };
}

function toPositionRequestDTO(
  position: AbrechnungPositionResponseDTO
): BerechnungPositionRequestDTO {
  if (position.id === undefined) {
    throw new Error("Die ID einer Position fehlt.");
  }
  if (position.beginn === undefined || position.ende === undefined) {
    throw new Error("Der Zeitraum einer Position fehlt.");
  }
  if (position.flaeche === undefined) {
    throw new Error("Die Fläche einer Position fehlt.");
  }

  return {
    positionId: position.id,
    beginn: position.beginn,
    ende: position.ende,
    flaecheQm: position.flaeche,
  };
}
