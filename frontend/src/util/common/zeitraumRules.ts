import type { ZeitraumEinheit } from "@/util/common/zeitraum";
import type { ValidationRule } from "@/util/validationRules";

import { zeitraumEndeMessage, zeitraumPerioden } from "@/util/common/zeitraum";

export function zeitraumEndeRule(
  beginn: string,
  einheit: ZeitraumEinheit
): ValidationRule {
  return (value: string) =>
    !beginn ||
    !value ||
    zeitraumPerioden(beginn, value, einheit) !== undefined ||
    zeitraumEndeMessage(einheit);
}
