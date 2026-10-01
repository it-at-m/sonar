import type { AbrechnungResponseDTO } from "@/api/generated/sonar-backend";

import { createTestingPinia } from "@pinia/testing";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import vuetify from "@/plugins/vuetify";
import { useSnackbarStore } from "@/stores/snackbar";
import AbrechnungAnsehenView from "@/views/AbrechnungAnsehenView.vue";

const PROJEKT_ID = "123e4567-e89b-12d3-a456-426614174000";
const ERSTE_VERSION_ID = "123e4567-e89b-12d3-a456-426614174001";
const ZWEITE_VERSION_ID = "123e4567-e89b-12d3-a456-426614174002";

const push = vi.fn();

vi.mock("vue-router", () => ({
  useRouter: () => ({ push }),
}));

function jsonResponse(body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
}

function stubFetch(
  geladeneAbrechnung: AbrechnungResponseDTO,
  versionen: { id: string; versionsnummer: number }[]
): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockImplementation((url: string) => {
    if (url.endsWith("/version")) {
      return jsonResponse(versionen);
    }
    return jsonResponse(geladeneAbrechnung);
  });
  vi.stubGlobal("fetch", fetchSpy);
  return fetchSpy;
}

function deferredFetch() {
  const pending: {
    url: string;
    resolve: (response: Response) => void;
    reject: (error: Error) => void;
  }[] = [];
  const fetchSpy = vi.fn().mockImplementation(
    (url: string) =>
      new Promise<Response>((resolve, reject) => {
        pending.push({ url, resolve, reject });
      })
  );
  vi.stubGlobal("fetch", fetchSpy);

  function take(urlSuffix: string) {
    const request = pending.find((candidate) =>
      candidate.url.endsWith(urlSuffix)
    );
    if (!request) {
      throw new Error(`No pending request for ${urlSuffix}`);
    }
    pending.splice(pending.indexOf(request), 1);
    return request;
  }

  return {
    fetchSpy,
    async resolveRequest(urlSuffix: string, body: unknown) {
      take(urlSuffix).resolve(jsonResponse(body));
      await flushPromises();
    },
    async rejectRequest(urlSuffix: string) {
      take(urlSuffix).reject(new Error("offline"));
      await flushPromises();
    },
  };
}

async function mountView(
  fetchSpy: ReturnType<typeof vi.fn>,
  abrechnungId: string
) {
  const wrapper = mount(AbrechnungAnsehenView, {
    props: { abrechnungId, projektId: PROJEKT_ID },
    global: {
      plugins: [
        vuetify,
        createTestingPinia({ createSpy: vi.fn, stubActions: false }),
      ],
      stubs: { AbrechnungTabs: true },
    },
  });
  await flushPromises();
  return { fetchSpy, wrapper };
}

