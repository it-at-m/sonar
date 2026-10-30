import type { AbrechnungPositionForm } from "@/types/abrechnung/AbrechnungPositionForm";

import { isPossibleBeginn } from "@/util/abrechnung/abrechnungPositionForm";

export function beginnRule(
  vorgaenger: AbrechnungPositionForm,
  position: AbrechnungPositionForm
) {
  return (beginn: string) =>
    !beginn ||
    isPossibleBeginn(vorgaenger, position, beginn) ||
    "Diese und die vorige Position müssen volle Wochen umfassen.";
}

export function laengeRule(position: AbrechnungPositionForm) {
  return (laenge: number | null) =>
    laenge !== null || position.breite === null || "Bitte die Länge angeben.";
}

export function breiteRule(position: AbrechnungPositionForm) {
  return (breite: number | null) =>
    breite !== null || position.laenge === null || "Bitte die Breite angeben.";
}
