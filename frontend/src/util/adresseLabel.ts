import type { Adresse } from "@/types/Adresse";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";

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
