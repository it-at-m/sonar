import { describe, expect, it } from "vitest";

import { createAbrechnungMast } from "@/util/abrechnung/abrechnungMastForm";

describe("abrechnungMastForm.ts", () => {
  describe("createAbrechnungMast", () => {
    it("givenNewMast_thenStartItWithAnEmptyZeitraum", () => {
      const mast = createAbrechnungMast();

      expect(mast.beginn).toBe("");
      expect(mast.ende).toBe("");
    });

    it("givenTwoNewMasten_thenGiveThemDifferentKeys", () => {
      expect(createAbrechnungMast().key).not.toBe(createAbrechnungMast().key);
    });
  });
});
