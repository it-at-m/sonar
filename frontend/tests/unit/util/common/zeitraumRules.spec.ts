import { describe, expect, it } from "vitest";

import { zeitraumEndeRule } from "@/util/common/zeitraumRules";

describe("zeitraumRules.ts", () => {
  describe("zeitraumEndeRule", () => {
    it("givenAWholeWoche_thenReturnTrue", () => {
      expect(zeitraumEndeRule("2026-01-05", "woche")("2026-01-18")).toBe(true);
    });

    it("givenAPartialWoche_thenReturnMessage", () => {
      expect(zeitraumEndeRule("2026-01-05", "woche")("2026-01-15")).toBe(
        "Der Zeitraum muss volle Wochen umfassen."
      );
    });

    it("givenTheEinheitTagAndAnyLaterEnde_thenReturnTrue", () => {
      expect(zeitraumEndeRule("2026-01-05", "tag")("2026-01-15")).toBe(true);
    });

    it("givenTheEinheitTagAndAnEndeBeforeTheBeginn_thenReturnMessage", () => {
      expect(zeitraumEndeRule("2026-01-05", "tag")("2026-01-04")).toBe(
        "Das Ende darf nicht vor dem Beginn liegen."
      );
    });

    it("givenAPartialMonat_thenReturnMessage", () => {
      expect(zeitraumEndeRule("2026-01-15", "monat")("2026-02-20")).toBe(
        "Der Zeitraum muss volle Monate umfassen."
      );
    });

    it("givenNoBeginn_thenReturnTrue", () => {
      expect(zeitraumEndeRule("", "woche")("2026-01-15")).toBe(true);
    });

    it("givenNoEnde_thenReturnTrue", () => {
      expect(zeitraumEndeRule("2026-01-05", "woche")("")).toBe(true);
    });
  });
});