describe("AbrechnungAnsehenView.vue", () => {
  beforeEach(() => {
    vi.stubGlobal(
      "ResizeObserver",
      class {
        observe = vi.fn();
        unobserve = vi.fn();
        disconnect = vi.fn();
      }
    );
    push.mockClear();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("givenAbrechnungWithSeveralVersionen_thenOfferThemInADropdown", async () => {
    const zweiteVersion: AbrechnungResponseDTO = {
      id: ZWEITE_VERSION_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 2,
      geschaeftspartnerId: "1000000001",
      zustellungsbevollmaechtigterGenutzt: false,
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      widerspruchVorhanden: false,
      neuereVersionVorhanden: false,
      nutzungsobjekte: [],
    };
    const { wrapper } = await mountView(
      stubFetch(zweiteVersion, [
        { id: ERSTE_VERSION_ID, versionsnummer: 1 },
        { id: ZWEITE_VERSION_ID, versionsnummer: 2 },
      ]),
      ZWEITE_VERSION_ID
    );

    const select = wrapper.findComponent({ name: "VSelect" });
    expect(select.props("items")).toEqual([
      { title: "Version 2 (aktuell)", value: ZWEITE_VERSION_ID },
      { title: "Version 1", value: ERSTE_VERSION_ID },
    ]);
  });

  it("givenDisplayedVersion_thenPreselectItInTheDropdown", async () => {
    const ersteVersion: AbrechnungResponseDTO = {
      id: ERSTE_VERSION_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      geschaeftspartnerId: "1000000001",
      zustellungsbevollmaechtigterGenutzt: false,
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      widerspruchVorhanden: false,
      neuereVersionVorhanden: true,
      nutzungsobjekte: [],
    };
    const { wrapper } = await mountView(
      stubFetch(ersteVersion, [
        { id: ERSTE_VERSION_ID, versionsnummer: 1 },
        { id: ZWEITE_VERSION_ID, versionsnummer: 2 },
      ]),
      ERSTE_VERSION_ID
    );

    expect(wrapper.findComponent({ name: "VSelect" }).props("modelValue")).toBe(
      ERSTE_VERSION_ID
    );
  });

  it("givenSingleVersion_thenShowNoDropdown", async () => {
    const einzigeVersion: AbrechnungResponseDTO = {
      id: ERSTE_VERSION_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      geschaeftspartnerId: "1000000001",
      zustellungsbevollmaechtigterGenutzt: false,
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      widerspruchVorhanden: false,
      neuereVersionVorhanden: false,
      nutzungsobjekte: [],
    };
    const { wrapper } = await mountView(
      stubFetch(einzigeVersion, [{ id: ERSTE_VERSION_ID, versionsnummer: 1 }]),
      ERSTE_VERSION_ID
    );

    expect(wrapper.findComponent({ name: "VSelect" }).exists()).toBe(false);
  });

  it("givenPickedVersion_thenNavigateToIt", async () => {
    const zweiteVersion: AbrechnungResponseDTO = {
      id: ZWEITE_VERSION_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 2,
      geschaeftspartnerId: "1000000001",
      zustellungsbevollmaechtigterGenutzt: false,
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      widerspruchVorhanden: false,
      neuereVersionVorhanden: false,
      nutzungsobjekte: [],
    };
    const { wrapper } = await mountView(
      stubFetch(zweiteVersion, [
        { id: ERSTE_VERSION_ID, versionsnummer: 1 },
        { id: ZWEITE_VERSION_ID, versionsnummer: 2 },
      ]),
      ZWEITE_VERSION_ID
    );

    await wrapper
      .findComponent({ name: "VSelect" })
      .vm.$emit("update:modelValue", ERSTE_VERSION_ID);

    expect(push).toHaveBeenCalledWith(
      `/projekte/${PROJEKT_ID}/abrechnungen/${ERSTE_VERSION_ID}/ansehen`
    );
  });

  it("givenChangedAbrechnungIdInsideTheChain_thenReloadTheAbrechnungWithoutReloadingTheVersionen", async () => {
    const zweiteVersion: AbrechnungResponseDTO = {
      id: ZWEITE_VERSION_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 2,
      geschaeftspartnerId: "1000000001",
      zustellungsbevollmaechtigterGenutzt: false,
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      widerspruchVorhanden: false,
      neuereVersionVorhanden: false,
      nutzungsobjekte: [],
    };
    const { fetchSpy, wrapper } = await mountView(
      stubFetch(zweiteVersion, [
        { id: ERSTE_VERSION_ID, versionsnummer: 1 },
        { id: ZWEITE_VERSION_ID, versionsnummer: 2 },
      ]),
      ZWEITE_VERSION_ID
    );
    const versionRequestsBefore = fetchSpy.mock.calls.filter((call) =>
      String(call[0]).endsWith("/version")
    ).length;

    await wrapper.setProps({ abrechnungId: ERSTE_VERSION_ID });
    await flushPromises();

    const versionRequestsAfter = fetchSpy.mock.calls.filter((call) =>
      String(call[0]).endsWith("/version")
    ).length;
    expect(versionRequestsAfter).toBe(versionRequestsBefore);
    expect(
      fetchSpy.mock.calls.some((call) =>
        String(call[0]).endsWith(`/abrechnung/${ERSTE_VERSION_ID}`)
      )
    ).toBe(true);
  });

  it("givenAbrechnungIdOfAnotherChain_thenReloadTheVersionenToo", async () => {
    const andereAbrechnungId = "123e4567-e89b-12d3-a456-426614174009";
    const zweiteVersion: AbrechnungResponseDTO = {
      id: ZWEITE_VERSION_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 2,
      geschaeftspartnerId: "1000000001",
      zustellungsbevollmaechtigterGenutzt: false,
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      widerspruchVorhanden: false,
      neuereVersionVorhanden: false,
      nutzungsobjekte: [],
    };
    const { fetchSpy, wrapper } = await mountView(
      stubFetch(zweiteVersion, [
        { id: ERSTE_VERSION_ID, versionsnummer: 1 },
        { id: ZWEITE_VERSION_ID, versionsnummer: 2 },
      ]),
      ZWEITE_VERSION_ID
    );

    await wrapper.setProps({ abrechnungId: andereAbrechnungId });
    await flushPromises();

    expect(
      fetchSpy.mock.calls.some((call) =>
        String(call[0]).endsWith(`/abrechnung/${andereAbrechnungId}/version`)
      )
    ).toBe(true);
  });

  it("givenFailingVersionenRequest_thenReportItAndStillShowTheAbrechnung", async () => {
    const zweiteVersion: AbrechnungResponseDTO = {
      id: ZWEITE_VERSION_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 2,
      geschaeftspartnerId: "1000000001",
      zustellungsbevollmaechtigterGenutzt: false,
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      widerspruchVorhanden: false,
      neuereVersionVorhanden: false,
      nutzungsobjekte: [],
    };
    const fetchSpy = vi.fn().mockImplementation((url: string) => {
      if (url.endsWith("/version")) {
        return Promise.reject(new Error("offline"));
      }
      return jsonResponse(zweiteVersion);
    });
    vi.stubGlobal("fetch", fetchSpy);

    const { wrapper } = await mountView(fetchSpy, ZWEITE_VERSION_ID);

    expect(wrapper.findComponent({ name: "VSelect" }).exists()).toBe(false);
    expect(wrapper.text()).toContain("Abrechnung, Version 2");
    expect(useSnackbarStore().queue).toHaveLength(1);
  });

  it("givenNeuereVersionVorhanden_thenPointItOut", async () => {
    const ersteVersion: AbrechnungResponseDTO = {
      id: ERSTE_VERSION_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      geschaeftspartnerId: "1000000001",
      zustellungsbevollmaechtigterGenutzt: false,
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      widerspruchVorhanden: false,
      neuereVersionVorhanden: true,
      nutzungsobjekte: [],
    };
    const { wrapper } = await mountView(
      stubFetch(ersteVersion, [
        { id: ERSTE_VERSION_ID, versionsnummer: 1 },
        { id: ZWEITE_VERSION_ID, versionsnummer: 2 },
      ]),
      ERSTE_VERSION_ID
    );

    expect(wrapper.text()).toContain(
      "Zu dieser Abrechnung besteht eine neuere Version."
    );
  });

  it("givenFailingAbrechnungRequest_thenReportItAndReturnToTheAbrechnungen", async () => {
    const fetchSpy = vi.fn().mockRejectedValue(new Error("offline"));
    vi.stubGlobal("fetch", fetchSpy);

    await mountView(fetchSpy, ZWEITE_VERSION_ID);

    expect(push).toHaveBeenCalledWith(`/projekte/${PROJEKT_ID}/abrechnungen`);
  });

  it("givenLateAnswerOfASupersededVersion_thenKeepTheShownVersion", async () => {
    const ersteVersion: AbrechnungResponseDTO = {
      id: ERSTE_VERSION_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      geschaeftspartnerId: "1000000001",
      zustellungsbevollmaechtigterGenutzt: false,
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      widerspruchVorhanden: false,
      neuereVersionVorhanden: true,
      nutzungsobjekte: [],
    };
    const zweiteVersion: AbrechnungResponseDTO = {
      id: ZWEITE_VERSION_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 2,
      geschaeftspartnerId: "1000000002",
      zustellungsbevollmaechtigterGenutzt: false,
      zeitraumVon: new Date("2026-04-01"),
      zeitraumBis: new Date("2026-06-30"),
      abrechnungsArt: "ENDABRECHNUNG",
      widerspruchVorhanden: false,
      neuereVersionVorhanden: false,
      nutzungsobjekte: [],
    };
    const { fetchSpy, resolveRequest } = deferredFetch();
    const { wrapper } = await mountView(fetchSpy, ERSTE_VERSION_ID);

    await wrapper.setProps({ abrechnungId: ZWEITE_VERSION_ID });
    await flushPromises();
    await resolveRequest(`/abrechnung/${ZWEITE_VERSION_ID}`, zweiteVersion);
    await resolveRequest(`/abrechnung/${ERSTE_VERSION_ID}`, ersteVersion);

    expect(wrapper.text()).toContain("Abrechnung, Version 2");
    expect(
      wrapper.findComponent({ name: "AbrechnungTabs" }).props("modelValue")
    ).toMatchObject({ geschaeftspartnerId: "1000000002" });
  });

  it("givenLateFailureOfASupersededVersion_thenStayOnTheShownVersion", async () => {
    const zweiteVersion: AbrechnungResponseDTO = {
      id: ZWEITE_VERSION_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 2,
      geschaeftspartnerId: "1000000002",
      zustellungsbevollmaechtigterGenutzt: false,
      zeitraumVon: new Date("2026-04-01"),
      zeitraumBis: new Date("2026-06-30"),
      abrechnungsArt: "ENDABRECHNUNG",
      widerspruchVorhanden: false,
      neuereVersionVorhanden: false,
      nutzungsobjekte: [],
    };
    const { fetchSpy, rejectRequest, resolveRequest } = deferredFetch();
    const { wrapper } = await mountView(fetchSpy, ERSTE_VERSION_ID);

    await wrapper.setProps({ abrechnungId: ZWEITE_VERSION_ID });
    await flushPromises();
    await resolveRequest(`/abrechnung/${ZWEITE_VERSION_ID}`, zweiteVersion);
    await rejectRequest(`/abrechnung/${ERSTE_VERSION_ID}`);

    expect(push).not.toHaveBeenCalled();
    expect(useSnackbarStore().queue).toHaveLength(0);
    expect(wrapper.text()).toContain("Abrechnung, Version 2");
  });
});
