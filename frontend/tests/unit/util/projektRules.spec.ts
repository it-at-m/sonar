import { describe, expect, it } from "vitest";

import { abrechnungEndeRule } from "@/util/projektRules";

describe("projektRules.ts", () => {
  describe("abrechnungEndeRule", () => {
    it("givenEndeAfterBeginn_thenReturnTrue", () => {
      expect(abrechnungEndeRule("2026-01-01")("2026-03-31")).toBe(true);
    });

    it("givenEndeBeforeBeginn_thenReturnMessage", () => {
      expect(abrechnungEndeRule("2026-03-31")("2026-01-01")).toBe(
        "Das Ende darf nicht vor dem Beginn liegen."
      );
    });

    it("givenNoBeginn_thenReturnTrue", () => {
      expect(abrechnungEndeRule("")("2026-01-01")).toBe(true);
    });
  });
});
