import type { ProjektAdresseRequestDTO } from "@/api/generated/sonar-backend";
import type { ProjektAdresseForm } from "@/types/ProjektAdresseForm";

import { hasZeitraum } from "@/util/projektAdresseForm";

export function toProjektAdresseRequestDTO(
  adresse: ProjektAdresseForm
): ProjektAdresseRequestDTO {
  return {
    bezeichnung: adresse.bezeichnung.trim(),
    baunutzung: adresse.baunutzung.trim() || undefined,
    unerlaubteNutzungVon: adresse.unerlaubteNutzungVon
      ? new Date(adresse.unerlaubteNutzungVon)
      : undefined,
    unerlaubteNutzungBis: adresse.unerlaubteNutzungBis
      ? new Date(adresse.unerlaubteNutzungBis)
      : undefined,
    // Sending both is rejected: with a period given the backend derives the days from it.
    tageUnerlaubteNutzung: hasZeitraum(adresse)
      ? undefined
      : (adresse.tageUnerlaubteNutzung ?? undefined),
    anzahlMahnungen: adresse.anzahlMahnungen,
    sondernutzungErlaubt: adresse.sondernutzungErlaubt,
  };
}
