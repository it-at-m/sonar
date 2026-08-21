import type { ProjektRequestDTO } from "@/api/generated/sonar-backend";

import { afterEach, describe, expect, it, vi } from "vitest";

import { ApiFactory } from "@/api/ApiFactory";
import { ProjektControllerApi } from "@/api/generated/sonar-backend";

const REQUEST_DTO: ProjektRequestDTO = {
  projektnummer: "2026-0001",
  abrechnungBeginn: new Date("2026-01-01"),
  abrechnungEnde: new Date("2026-03-31"),
  adressen: [],
};

function stubFetch(): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockResolvedValue(
    new Response("{}", {
      status: 201,
      headers: { "Content-Type": "application/json" },
    })
  );
  vi.stubGlobal("fetch", fetchSpy);
  return fetchSpy;
}

function sentHeaders(fetchSpy: ReturnType<typeof vi.fn>): Headers {
  const init = fetchSpy.mock.calls[0]?.[1] as RequestInit;
  return new Headers(init.headers);
}

describe("ApiFactory.ts", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    // The XSRF token is read from document.cookie, which outlives the test that set it and would
    // otherwise leak into every later one sharing this environment.
    document.cookie = "XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT";
  });

  describe("getInstance", () => {
    it("givenRequestWithBody_thenSendSingleContentTypeHeader", async () => {
      // given
      const fetchSpy = stubFetch();

      // when
      await ApiFactory.getInstance(ProjektControllerApi).saveProjekt(
        REQUEST_DTO
      );

      // then
      expect(sentHeaders(fetchSpy).get("content-type")).toBe(
        "application/json"
      );
    });

    it("givenRequestWithOwnContentType_thenKeepContentTypeOfRequest", async () => {
      // given
      const fetchSpy = stubFetch();

      // when
      await ApiFactory.getInstance(ProjektControllerApi).saveProjekt(
        REQUEST_DTO,
        { headers: { "Content-Type": "multipart/form-data" } }
      );

      // then
      expect(sentHeaders(fetchSpy).get("content-type")).toBe(
        "multipart/form-data"
      );
    });

    it("givenXsrfCookie_thenSendXsrfTokenHeader", async () => {
      // given
      document.cookie = "XSRF-TOKEN=token-from-cookie";
      const fetchSpy = stubFetch();

      // when
      await ApiFactory.getInstance(ProjektControllerApi).saveProjekt(
        REQUEST_DTO
      );

      // then
      expect(sentHeaders(fetchSpy).get("x-xsrf-token")).toBe(
        "token-from-cookie"
      );
    });
  });
});
