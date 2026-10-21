import { describe, expect, it } from "vitest";

import { toIsoDate } from "@/util/common/isoDate";

describe("isoDate.ts", () => {
  describe("toIsoDate", () => {
    it("givenAnIsoDate_thenHandItBack", () => {
      expect(toIsoDate("2026-01-05")).toBe("2026-01-05");
    });

    it("givenADate_thenReadItsDay", () => {
      expect(toIsoDate(new Date(2026, 0, 5))).toBe("2026-01-05");
    });

    it("givenATimeAfterMidnight_thenReadTheLocalDay", () => {
      expect(toIsoDate(new Date(2026, 0, 1, 1, 30))).toBe("2026-01-01");
    });

    it("givenADayAndMonthBelowTen_thenPadThem", () => {
      expect(toIsoDate(new Date(2026, 8, 7))).toBe("2026-09-07");
    });

    it("givenNull_thenReturnEmpty", () => {
      expect(toIsoDate(null)).toBe("");
    });

    it("givenUndefined_thenReturnEmpty", () => {
      expect(toIsoDate(undefined)).toBe("");
    });

    it("givenAnInvalidDate_thenReturnEmpty", () => {
      expect(toIsoDate(new Date("kein Datum"))).toBe("");
    });
  });
});
