import type { ProjektAdresseForm } from "@/types/ProjektAdresseForm";

import { describe, expect, it } from "vitest";

import {
  createProjektAdresse,
  tageUnerlaubteNutzung,
} from "@/util/projektAdresseForm";

function adresseWith(
  overrides: Partial<ProjektAdresseForm>
): ProjektAdresseForm {
  return { ...createProjektAdresse(), ...overrides };
}

describe("projektAdresseForm.ts", () => {
  describe("tageUnerlaubteNutzung", () => {
    it("givenSameDay_thenReturnOne", () => {
      const adresse = adresseWith({
        unerlaubteNutzungVon: "2026-01-01",
        unerlaubteNutzungBis: "2026-01-01",
      });

      expect(tageUnerlaubteNutzung(adresse)).toBe(1);
    });

    it("givenPeriodAcrossDstChange_thenCountBothBoundariesInclusive", () => {
      const adresse = adresseWith({
        unerlaubteNutzungVon: "2026-03-28",
        unerlaubteNutzungBis: "2026-03-31",
      });

      expect(tageUnerlaubteNutzung(adresse)).toBe(4);
    });

    it("givenIncompleteZeitraum_thenReturnUndefined", () => {
      const adresse = adresseWith({ unerlaubteNutzungVon: "2026-01-01" });

      expect(tageUnerlaubteNutzung(adresse)).toBeUndefined();
    });

    it("givenOnlyTage_thenReturnThem", () => {
      const adresse = adresseWith({ tageUnerlaubteNutzung: 12 });

      expect(tageUnerlaubteNutzung(adresse)).toBe(12);
    });

    it("givenZeitraumAndTage_thenPreferTheZeitraum", () => {
      const adresse = adresseWith({
        unerlaubteNutzungVon: "2026-01-01",
        unerlaubteNutzungBis: "2026-01-03",
        tageUnerlaubteNutzung: 99,
      });

      expect(tageUnerlaubteNutzung(adresse)).toBe(3);
    });

    it("givenInvertedZeitraum_thenReturnUndefined", () => {
      const adresse = adresseWith({
        unerlaubteNutzungVon: "2026-03-31",
        unerlaubteNutzungBis: "2026-01-01",
      });

      expect(tageUnerlaubteNutzung(adresse)).toBeUndefined();
    });
  });
});
