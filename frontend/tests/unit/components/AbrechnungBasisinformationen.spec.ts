import type { AbrechnungForm } from "@/composables/abrechnungForm";
import type { VueWrapper } from "@vue/test-utils";

import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import AbrechnungBasisinformationen from "@/components/AbrechnungBasisinformationen.vue";
import GeschaeftspartnerStammdaten from "@/components/GeschaeftspartnerStammdaten.vue";
import vuetify from "@/plugins/vuetify";

const LOOKUP_DEBOUNCE_MS = 300;

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

function stubFetch() {
  const resolvers: ((response: Response) => void)[] = [];
  vi.stubGlobal(
    "fetch",
    vi.fn(() => new Promise<Response>((resolve) => resolvers.push(resolve)))
  );
  return {
    requestCount: () => resolvers.length,
    async respond(index: number, response: Response) {
      resolvers[index]?.(response);
      await flushPromises();
    },
  };
}

function mountBasisinformationen(abrechnung: Partial<AbrechnungForm> = {}) {
  return mount(AbrechnungBasisinformationen, {
    props: {
      modelValue: {
        geschaeftspartnerId: "",
        zustellungsbevollmaechtigterGenutzt: false,
        zustellungsbevollmaechtigterId: "",
        zustellungsbevollmaechtigterTyp: null,
        ...abrechnung,
      },
    },
    global: { plugins: [vuetify] },
  });
}

type Basisinformationen = ReturnType<typeof mountBasisinformationen>;

async function type(
  wrapper: Basisinformationen,
  eingabe: Partial<AbrechnungForm>
) {
  await wrapper.setProps({
    modelValue: { ...wrapper.props("modelValue"), ...eingabe },
  });
  await vi.advanceTimersByTimeAsync(LOOKUP_DEBOUNCE_MS);
}

function stammdaten(wrapper: Basisinformationen, index = 0): VueWrapper {
  const karten = wrapper.findAllComponents(GeschaeftspartnerStammdaten);
  const karte = karten[index];
  if (karte === undefined) {
    throw new Error(`Keine Stammdaten an Position ${index}.`);
  }
  return karte;
}

function idFeldFehler(wrapper: Basisinformationen, index = 0): unknown {
  const felder = wrapper.findAllComponents({ name: "v-text-field" });
  return felder[index]?.props("errorMessages");
}

