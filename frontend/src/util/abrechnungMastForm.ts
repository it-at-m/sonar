import type { AbrechnungMastForm } from "@/types/AbrechnungMastForm";

export function createAbrechnungMast(): AbrechnungMastForm {
  return {
    id: crypto.randomUUID(),
    beginn: "",
    ende: "",
  };
}
