import type { AbrechnungPositionForm } from "@/types/abrechnung/AbrechnungPositionForm";

import { describe, expect, it } from "vitest";

import {
  alignPositionenToZeitraum,
  createAbrechnungPosition,
  hasLaengeUndBreite,
  isAbrechnungPositionDirty,
  isPossibleBeginn,
  moveBeginneWithZeitraumVon,
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

  describe("alignPositionenToZeitraum", () => {
    it("givenOnePosition_thenSpanTheWholeZeitraum", () => {
      const position = createAbrechnungPosition();

      alignPositionenToZeitraum([position], "2026-01-05", "2026-02-01");

      expect(position.beginn).toBe("2026-01-05");
      expect(position.ende).toBe("2026-02-01");
    });

    it("givenSeveralPositionen_thenEndEachTheDayBeforeTheNextBegins", () => {
      const erste = createAbrechnungPosition();
      const zweite = createAbrechnungPosition();
      zweite.beginn = "2026-01-12";
      const dritte = createAbrechnungPosition();
      dritte.beginn = "2026-01-26";

      alignPositionenToZeitraum(
        [erste, zweite, dritte],
        "2026-01-05",
        "2026-02-01"
      );

      expect(erste.beginn).toBe("2026-01-05");
      expect(erste.ende).toBe("2026-01-11");
      expect(zweite.beginn).toBe("2026-01-12");
      expect(zweite.ende).toBe("2026-01-25");
      expect(dritte.beginn).toBe("2026-01-26");
      expect(dritte.ende).toBe("2026-02-01");
    });

    it("givenSuccessorWithoutBeginn_thenLeaveTheEndeEmpty", () => {
      const erste = createAbrechnungPosition();
      erste.ende = "2026-01-11";
      const zweite = createAbrechnungPosition();

      alignPositionenToZeitraum([erste, zweite], "2026-01-05", "2026-02-01");

      expect(erste.ende).toBe("");
    });

    it("givenNoPositionen_thenDoNothing", () => {
      expect(() =>
        alignPositionenToZeitraum([], "2026-01-05", "2026-02-01")
      ).not.toThrow();
    });
  });

  describe("moveBeginneWithZeitraumVon", () => {
    it("givenMovedZeitraumVon_thenKeepTheWeeksOfEachBeginn", () => {
      const erste = createAbrechnungPosition();
      erste.beginn = "2026-01-05";
      const zweite = createAbrechnungPosition();
      zweite.beginn = "2026-01-19";

      moveBeginneWithZeitraumVon([erste, zweite], "2026-01-05", "2026-01-08");

      expect(erste.beginn).toBe("2026-01-05");
      expect(zweite.beginn).toBe("2026-01-22");
    });

    it("givenBeginnOffTheWeeks_thenLeaveIt", () => {
      const erste = createAbrechnungPosition();
      const zweite = createAbrechnungPosition();
      zweite.beginn = "2026-01-15";

      moveBeginneWithZeitraumVon([erste, zweite], "2026-01-05", "2026-01-08");

      expect(zweite.beginn).toBe("2026-01-15");
    });

    it("givenNoPreviousZeitraumVon_thenLeaveTheBeginn", () => {
      const erste = createAbrechnungPosition();
      const zweite = createAbrechnungPosition();
      zweite.beginn = "2026-01-19";

      moveBeginneWithZeitraumVon([erste, zweite], "", "2026-01-08");

      expect(zweite.beginn).toBe("2026-01-19");
    });
  });

  describe("isPossibleBeginn", () => {
    it("givenWholeWeeksOnBothSides_thenReturnTrue", () => {
      const vorgaenger = createAbrechnungPosition();
      vorgaenger.beginn = "2026-01-05";
      const position = createAbrechnungPosition();
      position.ende = "2026-02-01";

      expect(isPossibleBeginn(vorgaenger, position, "2026-01-19")).toBe(true);
    });

    it("givenPartialWeekForTheVorgaenger_thenReturnFalse", () => {
      const vorgaenger = createAbrechnungPosition();
      vorgaenger.beginn = "2026-01-05";
      const position = createAbrechnungPosition();
      position.ende = "2026-02-01";

      expect(isPossibleBeginn(vorgaenger, position, "2026-01-15")).toBe(false);
    });

    it("givenBeginnOfTheVorgaenger_thenReturnFalse", () => {
      const vorgaenger = createAbrechnungPosition();
      vorgaenger.beginn = "2026-01-05";
      const position = createAbrechnungPosition();
      position.ende = "2026-02-01";

      expect(isPossibleBeginn(vorgaenger, position, "2026-01-05")).toBe(false);
    });

    it("givenNoWeekLeftUntilTheEnde_thenReturnFalse", () => {
      const vorgaenger = createAbrechnungPosition();
      vorgaenger.beginn = "2026-01-05";
      const position = createAbrechnungPosition();
      position.ende = "2026-02-01";

      expect(isPossibleBeginn(vorgaenger, position, "2026-02-02")).toBe(false);
    });

    it("givenNoEnde_thenOnlyCheckTheVorgaenger", () => {
      const vorgaenger = createAbrechnungPosition();
      vorgaenger.beginn = "2026-01-05";
      const position = createAbrechnungPosition();

      expect(isPossibleBeginn(vorgaenger, position, "2026-03-02")).toBe(true);
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
