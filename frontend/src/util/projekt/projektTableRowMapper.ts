import type { ProjektResponseDTO } from "@/api/generated/sonar-backend";
import type { ProjektTableRow } from "@/types/projekt/ProjektTableRow";

import { toDateString } from "@/util/formatter";
import { stadtbezirkLabel } from "@/util/projekt/stadtbezirk";

export function toProjektTableRow(
  projekt: ProjektResponseDTO
): ProjektTableRow {
  return {
    id: projekt.id,
    projektnummer: projekt.projektnummer,
    stadtbezirk: stadtbezirkLabel(projekt.stadtbezirk),
    abrechnungBeginn: projekt.abrechnungBeginn
      ? toDateString(projekt.abrechnungBeginn)
      : "",
    abrechnungEnde: projekt.abrechnungEnde
      ? toDateString(projekt.abrechnungEnde)
      : "",
    anzahlAdressen: projekt.adressen?.length ?? 0,
  };
}
