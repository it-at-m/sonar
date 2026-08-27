import type { ProjektAdresseForm } from "@/types/ProjektAdresseForm";
import type { ValidationRule } from "@/util/validationRules";

import { endeNotBeforeBeginn } from "@/util/validationRules";

export function unerlaubteNutzungVonRule(
  adresse: ProjektAdresseForm
): ValidationRule {
  return (value: string) =>
    !!value ||
    !adresse.unerlaubteNutzungBis ||
    "Bitte den Beginn des Zeitraums angeben.";
}

export function unerlaubteNutzungBisRule(
  adresse: ProjektAdresseForm
): ValidationRule {
  return (value: string) => {
    if (!value) {
      return (
        !adresse.unerlaubteNutzungVon || "Bitte das Ende des Zeitraums angeben."
      );
    }
    return endeNotBeforeBeginn(adresse.unerlaubteNutzungVon, value);
  };
}
