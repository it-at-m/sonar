import type { ProjektResponseDTO } from "@/api/generated/sonar-backend";
import type { ProjektTableRow } from "@/types/ProjektTableRow";

import { toDateString } from "@/util/formatter";

export function toProjektTableRow(
  projekt: ProjektResponseDTO
): ProjektTableRow {
  return {
    id: projekt.id,
    projektnummer: projekt.projektnummer,
    abrechnungBeginn: projekt.abrechnungBeginn
      ? toDateString(projekt.abrechnungBeginn)
      : "",
    abrechnungEnde: projekt.abrechnungEnde
      ? toDateString(projekt.abrechnungEnde)
      : "",
    anzahlAdressen: projekt.adressen?.length ?? 0,
  };
}
