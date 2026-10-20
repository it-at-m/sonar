import type { UnerlaubteNutzung } from "@/types/common/UnerlaubteNutzung";
import type { ValidationRule } from "@/util/validationRules";

export function unerlaubteNutzungVonRule(
  unerlaubteNutzung: UnerlaubteNutzung
): ValidationRule {
  return (value: string) => {
    if (!value && unerlaubteNutzung.unerlaubteNutzungBis) {
      return "Bitte den Beginn des Zeitraums angeben.";
    }
    return true;
  };
}

export function unerlaubteNutzungBisRule(
  unerlaubteNutzung: UnerlaubteNutzung
): ValidationRule {
  return (value: string) => {
    if (!value && unerlaubteNutzung.unerlaubteNutzungVon) {
      return "Bitte das Ende des Zeitraums angeben.";
    }
    return true;
  };
}
