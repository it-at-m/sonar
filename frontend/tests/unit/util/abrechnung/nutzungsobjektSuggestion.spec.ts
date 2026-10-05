import type { NutzungsobjektSuggestion } from "@/types/abrechnung/NutzungsobjektSuggestion";

import { afterEach, describe, expect, it, vi } from "vitest";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungEnum,
} from "@/api/generated/sonar-backend";
import { createAbrechnungNutzungsobjekt } from "@/util/abrechnung/abrechnungNutzungsobjektForm";
import {
  applyNutzungsobjektSuggestion,
  fetchNutzungsobjektSuggestions,
} from "@/util/abrechnung/nutzungsobjektSuggestion";

const PROJEKT_ID = "0f9d1a3c-0f4e-4b9a-8f4a-9a5d1e2b3c4d";
const NUTZUNGSOBJEKT_ID = "7c6b5a4d-3e2f-4a1b-9c8d-7e6f5a4b3c2d";

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
  overrides: Partial<NutzungsobjektSuggestion> = {}
): NutzungsobjektSuggestion {
  return {
    id: NUTZUNGSOBJEKT_ID,
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
    bemerkung: "",
    aufschlag50prozent: false,
    ...overrides,
  };
}

afterEach(() => vi.unstubAllGlobals());

describe("nutzungsobjektSuggestion.ts", () => {
  describe("fetchNutzungsobjektSuggestions", () => {
    it("givenProjekt_thenOfferItsNutzungsobjekte", async () => {
      stubFetch(200, [
        {
          id: NUTZUNGSOBJEKT_ID,
          art: "ADRESSE",
          adresse: "Marienplatz",
          hausnummerVon: "8",
          nutzung: "NUTZUNG_A",
          unerlaubteNutzungVon: "2026-01-05",
          unerlaubteNutzungBis: "2026-01-10",
          tageUnerlaubteNutzung: 6,
          bemerkung: "Zweite Mahnung",
          aufschlag50prozent: true,
        },
        {
          id: "2f3e4d5c-6b7a-4891-a0b1-c2d3e4f5a6b7",
          art: "FLURSTUECK",
          flurstueck: "1234/5",
          gemarkung: "Sendling",
          aufschlag50prozent: false,
        },
      ]);

      const suggestions = await fetchNutzungsobjektSuggestions(PROJEKT_ID);

      expect(suggestions).toHaveLength(2);
      expect(suggestions[0]?.id).toBe(NUTZUNGSOBJEKT_ID);
      expect(suggestions[0]?.adresse).toBe("Marienplatz");
      expect(suggestions[0]?.unerlaubteNutzungVon).toBe("2026-01-05");
      expect(suggestions[0]?.bemerkung).toBe("Zweite Mahnung");
      expect(suggestions[0]?.aufschlag50prozent).toBe(true);
      expect(suggestions[1]?.art).toBe(
        ProjektAdresseRequestDTOArtEnum.FLURSTUECK
      );
      expect(suggestions[1]?.flurstueck).toBe("1234/5");
    });

    it("givenProjektId_thenRequestTheNutzungsobjekteOfThatProjekt", async () => {
      const fetchSpy = stubFetch(200, []);

      await fetchNutzungsobjektSuggestions(PROJEKT_ID);

      expect(String(fetchSpy.mock.calls[0]?.[0])).toContain(
        `/projekt/${PROJEKT_ID}/abrechnung/nutzungsobjekt`
      );
    });

    it("givenUnknownProjekt_thenFailSoTheViewCanReportIt", async () => {
      stubFetch(404, {});

      await expect(
        fetchNutzungsobjektSuggestions(PROJEKT_ID)
      ).rejects.toThrow();
    });
  });

  describe("applyNutzungsobjektSuggestion", () => {
    it("givenSuggestion_thenFillEveryFieldOfTheNutzungsobjekt", () => {
      const nutzungsobjekt = createAbrechnungNutzungsobjekt();

      applyNutzungsobjektSuggestion(
        nutzungsobjekt,
        suggestion({
          nutzung: ProjektAdresseRequestDTONutzungEnum.NUTZUNG_B,
          unerlaubteNutzungVon: "2026-01-01",
          unerlaubteNutzungBis: "2026-01-31",
          tageUnerlaubteNutzung: 31,
          bemerkung: "Zweite Mahnung",
          aufschlag50prozent: true,
        })
      );

      expect(nutzungsobjekt.adresse).toBe("Marienplatz");
      expect(nutzungsobjekt.hausnummerVon).toBe("8");
      expect(nutzungsobjekt.nutzung).toBe(
        ProjektAdresseRequestDTONutzungEnum.NUTZUNG_B
      );
      expect(nutzungsobjekt.unerlaubteNutzungVon).toBe("2026-01-01");
      expect(nutzungsobjekt.tageUnerlaubteNutzung).toBe(31);
      expect(nutzungsobjekt.bemerkung).toBe("Zweite Mahnung");
      expect(nutzungsobjekt.aufschlag50prozent).toBe(true);
    });

    it("givenSuggestion_thenRememberWhichNutzungsobjektIsBilled", () => {
      const nutzungsobjekt = createAbrechnungNutzungsobjekt();

      applyNutzungsobjektSuggestion(nutzungsobjekt, suggestion());

      expect(nutzungsobjekt.uebernommenesNutzungsobjektId).toBe(
        NUTZUNGSOBJEKT_ID
      );
    });

    it("givenSuggestion_thenKeepTheEntryAndItsPositionen", () => {
      const nutzungsobjekt = createAbrechnungNutzungsobjekt();
      const entryId = nutzungsobjekt.id;
      const positionen = nutzungsobjekt.positionen;

      applyNutzungsobjektSuggestion(nutzungsobjekt, suggestion());

      expect(nutzungsobjekt.id).toBe(entryId);
      expect(nutzungsobjekt.positionen).toBe(positionen);
    });
  });
});
