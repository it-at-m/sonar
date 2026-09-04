import type { Adresse } from "@/types/Adresse";

import { describe, expect, it } from "vitest";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungEnum,
} from "@/api/generated/sonar-backend";
import { clearFieldsOfUnselectedArt, isAdresseDirty } from "@/util/adresseForm";

function emptyAdresse(): Adresse {
  return {
    art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
    adresse: "",
    hausnummerVon: "",
    hausnummerBis: "",
    flurstueck: "",
    gemarkung: "",
    nutzung: null,
  };
}

describe("adresseForm.ts", () => {
  describe("clearFieldsOfUnselectedArt", () => {
    it("givenArtAdresse_thenDropFlurstueckAndGemarkung", () => {
      const adresse = emptyAdresse();
      adresse.flurstueck = "1234/5";
      adresse.gemarkung = "Sendling";
      adresse.adresse = "Marienplatz";

      clearFieldsOfUnselectedArt(adresse);

      expect(adresse.flurstueck).toBe("");
      expect(adresse.gemarkung).toBe("");
      expect(adresse.adresse).toBe("Marienplatz");
    });

    it("givenArtFlurstueck_thenDropAdresseAndHausnummern", () => {
      const adresse = emptyAdresse();
      adresse.art = ProjektAdresseRequestDTOArtEnum.FLURSTUECK;
      adresse.adresse = "Marienplatz";
      adresse.hausnummerVon = "1";
      adresse.hausnummerBis = "9";
      adresse.flurstueck = "1234/5";

      clearFieldsOfUnselectedArt(adresse);

      expect(adresse.adresse).toBe("");
      expect(adresse.hausnummerVon).toBe("");
      expect(adresse.hausnummerBis).toBe("");
      expect(adresse.flurstueck).toBe("1234/5");
    });

    it("givenNutzung_thenKeepItForBothArten", () => {
      const adresse = emptyAdresse();
      adresse.nutzung = ProjektAdresseRequestDTONutzungEnum.NUTZUNG_B;
      adresse.art = ProjektAdresseRequestDTOArtEnum.FLURSTUECK;

      clearFieldsOfUnselectedArt(adresse);

      expect(adresse.nutzung).toBe(
        ProjektAdresseRequestDTONutzungEnum.NUTZUNG_B
      );
    });
  });

  describe("isAdresseDirty", () => {
    it("givenEmptyAdresse_thenReturnFalse", () => {
      expect(isAdresseDirty(emptyAdresse())).toBe(false);
    });

    it("givenChangedArt_thenReturnTrue", () => {
      const adresse = emptyAdresse();
      adresse.art = ProjektAdresseRequestDTOArtEnum.FLURSTUECK;

      expect(isAdresseDirty(adresse)).toBe(true);
    });

    it("givenNutzung_thenReturnTrue", () => {
      const adresse = emptyAdresse();
      adresse.nutzung = ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A;

      expect(isAdresseDirty(adresse)).toBe(true);
    });

    it("givenHausnummerBisOnly_thenReturnTrue", () => {
      const adresse = emptyAdresse();
      adresse.hausnummerBis = "9";

      expect(isAdresseDirty(adresse)).toBe(true);
    });
  });
});
