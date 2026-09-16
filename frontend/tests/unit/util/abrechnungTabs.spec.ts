import { describe, expect, it } from "vitest";

import {
  nutzungsobjektIdPrefix,
  nutzungsobjektOfError,
  tabOfError,
  TABS,
} from "@/util/abrechnungTabs";

describe("abrechnungTabs.ts", () => {
  describe("tabOfError", () => {
    it("givenErrorOnBasisInput_thenReturnTheBasisTab", () => {
      expect(tabOfError("basis-geschaeftspartner-id")).toBe(TABS.BASIS);
    });

    it("givenErrorOnBerechnungInput_thenReturnTheBerechnungTab", () => {
      expect(tabOfError("berechnung-nutzungsobjekt-0-position-laenge-1")).toBe(
        TABS.BERECHNUNG
      );
    });

    it("givenUnprefixedId_thenReturnUndefined", () => {
      expect(tabOfError("input-42")).toBeUndefined();
    });
  });

  describe("nutzungsobjektOfError", () => {
    it("givenErrorOnNutzungsobjektInput_thenReturnItsIndex", () => {
      expect(nutzungsobjektOfError("berechnung-nutzungsobjekt-2-adresse")).toBe(
        2
      );
    });

    it("givenErrorOnPositionInput_thenReturnTheIndexOfTheNutzungsobjekt", () => {
      expect(
        nutzungsobjektOfError("berechnung-nutzungsobjekt-1-position-ende-3")
      ).toBe(1);
    });

    it("givenErrorOutsideANutzungsobjekt_thenReturnUndefined", () => {
      expect(nutzungsobjektOfError("berechnung-zeitraum-von")).toBeUndefined();
    });
  });

  describe("nutzungsobjektIdPrefix", () => {
    it("givenIndex_thenPrefixNamesTheBerechnungTabAndTheNutzungsobjekt", () => {
      const prefix = nutzungsobjektIdPrefix(0);

      expect(tabOfError(`${prefix}-adresse`)).toBe(TABS.BERECHNUNG);
      expect(nutzungsobjektOfError(`${prefix}-adresse`)).toBe(0);
    });
  });
});
