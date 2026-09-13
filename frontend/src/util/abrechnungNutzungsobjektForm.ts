import type { AbrechnungNutzungsobjektForm } from "@/types/AbrechnungNutzungsobjektForm";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";
import {
  createAbrechnungPosition,
  isAbrechnungPositionDirty,
} from "@/util/abrechnungPositionForm";
import { isAdresseDirty } from "@/util/adresseForm";
import { hasUnerlaubteNutzung } from "@/util/unerlaubteNutzung";

export function createAbrechnungNutzungsobjekt(): AbrechnungNutzungsobjektForm {
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
    bemerkung: "",
    positionen: [createAbrechnungPosition()],
  };
}

export function isAbrechnungNutzungsobjektDirty(
  nutzungsobjekt: AbrechnungNutzungsobjektForm
): boolean {
  return (
    isAdresseDirty(nutzungsobjekt) ||
    nutzungsobjekt.bemerkung !== "" ||
    hasUnerlaubteNutzung(nutzungsobjekt) ||
    nutzungsobjekt.positionen.length > 1 ||
    nutzungsobjekt.positionen.some(isAbrechnungPositionDirty)
  );
}
