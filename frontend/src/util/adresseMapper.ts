import type { Adresse } from "@/types/Adresse";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";

export function toAdresseRequestFields(adresse: Adresse) {
  const isAdresse = adresse.art === ProjektAdresseRequestDTOArtEnum.ADRESSE;
  return {
    art: adresse.art,
    adresse: isAdresse ? adresse.adresse.trim() : undefined,
    hausnummerVon: isAdresse ? adresse.hausnummerVon.trim() : undefined,
    hausnummerBis: isAdresse
      ? adresse.hausnummerBis.trim() || undefined
      : undefined,
    flurstueck: isAdresse ? undefined : adresse.flurstueck.trim(),
    gemarkung: isAdresse ? undefined : adresse.gemarkung.trim(),
    nutzung: adresse.nutzung ?? undefined,
  };
}
