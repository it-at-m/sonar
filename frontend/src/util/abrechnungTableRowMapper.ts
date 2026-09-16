import type { AbrechnungResponseDTO } from "@/api/generated/sonar-backend";
import type { AbrechnungTableRow } from "@/types/AbrechnungTableRow";

import { abrechnungsArtLabel } from "@/util/abrechnungsArt";
import { toDateString } from "@/util/formatter";

export function toAbrechnungTableRow(
  abrechnung: AbrechnungResponseDTO
): AbrechnungTableRow {
  return {
    id: abrechnung.id,
    geschaeftspartnerId: abrechnung.geschaeftspartnerId ?? "",
    zeitraumVon: abrechnung.zeitraumVon
      ? toDateString(abrechnung.zeitraumVon)
      : "",
    zeitraumBis: abrechnung.zeitraumBis
      ? toDateString(abrechnung.zeitraumBis)
      : "",
    abrechnungsArt: abrechnungsArtLabel(abrechnung.abrechnungsArt),
    anzahlNutzungsobjekte: abrechnung.nutzungsobjekte?.length ?? 0,
    widerspruchVorhanden: abrechnung.widerspruchVorhanden ?? false,
  };
}
