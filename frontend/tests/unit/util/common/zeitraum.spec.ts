import { describe, expect, it } from "vitest";

import {
  countWholePeriods,
  lastDayOfPeriods,
  snapToLastDayOfPeriod,
} from "@/util/common/zeitraum";

describe("zeitraum.ts", () => {
  describe("lastDayOfPeriods", () => {
    it("givenAMondayAndOneWoche_thenEndOnTheFollowingSunday", () => {
      expect(lastDayOfPeriods("2026-01-05", 1, "woche")).toBe("2026-01-11");
    });

    it("givenAWednesdayAndThreeWochen_thenEndOnTheThirdTuesday", () => {
      expect(lastDayOfPeriods("2026-01-07", 3, "woche")).toBe("2026-01-27");
    });

    it("givenTheEinheitTagAndOnePeriod_thenEndOnTheBeginn", () => {
      expect(lastDayOfPeriods("2026-01-05", 1, "tag")).toBe("2026-01-05");
    });

    it("givenTheEinheitTagAndSeveralPeriods_thenEndOnTheLastOfThem", () => {
      expect(lastDayOfPeriods("2026-01-05", 11, "tag")).toBe("2026-01-15");
    });

    it("givenMidMonthAndOneMonat_thenEndTheDayBeforeTheSameDayNextMonth", () => {
      expect(lastDayOfPeriods("2026-01-15", 1, "monat")).toBe("2026-02-14");
    });

    it("givenTheFirstAndOneMonat_thenEndOnTheLastOfThatMonth", () => {
      expect(lastDayOfPeriods("2026-01-01", 1, "monat")).toBe("2026-01-31");
    });

    it("givenADayTheNextMonthLacks_thenEndOnItsLastDay", () => {
      expect(lastDayOfPeriods("2026-01-31", 1, "monat")).toBe("2026-02-28");
    });

    it("givenALeapDayAndTwelveMonate_thenEndOnTheLastOfFebruary", () => {
      expect(lastDayOfPeriods("2024-02-29", 12, "monat")).toBe("2025-02-28");
    });

    it("givenMonateAcrossTheTurnOfTheYear_thenCountOn", () => {
      expect(lastDayOfPeriods("2025-11-10", 4, "monat")).toBe("2026-03-09");
    });

    it("givenNoPeriod_thenReturnEmpty", () => {
      expect(lastDayOfPeriods("2026-01-05", 0, "woche")).toBe("");
    });

    it("givenNoBeginn_thenReturnEmpty", () => {
      expect(lastDayOfPeriods("", 1, "woche")).toBe("");
    });
  });

  describe("countWholePeriods", () => {
    it("givenTwoWholeWochen_thenCountThem", () => {
      expect(countWholePeriods("2026-01-05", "2026-01-18", "woche")).toBe(2);
    });

    it("givenAPartialWoche_thenReturnUndefined", () => {
      expect(
        countWholePeriods("2026-01-05", "2026-01-15", "woche")
      ).toBeUndefined();
    });

    it("givenASingleDay_thenReturnUndefined", () => {
      expect(
        countWholePeriods("2026-01-05", "2026-01-05", "woche")
      ).toBeUndefined();
    });

    it("givenAnEndeBeforeTheBeginn_thenReturnUndefined", () => {
      expect(
        countWholePeriods("2026-01-19", "2026-01-11", "woche")
      ).toBeUndefined();
    });

    it("givenTheEinheitTagAndAnyLaterEnde_thenCountTheDays", () => {
      expect(countWholePeriods("2026-01-05", "2026-01-15", "tag")).toBe(11);
    });

    it("givenTheEinheitTagAndTheSameDay_thenCountOnePeriod", () => {
      expect(countWholePeriods("2026-01-05", "2026-01-05", "tag")).toBe(1);
    });

    it("givenTheEinheitTagAndAnEndeBeforeTheBeginn_thenReturnUndefined", () => {
      expect(
        countWholePeriods("2026-01-05", "2026-01-04", "tag")
      ).toBeUndefined();
    });

    it("givenAMonatEndingOnAShortenedDay_thenCountIt", () => {
      expect(countWholePeriods("2026-01-31", "2026-02-28", "monat")).toBe(1);
    });

    it("givenTwoMonateFromADayFebruaryLacks_thenCountThem", () => {
      expect(countWholePeriods("2026-01-31", "2026-03-30", "monat")).toBe(2);
    });

    it("givenAPartialMonat_thenReturnUndefined", () => {
      expect(
        countWholePeriods("2026-01-15", "2026-02-20", "monat")
      ).toBeUndefined();
    });

    it("givenAWholeWocheThatIsNoWholeMonat_thenReturnUndefined", () => {
      expect(
        countWholePeriods("2026-01-05", "2026-01-11", "monat")
      ).toBeUndefined();
    });
  });

  describe("snapToLastDayOfPeriod", () => {
    it("givenADateLateInAWoche_thenTakeTheEndeOfThatWoche", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2026-01-15", "woche")).toBe(
        "2026-01-18"
      );
    });

    // Rounding up, so an Ende is never pulled back before the date the user picked.
    it("givenADateEarlyInAWoche_thenStillTakeTheEndeOfThatWoche", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2026-01-13", "woche")).toBe(
        "2026-01-18"
      );
    });

    it("givenTheDayAfterAWholeWoche_thenTakeTheEndeOfTheNextWoche", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2026-01-12", "woche")).toBe(
        "2026-01-18"
      );
    });

    it("givenAWholeWoche_thenKeepIt", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2026-01-25", "woche")).toBe(
        "2026-01-25"
      );
    });

    it("givenADateBeforeTheBeginn_thenTakeTheFirstWoche", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2025-12-20", "woche")).toBe(
        "2026-01-11"
      );
    });

    it("givenTheEinheitTag_thenKeepEveryDateFromTheBeginnOn", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2026-01-15", "tag")).toBe(
        "2026-01-15"
      );
    });

    it("givenTheEinheitTagAndADateBeforeTheBeginn_thenTakeTheBeginn", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2025-12-20", "tag")).toBe(
        "2026-01-05"
      );
    });

    it("givenADateShortlyAfterAWholeMonat_thenTakeTheEndeOfTheNextMonat", () => {
      expect(snapToLastDayOfPeriod("2026-01-15", "2026-02-20", "monat")).toBe(
        "2026-03-14"
      );
    });

    it("givenADateShortlyBeforeAWholeMonat_thenTakeThatEnde", () => {
      expect(snapToLastDayOfPeriod("2026-01-15", "2026-03-05", "monat")).toBe(
        "2026-03-14"
      );
    });

    it("givenAWholeMonat_thenKeepIt", () => {
      expect(snapToLastDayOfPeriod("2026-01-15", "2026-02-14", "monat")).toBe(
        "2026-02-14"
      );
    });

    it("givenAMonatEndeOnAShortenedDay_thenKeepIt", () => {
      expect(snapToLastDayOfPeriod("2026-01-31", "2026-02-28", "monat")).toBe(
        "2026-02-28"
      );
    });

    it("givenADateBeforeTheBeginn_thenTakeTheFirstMonat", () => {
      expect(snapToLastDayOfPeriod("2026-01-15", "2025-11-01", "monat")).toBe(
        "2026-02-14"
      );
    });

    it("givenNoBeginn_thenKeepTheDate", () => {
      expect(snapToLastDayOfPeriod("", "2026-01-15", "woche")).toBe(
        "2026-01-15"
      );
    });
  });
});
