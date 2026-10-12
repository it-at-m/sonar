import type { Adresse } from "@/types/common/Adresse";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungenEnum,
} from "@/api/generated/sonar-backend";

export function clearFieldsOfUnselectedArt(adresse: Adresse): void {
  if (adresse.art === ProjektAdresseRequestDTOArtEnum.ADRESSE) {
    adresse.flurstueck = "";
    adresse.gemarkung = "";
  } else {
    adresse.adresse = "";
    adresse.hausnummerVon = "";
    adresse.hausnummerBis = "";
  }
}

export function clearSonstigesOfUnselectedNutzung(adresse: Adresse): void {
  if (
    !adresse.nutzungen.includes(ProjektAdresseRequestDTONutzungenEnum.SONSTIGES)
  ) {
    adresse.nutzungSonstiges = "";
  }
}

export function isAdresseDirty(adresse: Adresse): boolean {
  return (
    adresse.art !== ProjektAdresseRequestDTOArtEnum.ADRESSE ||
    adresse.adresse !== "" ||
    adresse.hausnummerVon !== "" ||
    adresse.hausnummerBis !== "" ||
    adresse.flurstueck !== "" ||
    adresse.gemarkung !== "" ||
    adresse.nutzungen.length > 0 ||
    adresse.nutzungSonstiges !== ""
  );
}
