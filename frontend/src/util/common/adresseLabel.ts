import type { Adresse } from "@/types/common/Adresse";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";
import { NUTZUNG_OPTIONS } from "@/util/common/nutzungOptions";

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

export function adresseTitle(adresse: Adresse): string {
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

export function nutzungTitle(adresse: Adresse): string {
  return (
    NUTZUNG_OPTIONS.find((option) => option.value === adresse.nutzung)?.title ??
    ""
  );
}