beforeEach(() => vi.useFakeTimers());

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe("AbrechnungBasisinformationen.vue", () => {
  describe("Geschäftspartner:in Lookup", () => {
    it("givenKnownId_thenShowTheStammdaten", async () => {
      const fetchStub = stubFetch();
      const wrapper = mountBasisinformationen();

      await type(wrapper, { geschaeftspartnerId: "GP-4711" });
      await fetchStub.respond(0, jsonResponse(200));

      expect(stammdaten(wrapper).props("daten")).toMatchObject({
        name1: "Musterfirma",
      });
    });

    it("givenPaddedId_thenTrimItBeforeRequesting", async () => {
      stubFetch();
      const wrapper = mountBasisinformationen();

      await type(wrapper, { geschaeftspartnerId: "  GP-4711  " });

      expect(String(vi.mocked(fetch).mock.calls[0]?.[0])).toContain(
        "/geschaeftspartner/GP-4711"
      );
    });

    it("givenBlankId_thenClearWithoutRequesting", async () => {
      const fetchStub = stubFetch();
      const wrapper = mountBasisinformationen();

      await type(wrapper, { geschaeftspartnerId: "   " });

      expect(fetchStub.requestCount()).toBe(0);
      expect(stammdaten(wrapper).props("daten")).toBeNull();
      expect(idFeldFehler(wrapper)).toBe("");
    });

    it("givenUnknownId_thenReportItOnTheIdField", async () => {
      const fetchStub = stubFetch();
      const wrapper = mountBasisinformationen();

      await type(wrapper, { geschaeftspartnerId: "GP-0000" });
      await fetchStub.respond(0, jsonResponse(404, {}));

      expect(idFeldFehler(wrapper)).toBe(
        "Zu dieser ID wurden keine Geschäftspartnerdaten gefunden."
      );
    });

    it("givenRunningLookup_thenShowLoading", async () => {
      stubFetch();
      const wrapper = mountBasisinformationen();

      await type(wrapper, { geschaeftspartnerId: "GP-4711" });

      expect(stammdaten(wrapper).props("loading")).toBe(true);
    });

    it("givenFinishedLookup_thenLoadingIsFalseAgain", async () => {
      const fetchStub = stubFetch();
      const wrapper = mountBasisinformationen();

      await type(wrapper, { geschaeftspartnerId: "GP-4711" });
      await fetchStub.respond(0, jsonResponse(200));

      expect(stammdaten(wrapper).props("loading")).toBe(false);
    });

    it("givenSecondLookup_thenResetThePreviousResult", async () => {
      const fetchStub = stubFetch();
      const wrapper = mountBasisinformationen();
      await type(wrapper, { geschaeftspartnerId: "GP-4711" });
      await fetchStub.respond(0, jsonResponse(200));

      await type(wrapper, { geschaeftspartnerId: "GP-0815" });

      expect(stammdaten(wrapper).props("daten")).toBeNull();
    });

    it("givenOutdatedLookupAnswersLast_thenKeepTheNewestResult", async () => {
      const fetchStub = stubFetch();
      const wrapper = mountBasisinformationen();

      await type(wrapper, { geschaeftspartnerId: "GP-4711" });
      await type(wrapper, { geschaeftspartnerId: "GP-0815" });
      await fetchStub.respond(
        1,
        jsonResponse(200, { ...GESCHAEFTSPARTNER, name1: "Newest" })
      );
      await fetchStub.respond(
        0,
        jsonResponse(200, { ...GESCHAEFTSPARTNER, name1: "Outdated" })
      );

      expect(stammdaten(wrapper).props("daten")).toMatchObject({
        name1: "Newest",
      });
    });

    it("givenOutdatedLookupFailsLast_thenKeepTheNewestResult", async () => {
      const fetchStub = stubFetch();
      const wrapper = mountBasisinformationen();

      await type(wrapper, { geschaeftspartnerId: "GP-4711" });
      await type(wrapper, { geschaeftspartnerId: "GP-0815" });
      await fetchStub.respond(1, jsonResponse(200));
      await fetchStub.respond(0, jsonResponse(404, {}));

      expect(idFeldFehler(wrapper)).toBe("");
      expect(stammdaten(wrapper).props("daten")).toMatchObject({
        name1: "Musterfirma",
      });
    });

    it("givenOutdatedLookupAnswersFirst_thenStayLoading", async () => {
      const fetchStub = stubFetch();
      const wrapper = mountBasisinformationen();

      await type(wrapper, { geschaeftspartnerId: "GP-4711" });
      await type(wrapper, { geschaeftspartnerId: "GP-0815" });
      await fetchStub.respond(0, jsonResponse(200));

      expect(stammdaten(wrapper).props("loading")).toBe(true);

      await fetchStub.respond(1, jsonResponse(200));

      expect(stammdaten(wrapper).props("loading")).toBe(false);
    });

    it("givenBlankIdWhileLoading_thenStopLoading", async () => {
      const fetchStub = stubFetch();
      const wrapper = mountBasisinformationen();

      await type(wrapper, { geschaeftspartnerId: "GP-4711" });
      await type(wrapper, { geschaeftspartnerId: "   " });

      expect(stammdaten(wrapper).props("loading")).toBe(false);

      await fetchStub.respond(0, jsonResponse(200));

      expect(stammdaten(wrapper).props("loading")).toBe(false);
      expect(stammdaten(wrapper).props("daten")).toBeNull();
    });
  });

  describe("Zustellungsbevollmächtigte:r Lookup", () => {
    it("givenKnownId_thenShowTheStammdatenOfThatField", async () => {
      const fetchStub = stubFetch();
      const wrapper = mountBasisinformationen({
        zustellungsbevollmaechtigterGenutzt: true,
      });

      await type(wrapper, { zustellungsbevollmaechtigterId: "GP-0815" });
      await fetchStub.respond(0, jsonResponse(200));

      expect(stammdaten(wrapper).props("daten")).toBeNull();
      expect(stammdaten(wrapper, 1).props("daten")).toMatchObject({
        name1: "Musterfirma",
      });
    });

    it("givenUnknownId_thenReportItOnTheOwnIdField", async () => {
      const fetchStub = stubFetch();
      const wrapper = mountBasisinformationen({
        zustellungsbevollmaechtigterGenutzt: true,
      });

      await type(wrapper, { zustellungsbevollmaechtigterId: "GP-0000" });
      await fetchStub.respond(0, jsonResponse(404, {}));

      expect(idFeldFehler(wrapper)).toBe("");
      expect(idFeldFehler(wrapper, 1)).toBe(
        "Zu dieser ID wurden keine Geschäftspartnerdaten gefunden."
      );
    });
  });
});
