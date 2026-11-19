import type { AbrechnungNutzungsobjektForm } from "@/types/abrechnung/AbrechnungNutzungsobjektForm";
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
  nutzungsobjekt: AbrechnungNutzungsobjektForm,
  suggestion: ProjektAdresseSuggestion
): void {
  Object.assign(nutzungsobjekt, suggestion);
  nutzungsobjekt.uebernahme = { quelle: "PROJEKT" };
}
