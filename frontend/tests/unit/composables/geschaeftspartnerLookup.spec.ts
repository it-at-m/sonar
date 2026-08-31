import { afterEach, describe, expect, it, vi } from "vitest";

import { useGeschaeftspartnerLookup } from "@/composables/geschaeftspartnerLookup";

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
  describe("lookup", () => {
    it("givenKnownId_thenFillTheReadOnlyFields", async () => {
      stubFetch(200);
      const { daten, lookup } = useGeschaeftspartnerLookup();

      await lookup("GP-4711");

      expect(daten.value?.name1).toBe("Musterfirma");
      expect(daten.value?.ort).toBe("München");
    });

    it("givenId_thenRequestThatId", async () => {
      const fetchSpy = stubFetch(200);
      const { lookup } = useGeschaeftspartnerLookup();

      await lookup("GP-4711");

      expect(String(fetchSpy.mock.calls[0]?.[0])).toContain(
        "/geschaeftspartner/GP-4711"
      );
    });

    it("givenPaddedId_thenTrimItBeforeRequesting", async () => {
      const fetchSpy = stubFetch(200);
      const { lookup } = useGeschaeftspartnerLookup();

      await lookup("  GP-4711  ");

      expect(String(fetchSpy.mock.calls[0]?.[0])).toContain(
        "/geschaeftspartner/GP-4711"
      );
    });

    it("givenBlankId_thenClearWithoutRequesting", async () => {
      const fetchSpy = stubFetch(200);
      const { daten, fehlermeldung, lookup } = useGeschaeftspartnerLookup();

      await lookup("   ");

      expect(fetchSpy).not.toHaveBeenCalled();
      expect(daten.value).toBeNull();
      expect(fehlermeldung.value).toBe("");
    });

    it("givenUnknownId_thenReportNotFound", async () => {
      stubFetch(404, {});
      const { fehlermeldung, lookup } = useGeschaeftspartnerLookup();

      await lookup("GP-0000");

      expect(fehlermeldung.value).toBe(
        "Zu dieser ID wurden keine Geschäftspartnerdaten gefunden."
      );
    });

    it("givenServerError_thenReportFailure", async () => {
      stubFetch(502, {});
      const { fehlermeldung, lookup } = useGeschaeftspartnerLookup();

      await lookup("GP-4711");

      expect(fehlermeldung.value).toBe(
        "Die Geschäftspartnerdaten konnten nicht abgerufen werden."
      );
    });

    it("givenSecondLookup_thenResetThePreviousResult", async () => {
      stubFetch(200);
      const lookupState = useGeschaeftspartnerLookup();
      await lookupState.lookup("GP-4711");

      stubFetch(404, {});
      await lookupState.lookup("GP-0000");

      expect(lookupState.daten.value).toBeNull();
    });

    it("givenFinishedLookup_thenLoadingIsFalseAgain", async () => {
      stubFetch(200);
      const { loading, lookup } = useGeschaeftspartnerLookup();

      await lookup("GP-4711");

      expect(loading.value).toBe(false);
    });
  });
});
