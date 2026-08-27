import type { ProjektAdresseForm } from "@/types/ProjektAdresseForm";

import { describe, expect, it } from "vitest";

import { createProjektAdresse } from "@/util/projektAdresseForm";
import {
  unerlaubteNutzungBisRule,
  unerlaubteNutzungVonRule,
} from "@/util/projektAdresseRules";

function adresseWith(
  overrides: Partial<ProjektAdresseForm>
): ProjektAdresseForm {
  return { ...createProjektAdresse(), ...overrides };
}

describe("projektAdresseRules.ts", () => {
  describe("unerlaubteNutzungBisRule", () => {
    it("givenBeginnWithoutEnde_thenReturnMessage", () => {
      const adresse = adresseWith({ unerlaubteNutzungVon: "2026-01-01" });

      expect(unerlaubteNutzungBisRule(adresse)("")).toBe(
        "Bitte das Ende des Zeitraums angeben."
      );
    });

    it("givenEndeBeforeBeginn_thenReturnMessage", () => {
      const adresse = adresseWith({ unerlaubteNutzungVon: "2026-03-31" });

      expect(unerlaubteNutzungBisRule(adresse)("2026-01-01")).toBe(
        "Das Ende darf nicht vor dem Beginn liegen."
      );
    });

    it("givenEmptyZeitraum_thenReturnTrue", () => {
      expect(unerlaubteNutzungBisRule(adresseWith({}))("")).toBe(true);
    });
  });

  describe("unerlaubteNutzungVonRule", () => {
    it("givenEndeWithoutBeginn_thenReturnMessage", () => {
      const adresse = adresseWith({ unerlaubteNutzungBis: "2026-03-31" });

      expect(unerlaubteNutzungVonRule(adresse)("")).toBe(
        "Bitte den Beginn des Zeitraums angeben."
      );
    });
  });
});
