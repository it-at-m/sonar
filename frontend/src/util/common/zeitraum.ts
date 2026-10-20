import { toIsoDateString } from "@/util/formatter";

export type ZeitraumEinheit = "tag" | "woche" | "monat";

const DAYS_PER_WOCHE = 7;
const MONATE_PRO_JAHR = 12;
const MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

export function lastDayOfPeriods(
  beginn: string,
  periods: number,
  einheit: ZeitraumEinheit
): string {
  const start = parseIsoDate(beginn);
  if (start === undefined || periods < 1) {
    return "";
  }
  switch (einheit) {
    case "tag":
      return toIsoDateString(
        new Date(
          Date.UTC(
            start.getUTCFullYear(),
            start.getUTCMonth(),
            start.getUTCDate() + periods - 1
          )
        )
      );
    case "woche":
      return toIsoDateString(
        new Date(
          Date.UTC(
            start.getUTCFullYear(),
            start.getUTCMonth(),
            start.getUTCDate() + periods * DAYS_PER_WOCHE - 1
          )
        )
      );
    case "monat":
      return toIsoDateString(lastDayOfMonatsPeriods(start, periods));
  }
}

export function countWholePeriods(
  beginn: string,
  ende: string,
  einheit: ZeitraumEinheit
): number | undefined {
  const start = parseIsoDate(beginn);
  const schluss = parseIsoDate(ende);
  if (start === undefined || schluss === undefined) {
    return undefined;
  }
  switch (einheit) {
    case "tag":
      return countWholeTagePeriods(start, schluss);
    case "woche":
      return countWholeWochenPeriods(start, schluss);
    case "monat":
      return countWholeMonatsPeriods(start, schluss);
  }
}

export function snapToLastDayOfPeriod(
  beginn: string,
  ende: string,
  einheit: ZeitraumEinheit
): string {
  const start = parseIsoDate(beginn);
  const gewaehlt = parseIsoDate(ende);
  if (start === undefined || gewaehlt === undefined) {
    return ende;
  }
  switch (einheit) {
    case "tag": {
      const tage = daysBetween(start, gewaehlt) + 1;
      return lastDayOfPeriods(beginn, Math.max(1, tage), einheit);
    }
    case "woche": {
      const tage = daysBetween(start, gewaehlt) + 1;
      return lastDayOfPeriods(
        beginn,
        Math.max(1, Math.ceil(tage / DAYS_PER_WOCHE)),
        einheit
      );
    }
    case "monat": {
      const next =
        monatsCandidates(start, gewaehlt).find(
          (monate) =>
            lastDayOfMonatsPeriods(start, monate).getTime() >=
            gewaehlt.getTime()
        ) ?? 1;
      return lastDayOfPeriods(beginn, next, einheit);
    }
  }
}

function countWholeTagePeriods(start: Date, schluss: Date): number | undefined {
  const tage = daysBetween(start, schluss) + 1;
  return tage >= 1 ? tage : undefined;
}

function countWholeWochenPeriods(
  start: Date,
  schluss: Date
): number | undefined {
  const tage = daysBetween(start, schluss) + 1;
  return tage >= DAYS_PER_WOCHE && tage % DAYS_PER_WOCHE === 0
    ? tage / DAYS_PER_WOCHE
    : undefined;
}

function countWholeMonatsPeriods(
  start: Date,
  schluss: Date
): number | undefined {
  return monatsCandidates(start, schluss).find(
    (monate) =>
      lastDayOfMonatsPeriods(start, monate).getTime() === schluss.getTime()
  );
}

function lastDayOfMonatsPeriods(start: Date, monate: number): Date {
  const jahr = start.getUTCFullYear();
  const monat = start.getUTCMonth();
  const tag = start.getUTCDate();
  const lastDay = lastDayOfMonth(jahr, monat + monate);
  return tag > lastDay
    ? new Date(Date.UTC(jahr, monat + monate, lastDay))
    : new Date(Date.UTC(jahr, monat + monate, tag - 1));
}

function monatsCandidates(start: Date, schluss: Date): number[] {
  const monate =
    (schluss.getUTCFullYear() - start.getUTCFullYear()) * MONATE_PRO_JAHR +
    (schluss.getUTCMonth() - start.getUTCMonth()) +
    (schluss.getUTCDate() >= start.getUTCDate() ? 1 : 0);
  const guess = Math.max(2, monate);
  return [guess - 1, guess, guess + 1];
}

function lastDayOfMonth(jahr: number, monat: number): number {
  return new Date(Date.UTC(jahr, monat + 1, 0)).getUTCDate();
}

function daysBetween(from: Date, to: Date): number {
  return Math.round((to.getTime() - from.getTime()) / MILLIS_PER_DAY);
}

function parseIsoDate(value: string): Date | undefined {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    return undefined;
  }
  const date = new Date(`${value}T00:00:00.000Z`);
  return Number.isNaN(date.getTime()) ? undefined : date;
}
