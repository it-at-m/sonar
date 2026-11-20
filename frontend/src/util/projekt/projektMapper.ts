import type { ProjektRequestDTO } from "@/api/generated/sonar-backend";
import type { ProjektForm } from "@/types/projekt/ProjektForm";

import { toProjektAdresseRequestDTO } from "@/util/projekt/projektAdresseMapper";

export function toProjektRequestDTO(projekt: ProjektForm): ProjektRequestDTO {
  const stadtbezirk = projekt.stadtbezirk;
  if (stadtbezirk === null) {
    throw new Error("Der Stadtbezirk fehlt.");
  }

  return {
    projektnummer: projekt.projektnummer.trim(),
    stadtbezirk,
    abrechnungBeginn: new Date(projekt.abrechnungBeginn),
    abrechnungEnde: new Date(projekt.abrechnungEnde),
    adressen: projekt.adressen.map(toProjektAdresseRequestDTO),
  };
}
