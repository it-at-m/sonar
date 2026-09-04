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

function jsonResponse(status: number, body: unknown = GESCHAEFTSPARTNER) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "content-type": "application/json" },
  });
}

function stubFetch(status: number, body: unknown = GESCHAEFTSPARTNER) {
  const fetchSpy = vi.fn(() => jsonResponse(status, body));
  vi.stubGlobal("fetch", fetchSpy);
  return fetchSpy;
}

function stubDeferredFetch() {
  const resolvers: ((response: Response) => void)[] = [];
  vi.stubGlobal(
    "fetch",
    vi.fn(() => new Promise<Response>((resolve) => resolvers.push(resolve)))
  );
  return {
    async waitForCalls(count: number) {
      await vi.waitFor(() => expect(resolvers).toHaveLength(count));
    },
    respond(index: number, response: Response) {
      resolvers[index]?.(response);
    },
  };
}

afterEach(() => vi.unstubAllGlobals());

describe("geschaeftspartnerLookup.ts", () => {
  describe("lookup", () => {
    it("givenKnownId_thenFillTheReadOnlyFields", async () => {
      stubFetch(200);
      const { data, lookup } = useGeschaeftspartnerLookup();

      await lookup("GP-4711");

      expect(data.value?.name1).toBe("Musterfirma");
      expect(data.value?.ort).toBe("München");
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
      const { data, errorMessage, lookup } = useGeschaeftspartnerLookup();

      await lookup("   ");

      expect(fetchSpy).not.toHaveBeenCalled();
      expect(data.value).toBeNull();
      expect(errorMessage.value).toBe("");
    });

    it("givenUnknownId_thenReportNotFound", async () => {
      stubFetch(404, {});
      const { errorMessage, lookup } = useGeschaeftspartnerLookup();

      await lookup("GP-0000");

      expect(errorMessage.value).toBe(
        "Zu dieser ID wurden keine Geschäftspartnerdaten gefunden."
      );
    });

    it("givenServerError_thenReportFailure", async () => {
      stubFetch(502, {});
      const { errorMessage, lookup } = useGeschaeftspartnerLookup();

      await lookup("GP-4711");

      expect(errorMessage.value).toBe(
        "Die Geschäftspartnerdaten konnten nicht abgerufen werden."
      );
    });

    it("givenSecondLookup_thenResetThePreviousResult", async () => {
      stubFetch(200);
      const lookupState = useGeschaeftspartnerLookup();
      await lookupState.lookup("GP-4711");

      stubFetch(404, {});
      await lookupState.lookup("GP-0000");

      expect(lookupState.data.value).toBeNull();
    });

    it("givenFinishedLookup_thenLoadingIsFalseAgain", async () => {
      stubFetch(200);
      const { loading, lookup } = useGeschaeftspartnerLookup();

      await lookup("GP-4711");

      expect(loading.value).toBe(false);
    });

    it("givenOutdatedLookupAnswersLast_thenKeepTheNewestResult", async () => {
      const fetchStub = stubDeferredFetch();
      const { data, lookup } = useGeschaeftspartnerLookup();

      const outdated = lookup("GP-4711");
      const newest = lookup("GP-0815");
      await fetchStub.waitForCalls(2);
      fetchStub.respond(
        1,
        jsonResponse(200, { ...GESCHAEFTSPARTNER, name1: "Newest" })
      );
      await newest;
      fetchStub.respond(
        0,
        jsonResponse(200, { ...GESCHAEFTSPARTNER, name1: "Outdated" })
      );
      await outdated;

      expect(data.value?.name1).toBe("Newest");
    });

    it("givenOutdatedLookupFailsLast_thenKeepTheNewestResult", async () => {
      const fetchStub = stubDeferredFetch();
      const { data, errorMessage, lookup } = useGeschaeftspartnerLookup();

      const outdated = lookup("GP-4711");
      const newest = lookup("GP-0815");
      await fetchStub.waitForCalls(2);
      fetchStub.respond(1, jsonResponse(200));
      await newest;
      fetchStub.respond(0, jsonResponse(404, {}));
      await outdated;

      expect(errorMessage.value).toBe("");
      expect(data.value?.name1).toBe("Musterfirma");
    });

    it("givenOutdatedLookupAnswersFirst_thenStayLoading", async () => {
      const fetchStub = stubDeferredFetch();
      const { loading, lookup } = useGeschaeftspartnerLookup();

      const outdated = lookup("GP-4711");
      const newest = lookup("GP-0815");
      await fetchStub.waitForCalls(2);
      fetchStub.respond(0, jsonResponse(200));
      await outdated;

      expect(loading.value).toBe(true);

      fetchStub.respond(1, jsonResponse(200));
      await newest;

      expect(loading.value).toBe(false);
    });

    it("givenBlankIdWhileLoading_thenStopLoading", async () => {
      const fetchStub = stubDeferredFetch();
      const { data, loading, lookup } = useGeschaeftspartnerLookup();

      const outdated = lookup("GP-4711");
      await fetchStub.waitForCalls(1);
      await lookup("   ");

      expect(loading.value).toBe(false);

      fetchStub.respond(0, jsonResponse(200));
      await outdated;

      expect(loading.value).toBe(false);
      expect(data.value).toBeNull();
    });
  });
});
