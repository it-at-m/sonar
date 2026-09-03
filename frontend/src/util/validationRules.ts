export type ValidationRule = (value: string) => boolean | string;

export function requiredRule(value: unknown): boolean | string {
  const filled =
    typeof value === "string"
      ? value.trim() !== ""
      : value !== null && value !== undefined;
  return filled || "Pflichtfeld";
}

export function endeNotBeforeBeginn(
  beginn: string,
  ende: string
): boolean | string {
  return (
    !beginn ||
    !ende ||
    ende >= beginn ||
    "Das Ende darf nicht vor dem Beginn liegen."
  );
}

export function greaterThanZeroRule(value: unknown): boolean | string {
  return (
    isEmpty(value) || Number(value) > 0 || "Der Wert muss größer als 0 sein."
  );
}

export function notNegativeRule(value: unknown): boolean | string {
  return (
    isEmpty(value) || Number(value) >= 0 || "Der Wert darf nicht negativ sein."
  );
}

function isEmpty(value: unknown): boolean {
  return value === null || value === undefined || value === "";
}
