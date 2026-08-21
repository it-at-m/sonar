import { afterEach, describe, expect, it, vi } from "vitest";

import { useProjekteListe } from "@/composables/projekteListe";
import { toDateString } from "@/util/formatter";

const PAGE_RESPONSE = {
  content: [
    {
      id: "123e4567-e89b-12d3-a456-426614174000",
      projektnummer: "2026-0001",
      abrechnungBeginn: "2026-01-01",
      abrechnungEnde: "2026-03-31",
      adressen: [
        { id: "123e4567-e89b-12d3-a456-426614174001", bezeichnung: "A" },
        { id: "123e4567-e89b-12d3-a456-426614174002", bezeichnung: "B" },
      ],
    },
  ],
  page: { size: 25, number: 2, totalElements: 42, totalPages: 2 },
};

function stubFetch(): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockResolvedValue(
    new Response(JSON.stringify(PAGE_RESPONSE), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    })
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
      // given
      const fetchSpy = stubFetch();
      const { load } = useProjekteListe();

      // when
      await load(3, 25);

      // then
      expect(requestedUrl(fetchSpy)).toContain("pageNumber=2");
      expect(requestedUrl(fetchSpy)).toContain("pageSize=25");
    });

    it("givenFirstPage_thenRequestPageZero", async () => {
      // given
      const fetchSpy = stubFetch();
      const { load } = useProjekteListe();

      // when
      await load(1, 10);

      // then
      expect(requestedUrl(fetchSpy)).toContain("pageNumber=0");
    });

    it("givenPageResponse_thenExposeTotalElementsOfAllPages", async () => {
      // given
      stubFetch();
      const { gesamtAnzahl, load } = useProjekteListe();

      // when
      await load(1, 25);

      // then
      expect(gesamtAnzahl.value).toBe(42);
    });

    it("givenPageResponse_thenMapProjekteToRows", async () => {
      // given
      stubFetch();
      const { load, zeilen } = useProjekteListe();

      // when
      await load(1, 25);

      // then
      expect(zeilen.value).toHaveLength(1);
      expect(itemAt(zeilen.value).projektnummer).toBe("2026-0001");
      expect(itemAt(zeilen.value).anzahlAdressen).toBe(2);
      expect(itemAt(zeilen.value).abrechnungBeginn).toBe(
        toDateString(new Date("2026-01-01"))
      );
    });

    it("givenProjektnummer_thenSendItAsSearchCriterion", async () => {
      // given
      const fetchSpy = stubFetch();
      const { load } = useProjekteListe();

      // when
      await load(1, 10, { projektnummer: "2026-" });

      // then
      expect(requestedUrl(fetchSpy)).toContain("projektnummer=2026-");
    });

    it("givenDates_thenSendThemAsIsoDates", async () => {
      // given
      const fetchSpy = stubFetch();
      const { load } = useProjekteListe();

      // when
      await load(1, 10, {
        abrechnungBeginn: "2026-01-01",
        abrechnungEnde: "2026-03-31",
      });

      // then
      expect(requestedUrl(fetchSpy)).toContain("abrechnungBeginn=2026-01-01");
      expect(requestedUrl(fetchSpy)).toContain("abrechnungEnde=2026-03-31");
    });

    it("givenEmptyFilter_thenSendNoSearchCriteria", async () => {
      // given
      const fetchSpy = stubFetch();
      const { load } = useProjekteListe();

      // when
      await load(1, 10, {
        projektnummer: "",
        abrechnungBeginn: "",
        abrechnungEnde: "",
      });

      // then
      expect(requestedUrl(fetchSpy)).not.toContain("projektnummer");
      expect(requestedUrl(fetchSpy)).not.toContain("abrechnung");
    });

    it("givenSort_thenSendItAsSortParameters", async () => {
      // given
      const fetchSpy = stubFetch();
      const { load } = useProjekteListe();

      // when
      await load(
        1,
        10,
        {},
        { sortBy: "ABRECHNUNG_BEGINN", sortDirection: "ASC" }
      );

      // then
      expect(requestedUrl(fetchSpy)).toContain("sortBy=ABRECHNUNG_BEGINN");
      expect(requestedUrl(fetchSpy)).toContain("sortDirection=ASC");
    });

    it("givenNoSort_thenSendNoSortParametersSoTheBackendDefaultApplies", async () => {
      // given
      const fetchSpy = stubFetch();
      const { load } = useProjekteListe();

      // when
      await load(1, 10);

      // then
      expect(requestedUrl(fetchSpy)).not.toContain("sortBy");
      expect(requestedUrl(fetchSpy)).not.toContain("sortDirection");
    });

    it("givenFailingRequest_thenRethrowAndResetLoading", async () => {
      // given
      vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("offline")));
      const { load, loading } = useProjekteListe();

      // when
      const result = load(1, 10);

      // then
      await expect(result).rejects.toThrow();
      expect(loading.value).toBe(false);
    });
  });
});
