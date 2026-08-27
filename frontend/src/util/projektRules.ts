import type { ValidationRule } from "@/util/validationRules";

import { endeNotBeforeBeginn } from "@/util/validationRules";

export function abrechnungEndeRule(abrechnungBeginn: string): ValidationRule {
  return (value: string) => endeNotBeforeBeginn(abrechnungBeginn, value);
}
