import type { ProjektAdresseSuggestion } from "@/types/ProjektAdresseSuggestion";

import { describe, expect, it } from "vitest";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungEnum,
} from "@/api/generated/sonar-backend";
import { createAbrechnungNutzungsobjekt } from "@/util/abrechnungNutzungsobjektForm";
import {
  applyProjektAdresseSuggestion,
  projektAdresseSuggestionSubtitle,
  projektAdresseSuggestionTitle,
} from "@/util/projektAdresseSuggestion";

function suggestion(
  overrides: Partial<ProjektAdresseSuggestion> = {}
): ProjektAdresseSuggestion {
  return {
    art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
    adresse: "Marienplatz",
    hausnummerVon: "8",
    hausnummerBis: "",
    flurstueck: "",
    gemarkung: "",
    nutzung: null,
    unerlaubteNutzungVon: "",
    unerlaubteNutzungBis: "",
    tageUnerlaubteNutzung: null,
    ...overrides,
  };
}

describe("projektAdresseSuggestion.ts", () => {
  describe("projektAdresseSuggestionTitle", () => {
    it("givenAdresse_thenNameItWithItsHausnummer", () => {
      expect(projektAdresseSuggestionTitle(suggestion())).toBe("Marienplatz 8");
    });

    it("givenSpanOfHausnummern_thenNameBothEnds", () => {
      const title = projektAdresseSuggestionTitle(
        suggestion({ hausnummerBis: "10" })
      );

      expect(title).toBe("Marienplatz 8–10");
    });

    it("givenAdresseWithoutHausnummer_thenNameOnlyTheStreet", () => {
      const title = projektAdresseSuggestionTitle(
        suggestion({ hausnummerVon: "" })
      );

      expect(title).toBe("Marienplatz");
    });

    it("givenFlurstueck_thenNameItWithItsGemarkung", () => {
      const title = projektAdresseSuggestionTitle(
        suggestion({
          art: ProjektAdresseRequestDTOArtEnum.FLURSTUECK,
          adresse: "",
          hausnummerVon: "",
          flurstueck: "1234/5",
          gemarkung: "Sendling",
        })
      );

      expect(title).toBe("Flurstück 1234/5, Sendling");
    });
  });

  describe("projektAdresseSuggestionSubtitle", () => {
    it("givenNutzung_thenNameIt", () => {
      const subtitle = projektAdresseSuggestionSubtitle(
        suggestion({ nutzung: ProjektAdresseRequestDTONutzungEnum.NUTZUNG_B })
      );

      expect(subtitle).toBe("Nutzung B");
    });

    it("givenNoNutzung_thenStayEmptySoTheEntryShowsOneLine", () => {
      expect(projektAdresseSuggestionSubtitle(suggestion())).toBe("");
    });
  });

  describe("applyProjektAdresseSuggestion", () => {
    it("givenSuggestion_thenFillTheAdresseAndTheUnerlaubteNutzung", () => {
      const nutzungsobjekt = createAbrechnungNutzungsobjekt();

      applyProjektAdresseSuggestion(
        nutzungsobjekt,
        suggestion({
          nutzung: ProjektAdresseRequestDTONutzungEnum.NUTZUNG_B,
          unerlaubteNutzungVon: "2026-01-01",
          unerlaubteNutzungBis: "2026-01-31",
          tageUnerlaubteNutzung: 31,
        })
      );

      expect(nutzungsobjekt.art).toBe(ProjektAdresseRequestDTOArtEnum.ADRESSE);
      expect(nutzungsobjekt.adresse).toBe("Marienplatz");
      expect(nutzungsobjekt.hausnummerVon).toBe("8");
      expect(nutzungsobjekt.nutzung).toBe(
        ProjektAdresseRequestDTONutzungEnum.NUTZUNG_B
      );
      expect(nutzungsobjekt.unerlaubteNutzungVon).toBe("2026-01-01");
      expect(nutzungsobjekt.unerlaubteNutzungBis).toBe("2026-01-31");
      expect(nutzungsobjekt.tageUnerlaubteNutzung).toBe(31);
    });

    it("givenFlurstueck_thenClearWhatBelongsToAnAdresse", () => {
      const nutzungsobjekt = createAbrechnungNutzungsobjekt();
      nutzungsobjekt.adresse = "Sendlinger Straße";
      nutzungsobjekt.hausnummerVon = "1";
      nutzungsobjekt.hausnummerBis = "3";

      applyProjektAdresseSuggestion(
        nutzungsobjekt,
        suggestion({
          art: ProjektAdresseRequestDTOArtEnum.FLURSTUECK,
          adresse: "",
          hausnummerVon: "",
          flurstueck: "1234/5",
          gemarkung: "Sendling",
        })
      );

      expect(nutzungsobjekt.art).toBe(
        ProjektAdresseRequestDTOArtEnum.FLURSTUECK
      );
      expect(nutzungsobjekt.flurstueck).toBe("1234/5");
      expect(nutzungsobjekt.gemarkung).toBe("Sendling");
      expect(nutzungsobjekt.adresse).toBe("");
      expect(nutzungsobjekt.hausnummerVon).toBe("");
      expect(nutzungsobjekt.hausnummerBis).toBe("");
    });

    it("givenSuggestion_thenKeepWhatTheProjektDoesNotKnow", () => {
      const nutzungsobjekt = createAbrechnungNutzungsobjekt();
      nutzungsobjekt.bemerkung = "Zweite Mahnung";

      applyProjektAdresseSuggestion(nutzungsobjekt, suggestion());

      expect(nutzungsobjekt.bemerkung).toBe("Zweite Mahnung");
      expect(nutzungsobjekt.positionen).toHaveLength(1);
    });
  });
});
