import type { AbrechnungMastForm } from "@/types/abrechnung/AbrechnungMastForm";

export function createAbrechnungMast(): AbrechnungMastForm {
  return {
    id: crypto.randomUUID(),
    beginn: "",
    ende: "",
  };
}
