import type { Adresse } from "@/types/common/Adresse";
import type { UnerlaubteNutzung } from "@/types/common/UnerlaubteNutzung";
import type { ProjektAdresseSuggestion } from "@/types/projekt/ProjektAdresseSuggestion";

import { ApiFactory } from "@/api/ApiFactory";
import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektControllerApi,
} from "@/api/generated/sonar-backend";
import { NUTZUNG_OPTIONS } from "@/util/common/nutzungOptions";
import { toProjektAdresseSuggestion } from "@/util/projekt/projektAdresseMapper";

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
  if (suggestion.art === ProjektAdresseRequestDTOArtEnum.FLURSTUECK) {
    const flurstueck = [suggestion.flurstueck, suggestion.gemarkung]
      .filter(Boolean)
      .join(", ");
    return `Flurstück ${flurstueck}`;
  }
  const hausnummer = suggestion.hausnummerBis
    ? `${suggestion.hausnummerVon}–${suggestion.hausnummerBis}`
    : suggestion.hausnummerVon;
  return [suggestion.adresse, hausnummer].filter(Boolean).join(" ");
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
