import type { ProjektAdresseSuggestion } from "@/types/ProjektAdresseSuggestion";

import { afterEach, describe, expect, it, vi } from "vitest";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungEnum,
} from "@/api/generated/sonar-backend";
import { createAbrechnungNutzungsobjekt } from "@/util/abrechnungNutzungsobjektForm";
import {
  applyProjektAdresseSuggestion,
  fetchProjektAdresseSuggestions,
  projektAdresseSuggestionSubtitle,
  projektAdresseSuggestionTitle,
} from "@/util/projektAdresseSuggestion";

const PROJEKT_ID = "0f9d1a3c-0f4e-4b9a-8f4a-9a5d1e2b3c4d";

function stubFetch(status: number, body: unknown) {
  const fetchSpy = vi.fn(
    () =>
      new Response(JSON.stringify(body), {
        status,
        headers: { "content-type": "application/json" },
      })
  );
  vi.stubGlobal("fetch", fetchSpy);
  return fetchSpy;
}

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

afterEach(() => vi.unstubAllGlobals());

describe("projektAdresseSuggestion.ts", () => {
  describe("fetchProjektAdresseSuggestions", () => {
    it("givenProjekt_thenOfferItsAdressenInTheOrderTheyWereEntered", async () => {
      stubFetch(200, {
        id: PROJEKT_ID,
        projektnummer: "2026-0001",
        abrechnungBeginn: "2026-01-01",
        abrechnungEnde: "2026-03-31",
        adressen: [
          {
            art: "ADRESSE",
            adresse: "Marienplatz",
            hausnummerVon: "8",
            unerlaubteNutzungVon: "2026-01-05",
            unerlaubteNutzungBis: "2026-01-10",
            tageUnerlaubteNutzung: 6,
            anzahlMahnungen: 2,
            sondernutzungErlaubt: true,
          },
          {
            art: "FLURSTUECK",
            flurstueck: "1234/5",
            gemarkung: "Sendling",
            anzahlMahnungen: 0,
            sondernutzungErlaubt: false,
          },
        ],
      });

      const suggestions = await fetchProjektAdresseSuggestions(PROJEKT_ID);

      expect(suggestions).toHaveLength(2);
      expect(suggestions[0]?.adresse).toBe("Marienplatz");
      expect(suggestions[0]?.unerlaubteNutzungVon).toBe("2026-01-05");
      expect(suggestions[1]?.art).toBe(
        ProjektAdresseRequestDTOArtEnum.FLURSTUECK
      );
      expect(suggestions[1]?.flurstueck).toBe("1234/5");
    });

    it("givenProjektId_thenRequestThatProjekt", async () => {
      const fetchSpy = stubFetch(200, {
        id: PROJEKT_ID,
        projektnummer: "2026-0001",
        abrechnungBeginn: "2026-01-01",
        abrechnungEnde: "2026-03-31",
        adressen: [],
      });

      await fetchProjektAdresseSuggestions(PROJEKT_ID);

      expect(String(fetchSpy.mock.calls[0]?.[0])).toContain(
        `/projekt/${PROJEKT_ID}`
      );
    });

    it("givenUnknownProjekt_thenFailSoTheViewCanReportIt", async () => {
      stubFetch(404, {});

      await expect(
        fetchProjektAdresseSuggestions(PROJEKT_ID)
      ).rejects.toThrow();
    });
  });

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
