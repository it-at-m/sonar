import type { AbrechnungPositionForm } from "@/types/AbrechnungPositionForm";

export function createAbrechnungPosition(): AbrechnungPositionForm {
  return {
    id: crypto.randomUUID(),
    beginn: "",
    ende: "",
    laenge: null,
    breite: null,
    flaeche: null,
    haelfte: false,
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
    position.haelfte ||
    position.anteilAnFlaeche !== null
  );
}
