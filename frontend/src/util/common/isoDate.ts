export function toIsoDate(value: unknown): string {
  if (typeof value === "string") {
    return value;
  }
  if (!(value instanceof Date) || Number.isNaN(value.getTime())) {
    return "";
  }
  const monat = String(value.getMonth() + 1).padStart(2, "0");
  const tag = String(value.getDate()).padStart(2, "0");
  return `${value.getFullYear()}-${monat}-${tag}`;
}
