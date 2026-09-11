import type { UnerlaubteNutzung } from "@/types/UnerlaubteNutzung";

import { describe, expect, it } from "vitest";

import {
  unerlaubteNutzungBisRule,
  unerlaubteNutzungVonRule,
} from "@/util/unerlaubteNutzungRules";

function unerlaubteNutzungWith(
  overrides: Partial<UnerlaubteNutzung>
): UnerlaubteNutzung {
  return {
    unerlaubteNutzungVon: "",
    unerlaubteNutzungBis: "",
    tageUnerlaubteNutzung: null,
    ...overrides,
  };
}

describe("unerlaubteNutzungRules.ts", () => {
  describe("unerlaubteNutzungBisRule", () => {
    it("givenBeginnWithoutEnde_thenReturnMessage", () => {
      const unerlaubteNutzung = unerlaubteNutzungWith({
        unerlaubteNutzungVon: "2026-01-01",
      });

      expect(unerlaubteNutzungBisRule(unerlaubteNutzung)("")).toBe(
        "Bitte das Ende des Zeitraums angeben."
      );
    });

    it("givenEndeBeforeBeginn_thenReturnMessage", () => {
      const unerlaubteNutzung = unerlaubteNutzungWith({
        unerlaubteNutzungVon: "2026-03-31",
      });

      expect(unerlaubteNutzungBisRule(unerlaubteNutzung)("2026-01-01")).toBe(
        "Das Ende darf nicht vor dem Beginn liegen."
      );
    });

    it("givenEmptyZeitraum_thenReturnTrue", () => {
      expect(unerlaubteNutzungBisRule(unerlaubteNutzungWith({}))("")).toBe(
        true
      );
    });
  });

  describe("unerlaubteNutzungVonRule", () => {
    it("givenEndeWithoutBeginn_thenReturnMessage", () => {
      const unerlaubteNutzung = unerlaubteNutzungWith({
        unerlaubteNutzungBis: "2026-03-31",
      });

      expect(unerlaubteNutzungVonRule(unerlaubteNutzung)("")).toBe(
        "Bitte den Beginn des Zeitraums angeben."
      );
    });
  });
});
