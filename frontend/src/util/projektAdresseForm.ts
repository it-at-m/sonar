import type { ProjektAdresseForm } from "@/types/ProjektAdresseForm";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";
import { isAdresseDirty } from "@/util/adresseForm";
import { hasUnerlaubteNutzung } from "@/util/unerlaubteNutzung";

export function createProjektAdresse(): ProjektAdresseForm {
  return {
    id: crypto.randomUUID(),
    art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
    adresse: "",
    hausnummerVon: "",
    hausnummerBis: "",
    flurstueck: "",
    gemarkung: "",
    nutzung: null,
    unerlaubteNutzungVon: "",
    unerlaubteNutzungBis: "",
    tageUnerlaubteNutzung: null,
    anzahlMahnungen: 0,
    sondernutzungErlaubt: false,
  };
}

export function isProjektAdresseDirty(adresse: ProjektAdresseForm): boolean {
  return (
    isAdresseDirty(adresse) ||
    hasUnerlaubteNutzung(adresse) ||
    adresse.anzahlMahnungen !== 0 ||
    adresse.sondernutzungErlaubt
  );
}
