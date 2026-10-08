import type { AbrechnungPositionForm } from "@/types/abrechnung/AbrechnungPositionForm";

import { describe, expect, it } from "vitest";

import {
  createAbrechnungPosition,
  hasLaengeUndBreite,
  isAbrechnungPositionDirty,
  setBreite,
  setLaenge,
} from "@/util/abrechnung/abrechnungPositionForm";

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

    it("givenFlaeche_thenReturnTrue", () => {
      const position = positionWith({ flaeche: 36 });

      expect(isAbrechnungPositionDirty(position)).toBe(true);
    });

    it("givenAnteilAnFlaecheOfZero_thenReturnTrue", () => {
      const position = positionWith({ anteilAnFlaeche: 0 });

      expect(isAbrechnungPositionDirty(position)).toBe(true);
    });
  });

  describe("hasLaengeUndBreite", () => {
    it("givenBoth_thenReturnTrue", () => {
      const position = createAbrechnungPosition();
      position.laenge = 12;
      position.breite = 3;

      expect(hasLaengeUndBreite(position)).toBe(true);
    });

    it("givenOnlyLaenge_thenReturnFalse", () => {
      const position = createAbrechnungPosition();
      position.laenge = 12;

      expect(hasLaengeUndBreite(position)).toBe(false);
    });
  });

  describe("setLaenge", () => {
    it("givenBreiteIsSet_thenWriteTheProductIntoTheFlaeche", () => {
      const position = createAbrechnungPosition();
      position.breite = 3;

      setLaenge(position, 12);

      expect(position.laenge).toBe(12);
      expect(position.flaeche).toBe(36);
    });

    it("givenAProductWithMoreDecimals_thenRoundItToTwo", () => {
      const position = createAbrechnungPosition();
      position.breite = 0.1;

      setLaenge(position, 1.15);

      expect(position.flaeche).toBe(0.12);
    });

    it("givenAProductOnTheHalf_thenRoundItUp", () => {
      const position = createAbrechnungPosition();
      position.breite = 5.5;

      setLaenge(position, 0.03);

      expect(position.flaeche).toBe(0.17);
    });

    it("givenBreiteIsSet_thenOverwriteATypedFlaeche", () => {
      const position = createAbrechnungPosition();
      position.breite = 3;
      position.flaeche = 99;

      setLaenge(position, 12);

      expect(position.flaeche).toBe(36);
    });

    it("givenNoBreite_thenLeaveTheTypedFlaeche", () => {
      const position = createAbrechnungPosition();
      position.flaeche = 99;

      setLaenge(position, 12);

      expect(position.laenge).toBe(12);
      expect(position.flaeche).toBe(99);
    });

    it("givenLaengeIsCleared_thenLeaveTheFlaecheAsItStands", () => {
      const position = createAbrechnungPosition();
      position.breite = 3;
      setLaenge(position, 12);

      setLaenge(position, null);

      expect(position.laenge).toBeNull();
      expect(position.flaeche).toBe(36);
    });
  });

  describe("setBreite", () => {
    it("givenLaengeIsSet_thenWriteTheProductIntoTheFlaeche", () => {
      const position = createAbrechnungPosition();
      position.laenge = 12;

      setBreite(position, 3);

      expect(position.breite).toBe(3);
      expect(position.flaeche).toBe(36);
    });

    it("givenNoLaenge_thenLeaveTheTypedFlaeche", () => {
      const position = createAbrechnungPosition();
      position.flaeche = 99;

      setBreite(position, 3);

      expect(position.breite).toBe(3);
      expect(position.flaeche).toBe(99);
    });
  });
});
