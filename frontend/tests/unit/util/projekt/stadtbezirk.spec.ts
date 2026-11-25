import { afterEach, describe, expect, it, vi } from "vitest";

import { ProjektRequestDTOStadtbezirkEnum } from "@/api/generated/sonar-backend";
import { fetchStadtbezirkOptionsOfCurrentUser } from "@/util/projekt/stadtbezirk";

function stubFetch(stadtbezirke: string[]): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockResolvedValue(
    new Response(JSON.stringify(stadtbezirke), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    })
  );
  vi.stubGlobal("fetch", fetchSpy);
  return fetchSpy;
}

function requestedUrl(fetchSpy: ReturnType<typeof vi.fn>): string {
  return fetchSpy.mock.calls[0]?.[0] as string;
}

describe("stadtbezirk.ts", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  describe("fetchStadtbezirkOptionsOfCurrentUser", () => {
    it("givenCall_thenRequestTheStadtbezirkeOfTheUser", async () => {
      const fetchSpy = stubFetch([]);

      await fetchStadtbezirkOptionsOfCurrentUser();

      expect(requestedUrl(fetchSpy)).toMatch(/\/stadtbezirk$/);
    });

    it("givenStadtbezirkeOfTheUser_thenOfferOnlyThoseInTheOrderOfTheirNumber", async () => {
      stubFetch([
        ProjektRequestDTOStadtbezirkEnum.BEZIRK_06_SENDLING,
        ProjektRequestDTOStadtbezirkEnum.BEZIRK_01_ALTSTADT_LEHEL,
      ]);

      const options = await fetchStadtbezirkOptionsOfCurrentUser();

      expect(options).toEqual([
        {
          title: "01 Altstadt-Lehel",
          value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_01_ALTSTADT_LEHEL,
        },
        {
          title: "06 Sendling",
          value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_06_SENDLING,
        },
      ]);
    });

    it("givenUserWithoutStadtbezirke_thenOfferNone", async () => {
      stubFetch([]);

      const options = await fetchStadtbezirkOptionsOfCurrentUser();

      expect(options).toEqual([]);
    });
  });
});
