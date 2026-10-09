import type { Adresse } from "@/types/common/Adresse";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungenEnum,
} from "@/api/generated/sonar-backend";

export function toAdresseRequestFields(adresse: Adresse) {
  const isAdresse = adresse.art === ProjektAdresseRequestDTOArtEnum.ADRESSE;
  const isSonstigeNutzung = adresse.nutzungen.includes(
    ProjektAdresseRequestDTONutzungenEnum.SONSTIGES
  );
  return {
    art: adresse.art,
    adresse: isAdresse ? adresse.adresse.trim() : undefined,
    hausnummerVon: isAdresse ? adresse.hausnummerVon.trim() : undefined,
    hausnummerBis: isAdresse
      ? adresse.hausnummerBis.trim() || undefined
      : undefined,
    flurstueck: isAdresse ? undefined : adresse.flurstueck.trim(),
    gemarkung: isAdresse ? undefined : adresse.gemarkung.trim(),
    nutzungen: adresse.nutzungen,
    nutzungSonstiges: isSonstigeNutzung
      ? adresse.nutzungSonstiges.trim() || undefined
      : undefined,
  };
}
