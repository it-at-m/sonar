import type { AbrechnungMastForm } from "@/types/abrechnung/AbrechnungMastForm";

export function createAbrechnungMast(): AbrechnungMastForm {
  return {
    key: crypto.randomUUID(),
    id: null,
    beginn: "",
    ende: "",
  };
}
