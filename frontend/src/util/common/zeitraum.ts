export type ZeitraumEinheit = "woche" | "monat";

const TAGE_PRO_WOCHE = 7;
const MONATE_PRO_JAHR = 12;
const MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

export function zeitraumEnde(
  beginn: string,
  perioden: number,
  einheit: ZeitraumEinheit
): string {
  const start = parseIsoDate(beginn);
  if (start === undefined || perioden < 1) {
    return "";
  }
  return toIsoDate(
    einheit === "woche"
      ? wochenEnde(start, perioden)
      : monatsEnde(start, perioden)
  );
}

export function zeitraumPerioden(
  beginn: string,
  ende: string,
  einheit: ZeitraumEinheit
): number | undefined {
  const start = parseIsoDate(beginn);
  const schluss = parseIsoDate(ende);
  if (start === undefined || schluss === undefined) {
    return undefined;
  }
  return einheit === "woche"
    ? wochenPerioden(start, schluss)
    : monatsPerioden(start, schluss);
}

export function nearestZeitraumEnde(
  beginn: string,
  ende: string,
  einheit: ZeitraumEinheit
): string {
  const start = parseIsoDate(beginn);
  const gewaehlt = parseIsoDate(ende);
  if (start === undefined || gewaehlt === undefined) {
    return ende;
  }
  return zeitraumEnde(
    beginn,
    nearestPerioden(start, gewaehlt, einheit),
    einheit
  );
}

export function zeitraumEndeMessage(einheit: ZeitraumEinheit): string {
  return einheit === "woche"
    ? "Der Zeitraum muss volle Wochen umfassen."
    : "Der Zeitraum muss volle Monate umfassen.";
}

function wochenEnde(start: Date, wochen: number): Date {
  return addDays(start, wochen * TAGE_PRO_WOCHE - 1);
}

function wochenPerioden(start: Date, schluss: Date): number | undefined {
  const tage = daysBetween(start, schluss) + 1;
  return tage >= TAGE_PRO_WOCHE && tage % TAGE_PRO_WOCHE === 0
    ? tage / TAGE_PRO_WOCHE
    : undefined;
}

function monatsEnde(start: Date, monate: number): Date {
  const jahr = start.getUTCFullYear();
  const monat = start.getUTCMonth();
  const tag = start.getUTCDate();
  const letzterTag = daysInMonth(jahr, monat + monate);
  return tag > letzterTag
    ? new Date(Date.UTC(jahr, monat + monate, letzterTag))
    : addDays(new Date(Date.UTC(jahr, monat + monate, tag)), -1);
}

function monatsPerioden(start: Date, schluss: Date): number | undefined {
  return monatsCandidates(start, schluss).find(
    (monate) => monatsEnde(start, monate).getTime() === schluss.getTime()
  );
}

function monatsCandidates(start: Date, schluss: Date): number[] {
  const monate =
    (schluss.getUTCFullYear() - start.getUTCFullYear()) * MONATE_PRO_JAHR +
    (schluss.getUTCMonth() - start.getUTCMonth()) +
    (schluss.getUTCDate() >= start.getUTCDate() ? 1 : 0);
  const guess = Math.max(2, monate);
  return [guess - 1, guess, guess + 1];
}

function nearestPerioden(
  start: Date,
  gewaehlt: Date,
  einheit: ZeitraumEinheit
): number {
  if (einheit === "woche") {
    const tage = daysBetween(start, gewaehlt) + 1;
    return Math.max(1, Math.round(tage / TAGE_PRO_WOCHE));
  }
  return monatsCandidates(start, gewaehlt).reduce((nearest, monate) =>
    abstand(start, gewaehlt, monate) < abstand(start, gewaehlt, nearest)
      ? monate
      : nearest
  );
}

function abstand(start: Date, gewaehlt: Date, monate: number): number {
  return Math.abs(monatsEnde(start, monate).getTime() - gewaehlt.getTime());
}

function daysInMonth(jahr: number, monat: number): number {
  return new Date(Date.UTC(jahr, monat + 1, 0)).getUTCDate();
}

function daysBetween(from: Date, to: Date): number {
  return Math.round((to.getTime() - from.getTime()) / MILLIS_PER_DAY);
}

function addDays(date: Date, tage: number): Date {
  return new Date(date.getTime() + tage * MILLIS_PER_DAY);
}

function parseIsoDate(value: string): Date | undefined {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return undefined;
  }
  const date = new Date(`${value}T00:00:00.000Z`);
  return Number.isNaN(date.getTime()) ? undefined : date;
}

function toIsoDate(date: Date): string {
  return date.toISOString().slice(0, 10);
}
