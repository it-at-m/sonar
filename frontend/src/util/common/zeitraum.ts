import { toIsoDateString } from "@/util/formatter";

export type ZeitraumEinheit = "day" | "week" | "month";

const DAYS_PER_WEEK = 7;
const MONTHS_PER_YEAR = 12;
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
    case "day":
      return toIsoDateString(
        new Date(
          Date.UTC(
            start.getUTCFullYear(),
            start.getUTCMonth(),
            start.getUTCDate() + periods - 1
          )
        )
      );
    case "week":
      return toIsoDateString(
        new Date(
          Date.UTC(
            start.getUTCFullYear(),
            start.getUTCMonth(),
            start.getUTCDate() + periods * DAYS_PER_WEEK - 1
          )
        )
      );
    case "month":
      return toIsoDateString(lastDayOfMonthPeriods(start, periods));
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
    case "day":
      return countWholeDayPeriods(start, schluss);
    case "week":
      return countWholeWeekPeriods(start, schluss);
    case "month":
      return countWholeMonthPeriods(start, schluss);
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
    case "day": {
      const days = daysBetween(start, gewaehlt) + 1;
      return lastDayOfPeriods(beginn, Math.max(1, days), einheit);
    }
    case "week": {
      const days = daysBetween(start, gewaehlt) + 1;
      return lastDayOfPeriods(
        beginn,
        Math.max(1, Math.ceil(days / DAYS_PER_WEEK)),
        einheit
      );
    }
    case "month": {
      const next =
        monthCandidates(start, gewaehlt).find(
          (months) =>
            lastDayOfMonthPeriods(start, months).getTime() >= gewaehlt.getTime()
        ) ?? 1;
      return lastDayOfPeriods(beginn, next, einheit);
    }
  }
}

function countWholeDayPeriods(start: Date, schluss: Date): number | undefined {
  const days = daysBetween(start, schluss) + 1;
  return days >= 1 ? days : undefined;
}

function countWholeWeekPeriods(start: Date, schluss: Date): number | undefined {
  const days = daysBetween(start, schluss) + 1;
  return days >= DAYS_PER_WEEK && days % DAYS_PER_WEEK === 0
    ? days / DAYS_PER_WEEK
    : undefined;
}

function countWholeMonthPeriods(
  start: Date,
  schluss: Date
): number | undefined {
  return monthCandidates(start, schluss).find(
    (months) =>
      lastDayOfMonthPeriods(start, months).getTime() === schluss.getTime()
  );
}

function lastDayOfMonthPeriods(start: Date, months: number): Date {
  const jahr = start.getUTCFullYear();
  const month = start.getUTCMonth();
  const day = start.getUTCDate();
  const lastDay = lastDayOfMonth(jahr, month + months);
  return day > lastDay
    ? new Date(Date.UTC(jahr, month + months, lastDay))
    : new Date(Date.UTC(jahr, month + months, day - 1));
}

function monthCandidates(start: Date, schluss: Date): number[] {
  const months =
    (schluss.getUTCFullYear() - start.getUTCFullYear()) * MONTHS_PER_YEAR +
    (schluss.getUTCMonth() - start.getUTCMonth()) +
    (schluss.getUTCDate() >= start.getUTCDate() ? 1 : 0);
  const guess = Math.max(2, months);
  return [guess - 1, guess, guess + 1];
}

function lastDayOfMonth(jahr: number, month: number): number {
  return new Date(Date.UTC(jahr, month + 1, 0)).getUTCDate();
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
