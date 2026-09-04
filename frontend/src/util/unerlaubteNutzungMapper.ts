import type { UnerlaubteNutzung } from "@/types/UnerlaubteNutzung";

import { hasZeitraum } from "@/util/unerlaubteNutzung";

export function toUnerlaubteNutzungRequestFields(
  unerlaubteNutzung: UnerlaubteNutzung
) {
  return {
    unerlaubteNutzungVon: unerlaubteNutzung.unerlaubteNutzungVon
      ? new Date(unerlaubteNutzung.unerlaubteNutzungVon)
      : undefined,
    unerlaubteNutzungBis: unerlaubteNutzung.unerlaubteNutzungBis
      ? new Date(unerlaubteNutzung.unerlaubteNutzungBis)
      : undefined,
    // Sending both is rejected: with a period given the backend derives the days from it.
    tageUnerlaubteNutzung: hasZeitraum(unerlaubteNutzung)
      ? undefined
      : (unerlaubteNutzung.tageUnerlaubteNutzung ?? undefined),
  };
}
