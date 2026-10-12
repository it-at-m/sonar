import type { Adresse } from "@/types/common/Adresse";

import { describe, expect, it } from "vitest";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungenEnum,
} from "@/api/generated/sonar-backend";
import {
  clearFieldsOfUnselectedArt,
  clearSonstigesOfUnselectedNutzung,
  isAdresseDirty,
} from "@/util/common/adresseForm";

function emptyAdresse(): Adresse {
  return {
    art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
    adresse: "",
    hausnummerVon: "",
    hausnummerBis: "",
    flurstueck: "",
    gemarkung: "",
    nutzungen: [],
    nutzungSonstiges: "",
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
      adresse.nutzungen = [ProjektAdresseRequestDTONutzungenEnum.CONTAINER];
      adresse.art = ProjektAdresseRequestDTOArtEnum.FLURSTUECK;

      clearFieldsOfUnselectedArt(adresse);

      expect(adresse.nutzungen).toEqual([
        ProjektAdresseRequestDTONutzungenEnum.CONTAINER,
      ]);
    });
  });

  describe("clearSonstigesOfUnselectedNutzung", () => {
    it("givenNutzungSonstiges_thenKeepTheBeschreibung", () => {
      const adresse = emptyAdresse();
      adresse.nutzungen = [ProjektAdresseRequestDTONutzungenEnum.SONSTIGES];
      adresse.nutzungSonstiges = "Gerüst über dem Gehweg";

      clearSonstigesOfUnselectedNutzung(adresse);

      expect(adresse.nutzungSonstiges).toBe("Gerüst über dem Gehweg");
    });

    it("givenSonstigesAmongSeveral_thenKeepTheBeschreibung", () => {
      const adresse = emptyAdresse();
      adresse.nutzungen = [
        ProjektAdresseRequestDTONutzungenEnum.BAUZAUN,
        ProjektAdresseRequestDTONutzungenEnum.SONSTIGES,
      ];
      adresse.nutzungSonstiges = "Gerüst über dem Gehweg";

      clearSonstigesOfUnselectedNutzung(adresse);

      expect(adresse.nutzungSonstiges).toBe("Gerüst über dem Gehweg");
    });

    it("givenAndereNutzung_thenDropTheBeschreibung", () => {
      const adresse = emptyAdresse();
      adresse.nutzungen = [ProjektAdresseRequestDTONutzungenEnum.BAUZAUN];
      adresse.nutzungSonstiges = "Gerüst über dem Gehweg";

      clearSonstigesOfUnselectedNutzung(adresse);

      expect(adresse.nutzungSonstiges).toBe("");
    });

    it("givenSeveralNutzungenWithoutSonstiges_thenDropTheBeschreibung", () => {
      const adresse = emptyAdresse();
      adresse.nutzungen = [
        ProjektAdresseRequestDTONutzungenEnum.BAUZAUN,
        ProjektAdresseRequestDTONutzungenEnum.CONTAINER,
      ];
      adresse.nutzungSonstiges = "Gerüst über dem Gehweg";

      clearSonstigesOfUnselectedNutzung(adresse);

      expect(adresse.nutzungSonstiges).toBe("");
    });

    it("givenKeineNutzung_thenDropTheBeschreibung", () => {
      const adresse = emptyAdresse();
      adresse.nutzungSonstiges = "Gerüst über dem Gehweg";

      clearSonstigesOfUnselectedNutzung(adresse);

      expect(adresse.nutzungSonstiges).toBe("");
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
      adresse.nutzungen = [ProjektAdresseRequestDTONutzungenEnum.BAUZAUN];

      expect(isAdresseDirty(adresse)).toBe(true);
    });

    it("givenNutzungSonstigesBeschreibung_thenReturnTrue", () => {
      const adresse = emptyAdresse();
      adresse.nutzungSonstiges = "Gerüst über dem Gehweg";

      expect(isAdresseDirty(adresse)).toBe(true);
    });

    it("givenHausnummerBisOnly_thenReturnTrue", () => {
      const adresse = emptyAdresse();
      adresse.hausnummerBis = "9";

      expect(isAdresseDirty(adresse)).toBe(true);
    });
  });
});
