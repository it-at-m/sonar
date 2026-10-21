import { describe, expect, it } from "vitest";

import { zeitraumEndeRule } from "@/util/common/zeitraumRules";

describe("zeitraumRules.ts", () => {
  describe("zeitraumEndeRule", () => {
    it("givenAWholeWeek_thenReturnTrue", () => {
      expect(zeitraumEndeRule("2026-01-05", "week")("2026-01-18")).toBe(true);
    });

    it("givenAPartialWeek_thenReturnMessage", () => {
      expect(zeitraumEndeRule("2026-01-05", "week")("2026-01-15")).toBe(
        "Der Zeitraum muss volle Wochen umfassen."
      );
    });

    it("givenTheEinheitDayAndAnyLaterEnde_thenReturnTrue", () => {
      expect(zeitraumEndeRule("2026-01-05", "day")("2026-01-15")).toBe(true);
    });

    it("givenTheEinheitDayAndAnEndeBeforeTheBeginn_thenReturnMessage", () => {
      expect(zeitraumEndeRule("2026-01-05", "day")("2026-01-04")).toBe(
        "Das Ende darf nicht vor dem Beginn liegen."
      );
    });

    it("givenAPartialMonth_thenReturnMessage", () => {
      expect(zeitraumEndeRule("2026-01-15", "month")("2026-02-20")).toBe(
        "Der Zeitraum muss volle Monate umfassen."
      );
    });

    it("givenNoBeginn_thenReturnTrue", () => {
      expect(zeitraumEndeRule("", "week")("2026-01-15")).toBe(true);
    });

    it("givenNoEnde_thenReturnTrue", () => {
      expect(zeitraumEndeRule("2026-01-05", "week")("")).toBe(true);
    });
  });
});
