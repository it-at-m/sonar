import type { UnerlaubteNutzung } from "@/types/UnerlaubteNutzung";
import type { ValidationRule } from "@/util/validationRules";

import { endeNotBeforeBeginn } from "@/util/validationRules";

export function unerlaubteNutzungVonRule(
  unerlaubteNutzung: UnerlaubteNutzung
): ValidationRule {
  return (value: string) =>
    !!value ||
    !unerlaubteNutzung.unerlaubteNutzungBis ||
    "Bitte den Beginn des Zeitraums angeben.";
}

export function unerlaubteNutzungBisRule(
  unerlaubteNutzung: UnerlaubteNutzung
): ValidationRule {
  return (value: string) => {
    if (!value) {
      return (
        !unerlaubteNutzung.unerlaubteNutzungVon ||
        "Bitte das Ende des Zeitraums angeben."
      );
    }
    return endeNotBeforeBeginn(unerlaubteNutzung.unerlaubteNutzungVon, value);
  };
}
