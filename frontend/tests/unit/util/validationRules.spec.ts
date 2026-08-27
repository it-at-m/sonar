import { describe, expect, it } from "vitest";

import { endeNotBeforeBeginn, requiredRule } from "@/util/validationRules";

describe("validationRules.ts", () => {
  describe("requiredRule", () => {
    it("givenEmptyString_thenReturnMessage", () => {
      expect(requiredRule("")).toBe("Pflichtfeld");
    });

    it("givenBlankString_thenReturnMessage", () => {
      expect(requiredRule("   ")).toBe("Pflichtfeld");
    });

    it("givenNull_thenReturnMessage", () => {
      expect(requiredRule(null)).toBe("Pflichtfeld");
    });

    it("givenZero_thenReturnTrue", () => {
      expect(requiredRule(0)).toBe(true);
    });
  });

  describe("endeNotBeforeBeginn", () => {
    it("givenEndeAfterBeginn_thenReturnTrue", () => {
      expect(endeNotBeforeBeginn("2026-01-01", "2026-03-31")).toBe(true);
    });

    it("givenSameDay_thenReturnTrue", () => {
      expect(endeNotBeforeBeginn("2026-01-01", "2026-01-01")).toBe(true);
    });

    it("givenEndeBeforeBeginn_thenReturnMessage", () => {
      expect(endeNotBeforeBeginn("2026-03-31", "2026-01-01")).toBe(
        "Das Ende darf nicht vor dem Beginn liegen."
      );
    });

    it("givenNoBeginn_thenReturnTrue", () => {
      expect(endeNotBeforeBeginn("", "2026-01-01")).toBe(true);
    });

    it("givenNoEnde_thenReturnTrue", () => {
      expect(endeNotBeforeBeginn("2026-01-01", "")).toBe(true);
    });
  });
});
