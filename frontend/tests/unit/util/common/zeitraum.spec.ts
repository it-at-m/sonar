import { describe, expect, it } from "vitest";

import {
  countWholePeriods,
  dayAfter,
  lastDayOfPeriods,
  snapToLastDayOfPeriod,
} from "@/util/common/zeitraum";

describe("zeitraum.ts", () => {
  describe("lastDayOfPeriods", () => {
    it("givenAMondayAndOneWeek_thenEndOnTheFollowingSunday", () => {
      expect(lastDayOfPeriods("2026-01-05", 1, "week")).toBe("2026-01-11");
    });

    it("givenAWednesdayAndThreeWeeks_thenEndOnTheThirdTuesday", () => {
      expect(lastDayOfPeriods("2026-01-07", 3, "week")).toBe("2026-01-27");
    });

    it("givenTheEinheitDayAndOnePeriod_thenEndOnTheBeginn", () => {
      expect(lastDayOfPeriods("2026-01-05", 1, "day")).toBe("2026-01-05");
    });

    it("givenTheEinheitDayAndSeveralPeriods_thenEndOnTheLastOfThem", () => {
      expect(lastDayOfPeriods("2026-01-05", 11, "day")).toBe("2026-01-15");
    });

    it("givenMidMonthAndOneMonth_thenEndTheDayBeforeTheSameDayNextMonth", () => {
      expect(lastDayOfPeriods("2026-01-15", 1, "month")).toBe("2026-02-14");
    });

    it("givenTheFirstAndOneMonth_thenEndOnTheLastOfThatMonth", () => {
      expect(lastDayOfPeriods("2026-01-01", 1, "month")).toBe("2026-01-31");
    });

    it("givenADayTheNextMonthLacks_thenEndOnItsLastDay", () => {
      expect(lastDayOfPeriods("2026-01-31", 1, "month")).toBe("2026-02-28");
    });

    it("givenALeapDayAndTwelveMonths_thenEndOnTheLastOfFebruary", () => {
      expect(lastDayOfPeriods("2024-02-29", 12, "month")).toBe("2025-02-28");
    });

    it("givenMonthsAcrossTheTurnOfTheYear_thenCountOn", () => {
      expect(lastDayOfPeriods("2025-11-10", 4, "month")).toBe("2026-03-09");
    });

    it("givenNoPeriod_thenReturnEmpty", () => {
      expect(lastDayOfPeriods("2026-01-05", 0, "week")).toBe("");
    });

    it("givenNoBeginn_thenReturnEmpty", () => {
      expect(lastDayOfPeriods("", 1, "week")).toBe("");
    });
  });
  describe("dayAfter", () => {
    it("givenTheLastOfAMonth_thenReturnTheFirstOfTheNext", () => {
      expect(dayAfter("2026-01-31")).toBe("2026-02-01");
    });

    it("givenNoDate_thenReturnEmpty", () => {
      expect(dayAfter("")).toBe("");
    });
  });

  describe("countWholePeriods", () => {
    it("givenTwoWholeWeeks_thenCountThem", () => {
      expect(countWholePeriods("2026-01-05", "2026-01-18", "week")).toBe(2);
    });

    it("givenAPartialWeek_thenReturnUndefined", () => {
      expect(
        countWholePeriods("2026-01-05", "2026-01-15", "week")
      ).toBeUndefined();
    });

    it("givenASingleDay_thenReturnUndefined", () => {
      expect(
        countWholePeriods("2026-01-05", "2026-01-05", "week")
      ).toBeUndefined();
    });

    it("givenAnEndeBeforeTheBeginn_thenReturnUndefined", () => {
      expect(
        countWholePeriods("2026-01-19", "2026-01-11", "week")
      ).toBeUndefined();
    });

    it("givenTheEinheitDayAndAnyLaterEnde_thenCountTheDays", () => {
      expect(countWholePeriods("2026-01-05", "2026-01-15", "day")).toBe(11);
    });

    it("givenTheEinheitDayAndTheSameDay_thenCountOnePeriod", () => {
      expect(countWholePeriods("2026-01-05", "2026-01-05", "day")).toBe(1);
    });

    it("givenTheEinheitDayAndAnEndeBeforeTheBeginn_thenReturnUndefined", () => {
      expect(
        countWholePeriods("2026-01-05", "2026-01-04", "day")
      ).toBeUndefined();
    });

    it("givenAMonthEndingOnAShortenedDay_thenCountIt", () => {
      expect(countWholePeriods("2026-01-31", "2026-02-28", "month")).toBe(1);
    });

    it("givenTwoMonthsFromADayFebruaryLacks_thenCountThem", () => {
      expect(countWholePeriods("2026-01-31", "2026-03-30", "month")).toBe(2);
    });

    it("givenAPartialMonth_thenReturnUndefined", () => {
      expect(
        countWholePeriods("2026-01-15", "2026-02-20", "month")
      ).toBeUndefined();
    });

    it("givenAWholeWeekThatIsNoWholeMonth_thenReturnUndefined", () => {
      expect(
        countWholePeriods("2026-01-05", "2026-01-11", "month")
      ).toBeUndefined();
    });
  });

  describe("snapToLastDayOfPeriod", () => {
    it("givenADateLateInAWeek_thenTakeTheEndeOfThatWeek", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2026-01-15", "week")).toBe(
        "2026-01-18"
      );
    });

    // Rounding up, so an Ende is never pulled back before the date the user picked.
    it("givenADateEarlyInAWeek_thenStillTakeTheEndeOfThatWeek", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2026-01-13", "week")).toBe(
        "2026-01-18"
      );
    });

    it("givenTheDayAfterAWholeWeek_thenTakeTheEndeOfTheNextWeek", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2026-01-12", "week")).toBe(
        "2026-01-18"
      );
    });

    it("givenAWholeWeek_thenKeepIt", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2026-01-25", "week")).toBe(
        "2026-01-25"
      );
    });

    it("givenADateBeforeTheBeginn_thenTakeTheFirstWeek", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2025-12-20", "week")).toBe(
        "2026-01-11"
      );
    });

    it("givenTheEinheitDay_thenKeepEveryDateFromTheBeginnOn", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2026-01-15", "day")).toBe(
        "2026-01-15"
      );
    });

    it("givenTheEinheitDayAndADateBeforeTheBeginn_thenTakeTheBeginn", () => {
      expect(snapToLastDayOfPeriod("2026-01-05", "2025-12-20", "day")).toBe(
        "2026-01-05"
      );
    });

    it("givenADateShortlyAfterAWholeMonth_thenTakeTheEndeOfTheNextMonth", () => {
      expect(snapToLastDayOfPeriod("2026-01-15", "2026-02-20", "month")).toBe(
        "2026-03-14"
      );
    });

    it("givenADateShortlyBeforeAWholeMonth_thenTakeThatEnde", () => {
      expect(snapToLastDayOfPeriod("2026-01-15", "2026-03-05", "month")).toBe(
        "2026-03-14"
      );
    });

    it("givenAWholeMonth_thenKeepIt", () => {
      expect(snapToLastDayOfPeriod("2026-01-15", "2026-02-14", "month")).toBe(
        "2026-02-14"
      );
    });

    it("givenAMonthEndeOnAShortenedDay_thenKeepIt", () => {
      expect(snapToLastDayOfPeriod("2026-01-31", "2026-02-28", "month")).toBe(
        "2026-02-28"
      );
    });

    it("givenADateBeforeTheBeginn_thenTakeTheFirstMonth", () => {
      expect(snapToLastDayOfPeriod("2026-01-15", "2025-11-01", "month")).toBe(
        "2026-02-14"
      );
    });

    it("givenNoBeginn_thenKeepTheDate", () => {
      expect(snapToLastDayOfPeriod("", "2026-01-15", "week")).toBe(
        "2026-01-15"
      );
    });
  });
});
