import type { AbrechnungVersionResponseDTO } from "@/api/generated/sonar-backend";

import { afterEach, describe, expect, it, vi } from "vitest";

import {
  abrechnungVersionLabel,
  abrechnungVersionOptions,
  fetchAbrechnungVersionen,
} from "@/util/abrechnung/abrechnungVersionen";

const PROJEKT_ID = "123e4567-e89b-12d3-a456-426614174000";
const ABRECHNUNG_ID = "123e4567-e89b-12d3-a456-426614174001";

function stubFetch(
  versionen: AbrechnungVersionResponseDTO[]
): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockResolvedValue(
    new Response(JSON.stringify(versionen), {
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

describe("abrechnungVersionen.ts", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  describe("fetchAbrechnungVersionen", () => {
    it("givenAbrechnungId_thenRequestTheVersionenOfThatAbrechnung", async () => {
      const fetchSpy = stubFetch([{ id: ABRECHNUNG_ID, versionsnummer: 1 }]);

      await fetchAbrechnungVersionen(PROJEKT_ID, ABRECHNUNG_ID);

      expect(requestedUrl(fetchSpy)).toContain(
        `/projekt/${PROJEKT_ID}/abrechnung/${ABRECHNUNG_ID}/version`
      );
    });

    it("givenResponse_thenReturnTheVersionenNewestFirst", async () => {
      stubFetch([
        { id: "id-1", versionsnummer: 1 },
        { id: "id-2", versionsnummer: 2 },
      ]);

      const versionen = await fetchAbrechnungVersionen(
        PROJEKT_ID,
        ABRECHNUNG_ID
      );

      expect(versionen.map((version) => version.id)).toEqual(["id-2", "id-1"]);
      expect(versionen[0]?.aktuell).toBe(true);
    });

    it("givenFailingRequest_thenFailSoTheViewCanReportIt", async () => {
      vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("offline")));

      await expect(
        fetchAbrechnungVersionen(PROJEKT_ID, ABRECHNUNG_ID)
      ).rejects.toThrow();
    });
  });

  describe("abrechnungVersionLabel", () => {
    it("givenAktuelleVersion_thenAppendAktuell", () => {
      expect(
        abrechnungVersionLabel({ id: "id-3", versionsnummer: 3, aktuell: true })
      ).toBe("Version 3 (aktuell)");
    });

    it("givenAeltereVersion_thenNameOnlyTheVersionsnummer", () => {
      expect(
        abrechnungVersionLabel({
          id: "id-2",
          versionsnummer: 2,
          aktuell: false,
        })
      ).toBe("Version 2");
    });
  });

  describe("abrechnungVersionOptions", () => {
    it("givenVersionen_thenUseTheAbrechnungIdAsValue", () => {
      const options = abrechnungVersionOptions([
        { id: "id-2", versionsnummer: 2, aktuell: true },
        { id: "id-1", versionsnummer: 1, aktuell: false },
      ]);

      expect(options).toEqual([
        { title: "Version 2 (aktuell)", value: "id-2" },
        { title: "Version 1", value: "id-1" },
      ]);
    });
  });
});
