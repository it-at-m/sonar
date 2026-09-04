import type { UnerlaubteNutzung } from "@/types/UnerlaubteNutzung";

import { describe, expect, it } from "vitest";

import {
  hasUnerlaubteNutzung,
  tageUnerlaubteNutzung,
} from "@/util/unerlaubteNutzung";

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

describe("unerlaubteNutzung.ts", () => {
  describe("tageUnerlaubteNutzung", () => {
    it("givenSameDay_thenReturnOne", () => {
      const unerlaubteNutzung = unerlaubteNutzungWith({
        unerlaubteNutzungVon: "2026-01-01",
        unerlaubteNutzungBis: "2026-01-01",
      });

      expect(tageUnerlaubteNutzung(unerlaubteNutzung)).toBe(1);
    });

    it("givenPeriodAcrossDstChange_thenCountBothBoundariesInclusive", () => {
      const unerlaubteNutzung = unerlaubteNutzungWith({
        unerlaubteNutzungVon: "2026-03-28",
        unerlaubteNutzungBis: "2026-03-31",
      });

      expect(tageUnerlaubteNutzung(unerlaubteNutzung)).toBe(4);
    });

    it("givenIncompleteZeitraum_thenReturnUndefined", () => {
      const unerlaubteNutzung = unerlaubteNutzungWith({
        unerlaubteNutzungVon: "2026-01-01",
      });

      expect(tageUnerlaubteNutzung(unerlaubteNutzung)).toBeUndefined();
    });

    it("givenOnlyTage_thenReturnThem", () => {
      const unerlaubteNutzung = unerlaubteNutzungWith({
        tageUnerlaubteNutzung: 12,
      });

      expect(tageUnerlaubteNutzung(unerlaubteNutzung)).toBe(12);
    });

    it("givenZeitraumAndTage_thenPreferTheZeitraum", () => {
      const unerlaubteNutzung = unerlaubteNutzungWith({
        unerlaubteNutzungVon: "2026-01-01",
        unerlaubteNutzungBis: "2026-01-03",
        tageUnerlaubteNutzung: 99,
      });

      expect(tageUnerlaubteNutzung(unerlaubteNutzung)).toBe(3);
    });

    it("givenInvertedZeitraum_thenReturnUndefined", () => {
      const unerlaubteNutzung = unerlaubteNutzungWith({
        unerlaubteNutzungVon: "2026-03-31",
        unerlaubteNutzungBis: "2026-01-01",
      });

      expect(tageUnerlaubteNutzung(unerlaubteNutzung)).toBeUndefined();
    });
  });

  describe("hasUnerlaubteNutzung", () => {
    it("givenNothingEntered_thenReturnFalse", () => {
      expect(hasUnerlaubteNutzung(unerlaubteNutzungWith({}))).toBe(false);
    });

    it("givenOnlyTage_thenReturnTrue", () => {
      const unerlaubteNutzung = unerlaubteNutzungWith({
        tageUnerlaubteNutzung: 1,
      });

      expect(hasUnerlaubteNutzung(unerlaubteNutzung)).toBe(true);
    });

    it("givenOnlyBeginn_thenReturnTrue", () => {
      const unerlaubteNutzung = unerlaubteNutzungWith({
        unerlaubteNutzungVon: "2026-01-01",
      });

      expect(hasUnerlaubteNutzung(unerlaubteNutzung)).toBe(true);
    });
  });
});
