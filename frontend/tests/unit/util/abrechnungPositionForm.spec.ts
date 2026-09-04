import type { AbrechnungPositionForm } from "@/types/AbrechnungPositionForm";

import { describe, expect, it } from "vitest";

import {
  createAbrechnungPosition,
  isAbrechnungPositionDirty,
} from "@/util/abrechnungPositionForm";

function positionWith(
  overrides: Partial<AbrechnungPositionForm>
): AbrechnungPositionForm {
  return { ...createAbrechnungPosition(), ...overrides };
}

describe("abrechnungPositionForm.ts", () => {
  describe("isAbrechnungPositionDirty", () => {
    it("givenNewPosition_thenReturnFalse", () => {
      expect(isAbrechnungPositionDirty(createAbrechnungPosition())).toBe(false);
    });

    it("givenHaelfte_thenReturnTrue", () => {
      const position = positionWith({ haelfte: true });

      expect(isAbrechnungPositionDirty(position)).toBe(true);
    });

    it("givenFlaeche_thenReturnTrue", () => {
      const position = positionWith({ flaeche: 36 });

      expect(isAbrechnungPositionDirty(position)).toBe(true);
    });

    it("givenAnteilAnFlaecheOfZero_thenReturnTrue", () => {
      const position = positionWith({ anteilAnFlaeche: 0 });

      expect(isAbrechnungPositionDirty(position)).toBe(true);
    });
  });
});
