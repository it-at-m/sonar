import type { PagedModelAbrechnungResponseDTO } from "@/api/generated/sonar-backend";

import { afterEach, describe, expect, it, vi } from "vitest";

import { PagedModelAbrechnungResponseDTOToJSON } from "@/api/generated/sonar-backend";
import { fetchAbrechnungenPage } from "@/util/abrechnungenListe";
import { toDateString } from "@/util/formatter";

const PROJEKT_ID = "123e4567-e89b-12d3-a456-426614174000";

function stubFetch(
  pageResponse: PagedModelAbrechnungResponseDTO
): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockResolvedValue(
    new Response(
      JSON.stringify(PagedModelAbrechnungResponseDTOToJSON(pageResponse)),
      {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }
    )
  );
  vi.stubGlobal("fetch", fetchSpy);
  return fetchSpy;
}

function itemAt<T>(items: readonly T[], index = 0): T {
  const item = items[index];
  if (item === undefined) {
    throw new Error(`Kein Element an Position ${index}.`);
  }
  return item;
}

function requestedUrl(fetchSpy: ReturnType<typeof vi.fn>): string {
  return fetchSpy.mock.calls[0]?.[0] as string;
}

describe("abrechnungenListe.ts", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  describe("fetchAbrechnungenPage", () => {
    it("givenProjektId_thenRequestTheAbrechnungenOfThatProjekt", async () => {
      const fetchSpy = stubFetch({ content: [] });

      await fetchAbrechnungenPage(PROJEKT_ID, 1, 10);

      expect(requestedUrl(fetchSpy)).toContain(
        `/projekt/${PROJEKT_ID}/abrechnung`
      );
    });

    it("givenTablePage_thenRequestZeroBasedBackendPage", async () => {
      const fetchSpy = stubFetch({ content: [] });

      await fetchAbrechnungenPage(PROJEKT_ID, 3, 25);

      expect(requestedUrl(fetchSpy)).toContain("pageNumber=2");
      expect(requestedUrl(fetchSpy)).toContain("pageSize=25");
    });

    it("givenPageResponse_thenReturnTotalElementsOfAllPages", async () => {
      stubFetch({
        content: [],
        page: { size: 25, number: 0, totalElements: 42, totalPages: 2 },
      });

      const { totalAbrechnungen } = await fetchAbrechnungenPage(
        PROJEKT_ID,
        1,
        25
      );

      expect(totalAbrechnungen).toBe(42);
    });

    it("givenPageResponse_thenMapAbrechnungenToRows", async () => {
      stubFetch({
        content: [
          {
            id: "123e4567-e89b-12d3-a456-426614174001",
            geschaeftspartnerId: "1000000001",
            zeitraumVon: new Date("2026-01-01"),
            zeitraumBis: new Date("2026-03-31"),
            abrechnungsArt: "ENDABRECHNUNG",
            nutzungsobjekte: [{ adresse: "A" }, { adresse: "B" }],
          },
        ],
      });

      const { rows } = await fetchAbrechnungenPage(PROJEKT_ID, 1, 25);

      expect(rows).toHaveLength(1);
      expect(itemAt(rows).geschaeftspartnerId).toBe("1000000001");
      expect(itemAt(rows).abrechnungsArt).toBe("Endabrechnung");
      expect(itemAt(rows).anzahlNutzungsobjekte).toBe(2);
      expect(itemAt(rows).zeitraumVon).toBe(
        toDateString(new Date("2026-01-01"))
      );
    });

    it("givenSort_thenSendItAsSortParameters", async () => {
      const fetchSpy = stubFetch({ content: [] });

      await fetchAbrechnungenPage(PROJEKT_ID, 1, 10, {
        sortBy: ["ZEITRAUM_BIS"],
        sortDirection: ["ASC"],
      });

      expect(requestedUrl(fetchSpy)).toContain("sortBy=ZEITRAUM_BIS");
      expect(requestedUrl(fetchSpy)).toContain("sortDirection=ASC");
    });

    it("givenSeveralSortColumns_thenSendOneCommaSeparatedSortParameter", async () => {
      const fetchSpy = stubFetch({ content: [] });

      await fetchAbrechnungenPage(PROJEKT_ID, 1, 10, {
        sortBy: ["ABRECHNUNGS_ART", "ZEITRAUM_VON"],
        sortDirection: ["ASC", "DESC"],
      });

      expect(requestedUrl(fetchSpy)).toContain(
        "sortBy=ABRECHNUNGS_ART%2CZEITRAUM_VON"
      );
      expect(requestedUrl(fetchSpy)).toContain("sortDirection=ASC%2CDESC");
      expect(requestedUrl(fetchSpy)).not.toContain("sortBy=ABRECHNUNGS_ART&");
    });

    it("givenNoSort_thenSendNoSortParametersSoTheBackendDefaultApplies", async () => {
      const fetchSpy = stubFetch({ content: [] });

      await fetchAbrechnungenPage(PROJEKT_ID, 1, 10);

      expect(requestedUrl(fetchSpy)).not.toContain("sortBy");
      expect(requestedUrl(fetchSpy)).not.toContain("sortDirection");
    });

    it("givenFailingRequest_thenFailSoTheViewCanReportIt", async () => {
      vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("offline")));

      await expect(fetchAbrechnungenPage(PROJEKT_ID, 1, 10)).rejects.toThrow();
    });
  });
});
