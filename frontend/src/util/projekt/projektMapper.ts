import type { ProjektRequestDTO } from "@/api/generated/sonar-backend";
import type { ProjektForm } from "@/types/projekt/ProjektForm";

import { toProjektAdresseRequestDTO } from "@/util/projekt/projektAdresseMapper";

export function toProjektRequestDTO(projekt: ProjektForm): ProjektRequestDTO {
  return {
    projektnummer: projekt.projektnummer.trim(),
    abrechnungBeginn: new Date(projekt.abrechnungBeginn),
    abrechnungEnde: new Date(projekt.abrechnungEnde),
    adressen: projekt.adressen.map(toProjektAdresseRequestDTO),
  };
}
