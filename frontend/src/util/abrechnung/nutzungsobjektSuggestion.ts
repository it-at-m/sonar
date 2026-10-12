import type { NutzungsobjektResponseDTO } from "@/api/generated/sonar-backend";
import type { AbrechnungNutzungsobjektForm } from "@/types/abrechnung/AbrechnungNutzungsobjektForm";
import type { NutzungsobjektSuggestion } from "@/types/abrechnung/NutzungsobjektSuggestion";

import { ApiFactory } from "@/api/ApiFactory";
import {
  AbrechnungControllerApi,
  ProjektAdresseRequestDTOArtEnum,
} from "@/api/generated/sonar-backend";
import { toIsoDateString } from "@/util/formatter";

export async function fetchNutzungsobjektSuggestions(
  projektId: string
): Promise<NutzungsobjektSuggestion[]> {
  const nutzungsobjekte = await ApiFactory.getInstance(
    AbrechnungControllerApi
  ).getNutzungsobjekte(projektId);
  return nutzungsobjekte.filter(hasId).map(toNutzungsobjektSuggestion);
}

function hasId(
  nutzungsobjekt: NutzungsobjektResponseDTO
): nutzungsobjekt is NutzungsobjektResponseDTO & { id: string } {
  return nutzungsobjekt.id !== undefined;
}

function toNutzungsobjektSuggestion(
  nutzungsobjekt: NutzungsobjektResponseDTO & { id: string }
): NutzungsobjektSuggestion {
  return {
    id: nutzungsobjekt.id,
    art: nutzungsobjekt.art ?? ProjektAdresseRequestDTOArtEnum.ADRESSE,
    adresse: nutzungsobjekt.adresse ?? "",
    hausnummerVon: nutzungsobjekt.hausnummerVon ?? "",
    hausnummerBis: nutzungsobjekt.hausnummerBis ?? "",
    flurstueck: nutzungsobjekt.flurstueck ?? "",
    gemarkung: nutzungsobjekt.gemarkung ?? "",
    nutzung: nutzungsobjekt.nutzung ?? null,
    unerlaubteNutzungVon: toIsoDateString(nutzungsobjekt.unerlaubteNutzungVon),
    unerlaubteNutzungBis: toIsoDateString(nutzungsobjekt.unerlaubteNutzungBis),
    tageUnerlaubteNutzung: nutzungsobjekt.tageUnerlaubteNutzung ?? null,
    bemerkung: nutzungsobjekt.bemerkung ?? "",
    aufschlag50prozent: nutzungsobjekt.aufschlag50prozent ?? false,
  };
}

export function applyNutzungsobjektSuggestion(
  nutzungsobjekt: AbrechnungNutzungsobjektForm,
  suggestion: NutzungsobjektSuggestion
): void {
  const { id, ...uebernommeneDaten } = suggestion;
  Object.assign(nutzungsobjekt, uebernommeneDaten);
  nutzungsobjekt.uebernommenesNutzungsobjektId = id;
}
