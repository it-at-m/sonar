import type { ProjektRequestDTO } from "@/api/generated/sonar-backend";
import type { Projekt } from "@/types/Projekt";

import { toProjektAdresseRequestDTO } from "@/util/projektAdresseMapper";

export function toProjektRequestDTO(projekt: Projekt): ProjektRequestDTO {
  return {
    projektnummer: projekt.projektnummer.trim(),
    abrechnungBeginn: new Date(projekt.abrechnungBeginn),
    abrechnungEnde: new Date(projekt.abrechnungEnde),
    adressen: projekt.adressen.map(toProjektAdresseRequestDTO),
  };
}
