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
    uebernahme: null,
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
    masten: [],
  };
}

export function isAbrechnungNutzungsobjektDirty(
  nutzungsobjekt: AbrechnungNutzungsobjektForm
): boolean {
  return (
    nutzungsobjekt.uebernahme !== null ||
    isAdresseDirty(nutzungsobjekt) ||
    nutzungsobjekt.bemerkung !== "" ||
    nutzungsobjekt.aufschlag50prozent ||
    hasUnerlaubteNutzung(nutzungsobjekt) ||
    nutzungsobjekt.positionen.length !== 1 ||
    nutzungsobjekt.positionen.some(isAbrechnungPositionDirty) ||
    nutzungsobjekt.masten.length > 0
  );
}

export function uebernommenesNutzungsobjektId(
  nutzungsobjekt: AbrechnungNutzungsobjektForm
): string | undefined {
  return nutzungsobjekt.uebernahme?.quelle === "NUTZUNGSOBJEKT"
    ? nutzungsobjekt.uebernahme.nutzungsobjektId
    : undefined;
}
