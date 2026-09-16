import type {
  ProjektAdresseRequestDTO,
  ProjektAdresseResponseDTO,
} from "@/api/generated/sonar-backend";
import type { ProjektAdresseForm } from "@/types/ProjektAdresseForm";
import type { ProjektAdresseSuggestion } from "@/types/ProjektAdresseSuggestion";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";
import { toAdresseRequestFields } from "@/util/adresseMapper";
import { toIsoDateString } from "@/util/formatter";
import { toUnerlaubteNutzungRequestFields } from "@/util/unerlaubteNutzungMapper";

export function toProjektAdresseRequestDTO(
  adresse: ProjektAdresseForm
): ProjektAdresseRequestDTO {
  return {
    ...toAdresseRequestFields(adresse),
    ...toUnerlaubteNutzungRequestFields(adresse),
    anzahlMahnungen: adresse.anzahlMahnungen,
    sondernutzungErlaubt: adresse.sondernutzungErlaubt,
  };
}

export function toProjektAdresseSuggestion(
  adresse: ProjektAdresseResponseDTO
): ProjektAdresseSuggestion {
  return {
    art: adresse.art ?? ProjektAdresseRequestDTOArtEnum.ADRESSE,
    adresse: adresse.adresse ?? "",
    hausnummerVon: adresse.hausnummerVon ?? "",
    hausnummerBis: adresse.hausnummerBis ?? "",
    flurstueck: adresse.flurstueck ?? "",
    gemarkung: adresse.gemarkung ?? "",
    nutzung: adresse.nutzung ?? null,
    unerlaubteNutzungVon: toIsoDateString(adresse.unerlaubteNutzungVon),
    unerlaubteNutzungBis: toIsoDateString(adresse.unerlaubteNutzungBis),
    tageUnerlaubteNutzung: adresse.tageUnerlaubteNutzung ?? null,
  };
}
