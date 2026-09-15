import type { Adresse } from "@/types/Adresse";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";

interface AdresseTeile {
  art?: ProjektAdresseRequestDTOArtEnum;
  adresse?: string;
  hausnummerVon?: string;
  hausnummerBis?: string;
  flurstueck?: string;
  gemarkung?: string;
}

/** The Art decides the word, so an entry reads either "Adresse 1" or "Flurstück 1". */
export function adresseLabel(
  adresse: Adresse,
  oneBasedPosition: number
): string {
  const art =
    adresse.art === ProjektAdresseRequestDTOArtEnum.FLURSTUECK
      ? "Flurstück"
      : "Adresse";
  return `${art} ${oneBasedPosition}`;
}

export function adressbezeichnung(adresse: AdresseTeile): string {
  if (adresse.art === ProjektAdresseRequestDTOArtEnum.FLURSTUECK) {
    const flurstueck = [adresse.flurstueck, adresse.gemarkung]
      .filter(Boolean)
      .join(", ");
    return `Flurstück ${flurstueck}`;
  }
  const hausnummer = adresse.hausnummerBis
    ? `${adresse.hausnummerVon}–${adresse.hausnummerBis}`
    : adresse.hausnummerVon;
  return [adresse.adresse, hausnummer].filter(Boolean).join(" ");
}
