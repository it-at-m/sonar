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
