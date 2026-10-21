import { describe, expect, it } from "vitest";

import { greaterThanZeroRule, requiredRule } from "@/util/validationRules";

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

  describe("greaterThanZeroRule", () => {
    it("givenZero_thenReturnMessage", () => {
      expect(greaterThanZeroRule(0)).toBe("Der Wert muss größer als 0 sein.");
    });

    it("givenPositiveValue_thenReturnTrue", () => {
      expect(greaterThanZeroRule(0.01)).toBe(true);
    });

    it("givenNull_thenReturnTrue", () => {
      expect(greaterThanZeroRule(null)).toBe(true);
    });
  });
});
