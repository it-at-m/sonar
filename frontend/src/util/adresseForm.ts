import type { Adresse } from "@/types/Adresse";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";

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

export function isAdresseDirty(adresse: Adresse): boolean {
  return (
    adresse.art !== ProjektAdresseRequestDTOArtEnum.ADRESSE ||
    adresse.adresse !== "" ||
    adresse.hausnummerVon !== "" ||
    adresse.hausnummerBis !== "" ||
    adresse.flurstueck !== "" ||
    adresse.gemarkung !== "" ||
    adresse.nutzung !== null
  );
}
