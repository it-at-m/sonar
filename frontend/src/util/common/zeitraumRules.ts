import type { ZeitraumEinheit } from "@/util/common/zeitraum";
import type { ValidationRule } from "@/util/validationRules";

import { countWholePeriods } from "@/util/common/zeitraum";

const ZEITRAUM_ENDE_MESSAGES: Record<ZeitraumEinheit, string> = {
  tag: "Das Ende darf nicht vor dem Beginn liegen.",
  woche: "Der Zeitraum muss volle Wochen umfassen.",
  monat: "Der Zeitraum muss volle Monate umfassen.",
};

export function zeitraumEndeRule(
  beginn: string,
  einheit: ZeitraumEinheit
): ValidationRule {
  return (value: string) =>
    !beginn ||
    !value ||
    countWholePeriods(beginn, value, einheit) !== undefined ||
    ZEITRAUM_ENDE_MESSAGES[einheit];
}
