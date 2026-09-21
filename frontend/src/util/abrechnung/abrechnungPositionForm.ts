import type { AbrechnungPositionForm } from "@/types/abrechnung/AbrechnungPositionForm";

export function createAbrechnungPosition(): AbrechnungPositionForm {
  return {
    id: crypto.randomUUID(),
    beginn: "",
    ende: "",
    laenge: null,
    breite: null,
    flaeche: null,
    isHaelfte: false,
    anteilAnFlaeche: null,
  };
}

export function isAbrechnungPositionDirty(
  position: AbrechnungPositionForm
): boolean {
  return (
    position.beginn !== "" ||
    position.ende !== "" ||
    position.laenge !== null ||
    position.breite !== null ||
    position.flaeche !== null ||
    position.isHaelfte ||
    position.anteilAnFlaeche !== null
  );
}
