const LOCALE = "de-DE";

export function toFirstLetterUppercase(text: string): string {
  return text ? text[0]?.toUpperCase() + text.slice(1).toLowerCase() : "";
}

export function toDateString(date: Date): string {
  return date ? date.toLocaleDateString(LOCALE) : "";
}

export function toIsoDateString(date: Date | undefined): string {
  return date ? date.toISOString().substring(0, 10) : "";
}

export function toTimeString(date: Date): string {
  return date ? date.toLocaleTimeString(LOCALE) : "";
}

export function toDateAndTimeString(date: Date): string {
  return date ? date.toLocaleString(LOCALE) : "";
}
