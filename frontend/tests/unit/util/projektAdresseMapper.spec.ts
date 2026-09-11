import { describe, expect, it } from "vitest";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungEnum,
  ProjektAdresseResponseDTOArtEnum,
  ProjektAdresseResponseDTONutzungEnum,
} from "@/api/generated/sonar-backend";
import { toProjektAdresseSuggestion } from "@/util/projektAdresseMapper";

describe("projektAdresseMapper.ts", () => {
  describe("toProjektAdresseSuggestion", () => {
    it("givenAdresse_thenMapItWithItsUnerlaubteNutzung", () => {
      const suggestion = toProjektAdresseSuggestion({
        id: "0f9d1a3c-0f4e-4b9a-8f4a-9a5d1e2b3c4d",
        art: ProjektAdresseResponseDTOArtEnum.ADRESSE,
        adresse: "Marienplatz",
        hausnummerVon: "8",
        hausnummerBis: "10",
        nutzung: ProjektAdresseResponseDTONutzungEnum.NUTZUNG_A,
        unerlaubteNutzungVon: new Date("2026-01-05"),
        unerlaubteNutzungBis: new Date("2026-01-10"),
        tageUnerlaubteNutzung: 6,
        anzahlMahnungen: 2,
        sondernutzungErlaubt: true,
      });

      expect(suggestion).toEqual({
        art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
        adresse: "Marienplatz",
        hausnummerVon: "8",
        hausnummerBis: "10",
        flurstueck: "",
        gemarkung: "",
        nutzung: ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A,
        unerlaubteNutzungVon: "2026-01-05",
        unerlaubteNutzungBis: "2026-01-10",
        tageUnerlaubteNutzung: 6,
      });
    });

    it("givenFlurstueck_thenMapItWithItsGemarkung", () => {
      const suggestion = toProjektAdresseSuggestion({
        art: ProjektAdresseResponseDTOArtEnum.FLURSTUECK,
        flurstueck: "1234/5",
        gemarkung: "Sendling",
      });

      expect(suggestion.art).toBe(ProjektAdresseRequestDTOArtEnum.FLURSTUECK);
      expect(suggestion.flurstueck).toBe("1234/5");
      expect(suggestion.gemarkung).toBe("Sendling");
    });

    it("givenOmittedFields_thenMapThemToTheEmptyValueOfTheirInput", () => {
      const suggestion = toProjektAdresseSuggestion({
        art: ProjektAdresseResponseDTOArtEnum.ADRESSE,
        adresse: "Marienplatz",
        hausnummerVon: "8",
      });

      expect(suggestion.hausnummerBis).toBe("");
      expect(suggestion.flurstueck).toBe("");
      expect(suggestion.gemarkung).toBe("");
      expect(suggestion.nutzung).toBeNull();
      expect(suggestion.unerlaubteNutzungVon).toBe("");
      expect(suggestion.unerlaubteNutzungBis).toBe("");
      expect(suggestion.tageUnerlaubteNutzung).toBeNull();
    });
  });
});
