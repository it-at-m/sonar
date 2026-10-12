export function toFirstLetterUppercase(text: string): string {
  return text ? text[0]?.toUpperCase() + text.slice(1).toLowerCase() : "";
}

const DECIMAL_FORMAT = new Intl.NumberFormat("de-DE", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const EURO_FORMAT = new Intl.NumberFormat("de-DE", {
  style: "currency",
  currency: "EUR",
});

export function toDateString(date: Date | undefined): string {
  return date ? date.toLocaleDateString() : "";
}

export function toDecimalString(value: number | null | undefined): string {
  return value === null || value === undefined
    ? ""
    : DECIMAL_FORMAT.format(value);
}

export function toEuroString(value: number | null | undefined): string {
  return value === null || value === undefined ? "" : EURO_FORMAT.format(value);
}

export function toIsoDateString(date: Date | undefined): string {
  return date ? date.toISOString().substring(0, 10) : "";
}

export function toTimeString(date: Date): string {
  return date ? date.toLocaleTimeString() : "";
}

export function toDateAndTimeString(date: Date): string {
  return date ? date.toLocaleString() : "";
}
