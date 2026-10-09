import type { AbrechnungNutzungsobjektForm } from "@/types/abrechnung/AbrechnungNutzungsobjektForm";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";
import {
  createAbrechnungPosition,
  isAbrechnungPositionDirty,
} from "@/util/abrechnung/abrechnungPositionForm";
import { isAdresseDirty } from "@/util/common/adresseForm";
import { hasUnerlaubteNutzung } from "@/util/common/unerlaubteNutzungForm";

export function createAbrechnungNutzungsobjekt(): AbrechnungNutzungsobjektForm {
  return {
    id: crypto.randomUUID(),
    art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
    adresse: "",
    hausnummerVon: "",
    hausnummerBis: "",
    flurstueck: "",
    gemarkung: "",
    nutzungen: [],
    nutzungSonstiges: "",
    unerlaubteNutzungVon: "",
    unerlaubteNutzungBis: "",
    tageUnerlaubteNutzung: null,
    bemerkung: "",
    aufschlag50prozent: false,
    positionen: [createAbrechnungPosition()],
  };
}

export function isAbrechnungNutzungsobjektDirty(
  nutzungsobjekt: AbrechnungNutzungsobjektForm
): boolean {
  return (
    isAdresseDirty(nutzungsobjekt) ||
    nutzungsobjekt.bemerkung !== "" ||
    nutzungsobjekt.aufschlag50prozent ||
    hasUnerlaubteNutzung(nutzungsobjekt) ||
    nutzungsobjekt.positionen.length > 1 ||
    nutzungsobjekt.positionen.some(isAbrechnungPositionDirty)
  );
}
