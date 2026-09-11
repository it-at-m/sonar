import type { PagedModelProjektResponseDTO } from "@/api/generated/sonar-backend";

import { afterEach, describe, expect, it, vi } from "vitest";

import { PagedModelProjektResponseDTOToJSON } from "@/api/generated/sonar-backend";
import { useProjekteListe } from "@/composables/projekteListe";
import { toDateString } from "@/util/formatter";

function stubFetch(
  pageResponse: PagedModelProjektResponseDTO
): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockResolvedValue(
    new Response(
      JSON.stringify(PagedModelProjektResponseDTOToJSON(pageResponse)),
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

describe("projekteListe.ts", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  describe("load", () => {
    it("givenTablePage_thenRequestZeroBasedBackendPage", async () => {
      const pageResponse: PagedModelProjektResponseDTO = { content: [] };
      const fetchSpy = stubFetch(pageResponse);
      const { load } = useProjekteListe();

      await load(3, 25);

      expect(requestedUrl(fetchSpy)).toContain("pageNumber=2");
      expect(requestedUrl(fetchSpy)).toContain("pageSize=25");
    });

    it("givenFirstPage_thenRequestPageZero", async () => {
      const pageResponse: PagedModelProjektResponseDTO = { content: [] };
      const fetchSpy = stubFetch(pageResponse);
      const { load } = useProjekteListe();

      await load(1, 10);

      expect(requestedUrl(fetchSpy)).toContain("pageNumber=0");
    });

    it("givenPageResponse_thenExposeTotalElementsOfAllPages", async () => {
      const pageResponse: PagedModelProjektResponseDTO = {
        content: [],
        page: { size: 25, number: 0, totalElements: 42, totalPages: 2 },
      };
      stubFetch(pageResponse);
      const { load, totalProjekte } = useProjekteListe();

      await load(1, 25);

      expect(totalProjekte.value).toBe(42);
    });

    it("givenPageResponse_thenMapProjekteToRows", async () => {
      const pageResponse: PagedModelProjektResponseDTO = {
        content: [
          {
            id: "123e4567-e89b-12d3-a456-426614174000",
            projektnummer: "2026-0001",
            abrechnungBeginn: new Date("2026-01-01"),
            abrechnungEnde: new Date("2026-03-31"),
            adressen: [{ adresse: "A" }, { adresse: "B" }],
          },
        ],
      };
      stubFetch(pageResponse);
      const { load, rows } = useProjekteListe();

      await load(1, 25);

      expect(rows.value).toHaveLength(1);
      expect(itemAt(rows.value).projektnummer).toBe("2026-0001");
      expect(itemAt(rows.value).anzahlAdressen).toBe(2);
      expect(itemAt(rows.value).abrechnungBeginn).toBe(
        toDateString(new Date("2026-01-01"))
      );
    });

    it("givenProjektnummer_thenSendItAsSearchCriterion", async () => {
      const pageResponse: PagedModelProjektResponseDTO = { content: [] };
      const fetchSpy = stubFetch(pageResponse);
      const { load } = useProjekteListe();

      await load(1, 10, { projektnummer: "2026-" });

      expect(requestedUrl(fetchSpy)).toContain("projektnummer=2026-");
    });

    it("givenDates_thenSendThemAsIsoDates", async () => {
      const pageResponse: PagedModelProjektResponseDTO = { content: [] };
      const fetchSpy = stubFetch(pageResponse);
      const { load } = useProjekteListe();

      await load(1, 10, {
        abrechnungBeginn: "2026-01-01",
        abrechnungEnde: "2026-03-31",
      });

      expect(requestedUrl(fetchSpy)).toContain("abrechnungBeginn=2026-01-01");
      expect(requestedUrl(fetchSpy)).toContain("abrechnungEnde=2026-03-31");
    });

    it("givenEmptyFilter_thenSendNoSearchCriteria", async () => {
      const pageResponse: PagedModelProjektResponseDTO = { content: [] };
      const fetchSpy = stubFetch(pageResponse);
      const { load } = useProjekteListe();

      await load(1, 10, {
        projektnummer: "",
        abrechnungBeginn: "",
        abrechnungEnde: "",
      });

      expect(requestedUrl(fetchSpy)).not.toContain("projektnummer");
      expect(requestedUrl(fetchSpy)).not.toContain("abrechnung");
    });

    it("givenSort_thenSendItAsSortParameters", async () => {
      const pageResponse: PagedModelProjektResponseDTO = { content: [] };
      const fetchSpy = stubFetch(pageResponse);
      const { load } = useProjekteListe();

      await load(
        1,
        10,
        {},
        { sortBy: "ABRECHNUNG_BEGINN", sortDirection: "ASC" }
      );

      expect(requestedUrl(fetchSpy)).toContain("sortBy=ABRECHNUNG_BEGINN");
      expect(requestedUrl(fetchSpy)).toContain("sortDirection=ASC");
    });

    it("givenNoSort_thenSendNoSortParametersSoTheBackendDefaultApplies", async () => {
      const pageResponse: PagedModelProjektResponseDTO = { content: [] };
      const fetchSpy = stubFetch(pageResponse);
      const { load } = useProjekteListe();

      await load(1, 10);

      expect(requestedUrl(fetchSpy)).not.toContain("sortBy");
      expect(requestedUrl(fetchSpy)).not.toContain("sortDirection");
    });

    it("givenFailingRequest_thenRethrowAndResetLoading", async () => {
      vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("offline")));
      const { load, loading } = useProjekteListe();

      const result = load(1, 10);

      await expect(result).rejects.toThrow();
      expect(loading.value).toBe(false);
    });
  });
});
