import { afterEach, describe, expect, it, vi } from "vitest";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";
import { useProjektAdresseSuggestions } from "@/composables/projektAdresseSuggestions";

const PROJEKT_ID = "0f9d1a3c-0f4e-4b9a-8f4a-9a5d1e2b3c4d";

const PROJEKT = {
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
};

function stubFetch(status: number, body: unknown = PROJEKT) {
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

afterEach(() => vi.unstubAllGlobals());

describe("projektAdresseSuggestions.ts", () => {
  describe("load", () => {
    it("givenProjekt_thenOfferItsAdressenInTheOrderTheyWereEntered", async () => {
      stubFetch(200);
      const { load, suggestions } = useProjektAdresseSuggestions();

      await load(PROJEKT_ID);

      expect(suggestions.value).toHaveLength(2);
      expect(suggestions.value[0]?.adresse).toBe("Marienplatz");
      expect(suggestions.value[0]?.unerlaubteNutzungVon).toBe("2026-01-05");
      expect(suggestions.value[1]?.art).toBe(
        ProjektAdresseRequestDTOArtEnum.FLURSTUECK
      );
      expect(suggestions.value[1]?.flurstueck).toBe("1234/5");
    });

    it("givenProjektId_thenRequestThatProjekt", async () => {
      const fetchSpy = stubFetch(200);
      const { load } = useProjektAdresseSuggestions();

      await load(PROJEKT_ID);

      expect(String(fetchSpy.mock.calls[0]?.[0])).toContain(
        `/projekt/${PROJEKT_ID}`
      );
    });

    it("givenUnknownProjekt_thenFailSoTheViewCanReportIt", async () => {
      stubFetch(404, {});
      const { load, suggestions } = useProjektAdresseSuggestions();

      await expect(load(PROJEKT_ID)).rejects.toThrow();
      expect(suggestions.value).toHaveLength(0);
    });
  });
});
