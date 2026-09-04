import type { Adresse } from "@/types/Adresse";
import type { ProjektAdresseSuggestion } from "@/types/ProjektAdresseSuggestion";
import type { UnerlaubteNutzung } from "@/types/UnerlaubteNutzung";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";
import { NUTZUNG_OPTIONS } from "@/util/nutzungOptions";

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
