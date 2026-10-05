import type { Adresse } from "@/types/common/Adresse";
import type { UnerlaubteNutzung } from "@/types/common/UnerlaubteNutzung";
import type { ProjektAdresseSuggestion } from "@/types/projekt/ProjektAdresseSuggestion";

import { ApiFactory } from "@/api/ApiFactory";
import { ProjektControllerApi } from "@/api/generated/sonar-backend";
import { toProjektAdresseSuggestion } from "@/util/projekt/projektAdresseMapper";

export async function fetchProjektAdresseSuggestions(
  projektId: string
): Promise<ProjektAdresseSuggestion[]> {
  const projekt =
    await ApiFactory.getInstance(ProjektControllerApi).getProjekt(projektId);
  return (projekt.adressen ?? []).map(toProjektAdresseSuggestion);
}

export function applyProjektAdresseSuggestion(
  entry: Adresse & UnerlaubteNutzung,
  suggestion: ProjektAdresseSuggestion
): void {
  Object.assign(entry, suggestion);
}
