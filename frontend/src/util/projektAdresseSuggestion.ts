import type { Adresse } from "@/types/Adresse";
import type { ProjektAdresseSuggestion } from "@/types/ProjektAdresseSuggestion";
import type { UnerlaubteNutzung } from "@/types/UnerlaubteNutzung";

import { ApiFactory } from "@/api/ApiFactory";
import { ProjektControllerApi } from "@/api/generated/sonar-backend";
import { adressbezeichnung } from "@/util/adresseLabel";
import { NUTZUNG_OPTIONS } from "@/util/nutzungOptions";
import { toProjektAdresseSuggestion } from "@/util/projektAdresseMapper";

export async function fetchProjektAdresseSuggestions(
  projektId: string
): Promise<ProjektAdresseSuggestion[]> {
  const projekt =
    await ApiFactory.getInstance(ProjektControllerApi).getProjekt(projektId);
  return (projekt.adressen ?? []).map(toProjektAdresseSuggestion);
}

export function projektAdresseSuggestionTitle(
  suggestion: ProjektAdresseSuggestion
): string {
  return adressbezeichnung(suggestion);
}

export function projektAdresseSuggestionSubtitle(
  suggestion: ProjektAdresseSuggestion
): string {
  return (
    NUTZUNG_OPTIONS.find((option) => option.value === suggestion.nutzung)
      ?.title ?? ""
  );
}

export function applyProjektAdresseSuggestion(
  entry: Adresse & UnerlaubteNutzung,
  suggestion: ProjektAdresseSuggestion
): void {
  Object.assign(entry, suggestion);
}
