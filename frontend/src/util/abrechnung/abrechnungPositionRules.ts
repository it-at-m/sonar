import type { AbrechnungPositionForm } from "@/types/abrechnung/AbrechnungPositionForm";

export function laengeRule(position: AbrechnungPositionForm) {
  return (laenge: number | null) =>
    laenge !== null || position.breite === null || "Bitte die Länge angeben.";
}

export function breiteRule(position: AbrechnungPositionForm) {
  return (breite: number | null) =>
    breite !== null || position.laenge === null || "Bitte die Breite angeben.";
}
