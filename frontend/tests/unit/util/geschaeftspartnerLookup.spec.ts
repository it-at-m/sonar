import { afterEach, describe, expect, it, vi } from "vitest";

import { lookupGeschaeftspartner } from "@/util/geschaeftspartnerLookup";

const GESCHAEFTSPARTNER = {
  anrede: "Firma",
  name1: "Musterfirma",
  strasse: "Musterstraße",
  hausnummer: "1",
  postleitzahl: "80331",
  ort: "München",
};

function stubFetch(status: number, body: unknown = GESCHAEFTSPARTNER) {
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

describe("geschaeftspartnerLookup.ts", () => {
  describe("lookupGeschaeftspartner", () => {
    it("givenKnownId_thenReturnTheGeschaeftspartner", async () => {
      stubFetch(200);

      const result = await lookupGeschaeftspartner("GP-4711");

      expect(result.data?.name1).toBe("Musterfirma");
      expect(result.data?.ort).toBe("München");
      expect(result.errorMessage).toBe("");
    });

    it("givenId_thenRequestThatId", async () => {
      const fetchSpy = stubFetch(200);

      await lookupGeschaeftspartner("GP-4711");

      expect(String(fetchSpy.mock.calls[0]?.[0])).toContain(
        "/geschaeftspartner/GP-4711"
      );
    });

    it("givenUnknownId_thenReportNotFound", async () => {
      stubFetch(404, {});

      const result = await lookupGeschaeftspartner("GP-0000");

      expect(result.data).toBeNull();
      expect(result.errorMessage).toBe(
        "Zu dieser ID wurden keine Geschäftspartnerdaten gefunden."
      );
    });

    it("givenServerError_thenReportFailure", async () => {
      stubFetch(502, {});

      const result = await lookupGeschaeftspartner("GP-4711");

      expect(result.data).toBeNull();
      expect(result.errorMessage).toBe(
        "Die Geschäftspartnerdaten konnten nicht abgerufen werden."
      );
    });
  });
});
