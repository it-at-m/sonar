import type { AbrechnungPositionForm } from "@/types/abrechnung/AbrechnungPositionForm";

const HUNDREDTHS_PER_UNIT = 100;

export function createAbrechnungPosition(): AbrechnungPositionForm {
  return {
    id: crypto.randomUUID(),
    beginn: "",
    ende: "",
    laenge: null,
    breite: null,
    flaeche: null,
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
    position.anteilAnFlaeche !== null
  );
}

export function hasLaengeUndBreite(position: AbrechnungPositionForm): boolean {
  return position.laenge !== null && position.breite !== null;
}

export function setLaenge(
  position: AbrechnungPositionForm,
  laenge: number | null
): void {
  position.laenge = laenge;
  updateFlaeche(position);
}

export function setBreite(
  position: AbrechnungPositionForm,
  breite: number | null
): void {
  position.breite = breite;
  updateFlaeche(position);
}

function updateFlaeche(position: AbrechnungPositionForm): void {
  const { breite, laenge } = position;
  if (laenge === null || breite === null) {
    return;
  }
  const laengeHundertstel = Math.round(laenge * HUNDREDTHS_PER_UNIT);
  const breiteHundertstel = Math.round(breite * HUNDREDTHS_PER_UNIT);
  position.flaeche =
    Math.round((laengeHundertstel * breiteHundertstel) / HUNDREDTHS_PER_UNIT) /
    HUNDREDTHS_PER_UNIT;
}
