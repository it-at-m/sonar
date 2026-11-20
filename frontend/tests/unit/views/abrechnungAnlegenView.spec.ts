import type { AbrechnungResponseDTO } from "@/api/generated/sonar-backend";
import type { AbrechnungForm } from "@/types/abrechnung/AbrechnungForm";

import { createTestingPinia } from "@pinia/testing";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { AbrechnungRequestDTOAbrechnungsArtEnum } from "@/api/generated/sonar-backend";
import BerechnungDialog from "@/components/berechnung/BerechnungDialog.vue";
import BerechnungErgebnisDialog from "@/components/berechnung/BerechnungErgebnisDialog.vue";
import vuetify from "@/plugins/vuetify";
import { useSnackbarStore } from "@/stores/snackbar";
import { createAbrechnungNutzungsobjekt } from "@/util/abrechnung/abrechnungNutzungsobjektForm";
import AbrechnungAnlegenView from "@/views/AbrechnungAnlegenView.vue";

const PROJEKT_ID = "123e4567-e89b-12d3-a456-426614174000";
const ENTWURF_ID = "123e4567-e89b-12d3-a456-426614174001";
const VORGAENGER_ID = "123e4567-e89b-12d3-a456-426614174002";
const ABRECHNUNGEN_PATH = `/projekte/${PROJEKT_ID}/abrechnungen`;

const push = vi.fn();

vi.mock("vue-router", () => ({
  onBeforeRouteLeave: vi.fn(),
  useRouter: () => ({ push }),
}));

function jsonResponse(body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
}

function stubFetch(
  abrechnung: AbrechnungResponseDTO
): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockImplementation((url: string) => {
    if (url.includes("/berechnung/preview")) {
      return jsonResponse({
        lfdNr: 1,
        abrechnungszeitraumVon: "2026-01-01",
        abrechnungszeitraumBis: "2026-03-31",
        zusammenfassung: "",
        berechnungslog: "",
        gebuehrFlaechen: 0,
        gebuehrUeberspannungen: 0,
        gebuehrVerwaltung: 0,
        gebuehrGesamt: 0,
        gebuehrZahlung: 0,
        bescheiddatenFlaechen: [],
        abrechnungenUeberspannungen: [],
      });
    }
    if (url.endsWith("/finalize")) {
      return jsonResponse({ ...abrechnung, draft: false });
    }
    if (url.endsWith("/abrechnung/nutzungsobjekt")) {
      return jsonResponse([]);
    }
    if (url.includes("/geschaeftspartner/")) {
      return jsonResponse({ name1: "Bau GmbH" });
    }
    if (url.includes("/abrechnung")) {
      return jsonResponse(abrechnung);
    }
    return jsonResponse({ projektnummer: "P-0001", adressen: [] });
  });
  vi.stubGlobal("fetch", fetchSpy);
  return fetchSpy;
}

function lastRequestBody(
  fetchSpy: ReturnType<typeof vi.fn>,
  method: string
): unknown {
  const init = fetchSpy.mock.calls
    .map(([, requestInit]) => requestInit as RequestInit | undefined)
    .filter((requestInit) => requestInit?.method === method)
    .at(-1);
  return JSON.parse(String(init?.body));
}

function requests(
  fetchSpy: ReturnType<typeof vi.fn>
): { url: string; method: string }[] {
  return fetchSpy.mock.calls.map(([url, init]) => ({
    url: url as string,
    method: (init as RequestInit | undefined)?.method ?? "GET",
  }));
}

async function mountView(props: {
  vorgaengerAbrechnungId?: string;
  entwurfId?: string;
}) {
  const wrapper = mount(AbrechnungAnlegenView, {
    props: { projektId: PROJEKT_ID, ...props },
    attachTo: document.body,
    global: {
      plugins: [
        vuetify,
        createTestingPinia({ createSpy: vi.fn, stubActions: false }),
      ],
      stubs: { AbrechnungTabs: true, YesNoDialog: true },
    },
  });
  await flushPromises();
  return wrapper;
}

async function enter(
  wrapper: Awaited<ReturnType<typeof mountView>>,
  abrechnung: AbrechnungForm
): Promise<void> {
  wrapper
    .findComponent({ name: "AbrechnungTabs" })
    .vm.$emit("update:modelValue", abrechnung);
  await flushPromises();
}

async function save(
  wrapper: Awaited<ReturnType<typeof mountView>>
): Promise<void> {
  await wrapper.find("form").trigger("submit");
  await flushPromises();
}

async function calculate(): Promise<void> {
  document
    .querySelector("#berechnung-abrechnungsdatum")
    ?.closest("form")
    ?.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
  await flushPromises();
}

async function clickButton(text: string): Promise<void> {
  [...document.querySelectorAll<HTMLElement>("button")]
    .find((button) => button.textContent?.trim() === text)
    ?.click();
  await flushPromises();
}

describe("AbrechnungAnlegenView.vue", () => {
  beforeEach(() => {
    push.mockClear();
    vi.stubGlobal(
      "ResizeObserver",
      class {
        observe = vi.fn();
        unobserve = vi.fn();
        disconnect = vi.fn();
      }
    );
    vi.stubGlobal("visualViewport", {
      width: 1024,
      height: 768,
      offsetLeft: 0,
      offsetTop: 0,
      scale: 1,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
    });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    document.body.innerHTML = "";
  });

  it("givenNewAbrechnung_whenSaved_thenStoreItAsEntwurfAndOpenTheBerechnung", async () => {
    const fetchSpy = stubFetch({
      id: ENTWURF_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      draft: true,
      geschaeftspartnerId: "1000000001",
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      nutzungsobjekte: [],
    });
    const wrapper = await mountView({});
    await enter(wrapper, {
      geschaeftspartnerId: "1000000001",
      zustellungsbevollmaechtigterGenutzt: false,
      zustellungsbevollmaechtigterId: "",
      zustellungsbevollmaechtigterTyp: null,
      zeitraumVon: "2026-01-01",
      zeitraumBis: "2026-03-31",
      abrechnungsArt: AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG,
      nutzungsobjekte: [
        {
          ...createAbrechnungNutzungsobjekt(),
          positionen: [
            {
              key: "position",
              id: null,
              beginn: "2026-01-01",
              ende: "2026-03-31",
              laenge: 12,
              breite: 3,
              flaeche: 36,
            },
          ],
        },
      ],
    });

    await save(wrapper);

    expect(requests(fetchSpy)).toContainEqual({
      url: expect.stringMatching(
        new RegExp(`/projekt/${PROJEKT_ID}/abrechnung$`)
      ),
      method: "POST",
    });
    const berechnungDialog = wrapper.findComponent(BerechnungDialog);
    expect(berechnungDialog.props("modelValue")).toBe(true);
    expect(berechnungDialog.props("abrechnungId")).toBe(ENTWURF_ID);
    expect(push).not.toHaveBeenCalled();
  });

  it("givenSaveInProgress_thenLockTheFormUntilTheResponseArrives", async () => {
    const entwurf: AbrechnungResponseDTO = {
      id: ENTWURF_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      draft: true,
      geschaeftspartnerId: "1000000001",
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      nutzungsobjekte: [],
    };
    const fetchSpy = stubFetch(entwurf);
    const answer = fetchSpy.getMockImplementation();
    const saveResponse = Promise.withResolvers<Response>();
    fetchSpy.mockImplementation((url: string, init?: RequestInit) =>
      init?.method === "POST" ? saveResponse.promise : answer?.(url, init)
    );
    const wrapper = await mountView({});
    await enter(wrapper, {
      geschaeftspartnerId: "1000000001",
      zustellungsbevollmaechtigterGenutzt: false,
      zustellungsbevollmaechtigterId: "",
      zustellungsbevollmaechtigterTyp: null,
      zeitraumVon: "2026-01-01",
      zeitraumBis: "2026-03-31",
      abrechnungsArt: AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG,
      nutzungsobjekte: [
        {
          ...createAbrechnungNutzungsobjekt(),
          positionen: [
            {
              key: "position",
              id: null,
              beginn: "2026-01-01",
              ende: "2026-03-31",
              laenge: 12,
              breite: 3,
              flaeche: 36,
            },
          ],
        },
      ],
    });
    const tabsContainer = wrapper.findComponent({ name: "AbrechnungTabs" })
      .element.parentElement;

    await save(wrapper);

    expect(tabsContainer?.hasAttribute("inert")).toBe(true);

    saveResponse.resolve(jsonResponse(entwurf));
    await flushPromises();

    expect(tabsContainer?.hasAttribute("inert")).toBe(false);
  });

  it("givenNewAbrechnung_whenSavedTwice_thenSendTheSavedIdsTheSecondTime", async () => {
    const fetchSpy = stubFetch({
      id: ENTWURF_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      draft: true,
      geschaeftspartnerId: "1000000001",
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      nutzungsobjekte: [
        {
          art: "ADRESSE",
          adresse: "Marienplatz",
          hausnummerVon: "8",
          nutzungen: ["BAUZAUN"],
          aufschlag50prozent: false,
          positionen: [
            {
              id: "123e4567-e89b-12d3-a456-426614174011",
              beginn: new Date("2026-01-01"),
              ende: new Date("2026-03-31"),
              laenge: 12,
              breite: 3,
              flaeche: 36,
            },
          ],
          masten: [],
        },
      ],
    });
    const wrapper = await mountView({});
    await enter(wrapper, {
      geschaeftspartnerId: "1000000001",
      zustellungsbevollmaechtigterGenutzt: false,
      zustellungsbevollmaechtigterId: "",
      zustellungsbevollmaechtigterTyp: null,
      zeitraumVon: "2026-01-01",
      zeitraumBis: "2026-03-31",
      abrechnungsArt: AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG,
      nutzungsobjekte: [
        {
          ...createAbrechnungNutzungsobjekt(),
          positionen: [
            {
              key: "position",
              id: null,
              beginn: "2026-01-01",
              ende: "2026-03-31",
              laenge: 12,
              breite: 3,
              flaeche: 36,
            },
          ],
        },
      ],
    });

    await save(wrapper);
    const ersteFassung = lastRequestBody(fetchSpy, "POST");
    await save(wrapper);
    const zweiteFassung = lastRequestBody(fetchSpy, "PUT");

    expect(ersteFassung).not.toHaveProperty([
      "nutzungsobjekte",
      0,
      "positionen",
      0,
      "id",
    ]);
    expect(zweiteFassung).toMatchObject({
      nutzungsobjekte: [
        { positionen: [{ id: "123e4567-e89b-12d3-a456-426614174011" }] },
      ],
    });
  });

  it("givenEntwurfId_whenSaved_thenSendTheIdsOfItsPositionenAndMasten", async () => {
    const fetchSpy = stubFetch({
      id: ENTWURF_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      draft: true,
      geschaeftspartnerId: "1000000001",
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      nutzungsobjekte: [
        {
          art: "ADRESSE",
          adresse: "Marienplatz",
          hausnummerVon: "8",
          nutzungen: ["BAUZAUN"],
          aufschlag50prozent: false,
          positionen: [
            {
              id: "123e4567-e89b-12d3-a456-426614174011",
              beginn: new Date("2026-01-01"),
              ende: new Date("2026-03-31"),
              laenge: 12,
              breite: 3,
              flaeche: 36,
            },
          ],
          masten: [
            {
              id: "123e4567-e89b-12d3-a456-426614174021",
              beginn: new Date("2026-01-01"),
              ende: new Date("2026-01-31"),
            },
          ],
        },
      ],
    });
    const wrapper = await mountView({ entwurfId: ENTWURF_ID });

    await save(wrapper);

    expect(lastRequestBody(fetchSpy, "PUT")).toMatchObject({
      nutzungsobjekte: [
        {
          positionen: [{ id: "123e4567-e89b-12d3-a456-426614174011" }],
          masten: [{ id: "123e4567-e89b-12d3-a456-426614174021" }],
        },
      ],
    });
  });

  it("givenEntwurfWithTakenOverAndOwnNutzungsobjekt_thenKeepTheUebernahmeAndSendBothIds", async () => {
    const fetchSpy = stubFetch({
      id: ENTWURF_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      draft: true,
      geschaeftspartnerId: "1000000001",
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      nutzungsobjekte: [
        {
          id: "7c6b5a4d-3e2f-4a1b-9c8d-7e6f5a4b3c2d",
          changeable: false,
          art: "ADRESSE",
          adresse: "Marienplatz",
          hausnummerVon: "8",
          nutzungen: ["BAUZAUN"],
          aufschlag50prozent: false,
          positionen: [
            {
              beginn: new Date("2026-01-01"),
              ende: new Date("2026-03-31"),
              flaeche: 36,
            },
          ],
          masten: [],
        },
        {
          id: "123e4567-e89b-12d3-a456-426614174031",
          changeable: true,
          art: "ADRESSE",
          adresse: "Sendlinger Straße",
          hausnummerVon: "1",
          nutzungen: ["CONTAINER"],
          aufschlag50prozent: false,
          positionen: [
            {
              beginn: new Date("2026-01-01"),
              ende: new Date("2026-03-31"),
              flaeche: 45,
            },
          ],
          masten: [],
        },
      ],
    });
    const wrapper = await mountView({ entwurfId: ENTWURF_ID });

    await save(wrapper);

    expect(
      wrapper.findComponent({ name: "AbrechnungTabs" }).props("modelValue")
    ).toMatchObject({
      nutzungsobjekte: [
        {
          uebernahme: {
            quelle: "NUTZUNGSOBJEKT",
            nutzungsobjektId: "7c6b5a4d-3e2f-4a1b-9c8d-7e6f5a4b3c2d",
          },
        },
        { uebernahme: null, id: "123e4567-e89b-12d3-a456-426614174031" },
      ],
    });
    expect(lastRequestBody(fetchSpy, "PUT")).toMatchObject({
      nutzungsobjekte: [
        { takenOverId: "7c6b5a4d-3e2f-4a1b-9c8d-7e6f5a4b3c2d" },
        { id: "123e4567-e89b-12d3-a456-426614174031" },
      ],
    });
  });

  it("givenVorgaenger_whenSaved_thenStoreTheNewVersionAsEntwurf", async () => {
    const fetchSpy = stubFetch({
      id: VORGAENGER_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      draft: false,
      geschaeftspartnerId: "1000000001",
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      nutzungsobjekte: [
        {
          art: "ADRESSE",
          adresse: "Marienplatz",
          hausnummerVon: "8",
          nutzungen: ["BAUZAUN"],
          aufschlag50prozent: false,
          positionen: [
            {
              beginn: new Date("2026-01-01"),
              ende: new Date("2026-03-31"),
              laenge: 12,
              breite: 3,
              flaeche: 36,
            },
          ],
          masten: [],
        },
      ],
    });
    const wrapper = await mountView({ vorgaengerAbrechnungId: VORGAENGER_ID });

    await save(wrapper);

    expect(requests(fetchSpy)).toContainEqual({
      url: expect.stringContaining(
        `/projekt/${PROJEKT_ID}/abrechnung/${VORGAENGER_ID}/version`
      ),
      method: "POST",
    });
    expect(wrapper.findComponent(BerechnungDialog).props("modelValue")).toBe(
      true
    );
  });

  it("givenEntwurfId_thenFillTheFormWithTheEntwurf", async () => {
    stubFetch({
      id: ENTWURF_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      draft: true,
      geschaeftspartnerId: "1000000007",
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      nutzungsobjekte: [],
    });

    const wrapper = await mountView({ entwurfId: ENTWURF_ID });

    expect(wrapper.find("h1").text()).toBe("Entwurf der Abrechnung bearbeiten");
    expect(
      wrapper.findComponent({ name: "AbrechnungTabs" }).props("modelValue")
    ).toMatchObject({ geschaeftspartnerId: "1000000007" });
  });

  it("givenEntwurfIdOfAFinalizedAbrechnung_thenReturnToTheAbrechnungen", async () => {
    stubFetch({
      id: ENTWURF_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      draft: false,
      geschaeftspartnerId: "1000000001",
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      nutzungsobjekte: [],
    });

    await mountView({ entwurfId: ENTWURF_ID });

    expect(useSnackbarStore().queue.at(-1)?.text).toBe(
      "Die Abrechnung ist bereits abgeschlossen und lässt sich nicht mehr ändern."
    );
    expect(push).toHaveBeenCalledWith(ABRECHNUNGEN_PATH);
  });

  it("givenStoredEntwurf_whenSavedAgain_thenUpdateItInsteadOfCreatingAnother", async () => {
    const fetchSpy = stubFetch({
      id: ENTWURF_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      draft: true,
      geschaeftspartnerId: "1000000001",
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      nutzungsobjekte: [
        {
          art: "ADRESSE",
          adresse: "Marienplatz",
          hausnummerVon: "8",
          nutzungen: ["BAUZAUN"],
          aufschlag50prozent: false,
          positionen: [
            {
              beginn: new Date("2026-01-01"),
              ende: new Date("2026-03-31"),
              laenge: 12,
              breite: 3,
              flaeche: 36,
            },
          ],
          masten: [],
        },
      ],
    });
    const wrapper = await mountView({ entwurfId: ENTWURF_ID });

    await save(wrapper);
    await save(wrapper);

    const saves = requests(fetchSpy).filter(
      (request) => request.method !== "GET"
    );
    expect(saves).toEqual([
      {
        url: expect.stringContaining(
          `/projekt/${PROJEKT_ID}/abrechnung/${ENTWURF_ID}`
        ),
        method: "PUT",
      },
      {
        url: expect.stringContaining(
          `/projekt/${PROJEKT_ID}/abrechnung/${ENTWURF_ID}`
        ),
        method: "PUT",
      },
    ]);
  });

  it("givenErgebnis_whenAbrechnungAbschliessen_thenFinalizeTheEntwurfAndReturn", async () => {
    const fetchSpy = stubFetch({
      id: ENTWURF_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      draft: true,
      geschaeftspartnerId: "1000000001",
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      nutzungsobjekte: [
        {
          art: "ADRESSE",
          adresse: "Marienplatz",
          hausnummerVon: "8",
          nutzungen: ["BAUZAUN"],
          aufschlag50prozent: false,
          positionen: [
            {
              beginn: new Date("2026-01-01"),
              ende: new Date("2026-03-31"),
              laenge: 12,
              breite: 3,
              flaeche: 36,
            },
          ],
          masten: [],
        },
      ],
    });
    const wrapper = await mountView({ entwurfId: ENTWURF_ID });

    await save(wrapper);
    await calculate();
    await clickButton("Abrechnung abschließen");

    expect(requests(fetchSpy)).toContainEqual({
      url: expect.stringContaining(
        `/projekt/${PROJEKT_ID}/abrechnung/${ENTWURF_ID}/finalize`
      ),
      method: "POST",
    });
    expect(useSnackbarStore().queue.at(-1)?.text).toBe(
      `Die Abrechnung ${ENTWURF_ID} wurde abgeschlossen.`
    );
    expect(push).toHaveBeenCalledWith(ABRECHNUNGEN_PATH);
  });

  it("givenErgebnis_whenAlsEntwurfBehalten_thenReturnWithoutFinalizing", async () => {
    const fetchSpy = stubFetch({
      id: ENTWURF_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      draft: true,
      geschaeftspartnerId: "1000000001",
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      nutzungsobjekte: [
        {
          art: "ADRESSE",
          adresse: "Marienplatz",
          hausnummerVon: "8",
          nutzungen: ["BAUZAUN"],
          aufschlag50prozent: false,
          positionen: [
            {
              beginn: new Date("2026-01-01"),
              ende: new Date("2026-03-31"),
              laenge: 12,
              breite: 3,
              flaeche: 36,
            },
          ],
          masten: [],
        },
      ],
    });
    const wrapper = await mountView({ entwurfId: ENTWURF_ID });

    await save(wrapper);
    await calculate();
    await clickButton("Als Entwurf behalten");

    expect(
      requests(fetchSpy).some((request) => request.url.endsWith("/finalize"))
    ).toBe(false);
    expect(useSnackbarStore().queue.at(-1)?.text).toBe(
      "Die Abrechnung wurde als Entwurf gespeichert."
    );
    expect(push).toHaveBeenCalledWith(ABRECHNUNGEN_PATH);
  });

  it("givenErgebnis_whenWeiterBearbeiten_thenStayOnTheForm", async () => {
    const fetchSpy = stubFetch({
      id: ENTWURF_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      draft: true,
      geschaeftspartnerId: "1000000001",
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      nutzungsobjekte: [
        {
          art: "ADRESSE",
          adresse: "Marienplatz",
          hausnummerVon: "8",
          nutzungen: ["BAUZAUN"],
          aufschlag50prozent: false,
          positionen: [
            {
              beginn: new Date("2026-01-01"),
              ende: new Date("2026-03-31"),
              laenge: 12,
              breite: 3,
              flaeche: 36,
            },
          ],
          masten: [],
        },
      ],
    });
    const wrapper = await mountView({ entwurfId: ENTWURF_ID });

    await save(wrapper);
    await calculate();
    await clickButton("Weiter bearbeiten");

    expect(
      wrapper.findComponent(BerechnungErgebnisDialog).props("modelValue")
    ).toBe(false);
    expect(
      requests(fetchSpy).some((request) => request.url.endsWith("/finalize"))
    ).toBe(false);
    expect(push).not.toHaveBeenCalled();
  });

  it("givenEntwurfFinalizedMeanwhile_whenSaved_thenReportItAndOpenNoBerechnung", async () => {
    const fetchSpy = stubFetch({
      id: ENTWURF_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      draft: true,
      geschaeftspartnerId: "1000000001",
      zeitraumVon: new Date("2026-01-01"),
      zeitraumBis: new Date("2026-03-31"),
      abrechnungsArt: "ENDABRECHNUNG",
      nutzungsobjekte: [
        {
          art: "ADRESSE",
          adresse: "Marienplatz",
          hausnummerVon: "8",
          nutzungen: ["BAUZAUN"],
          aufschlag50prozent: false,
          positionen: [
            {
              beginn: new Date("2026-01-01"),
              ende: new Date("2026-03-31"),
              laenge: 12,
              breite: 3,
              flaeche: 36,
            },
          ],
          masten: [],
        },
      ],
    });
    const answer = fetchSpy.getMockImplementation();
    fetchSpy.mockImplementation((url: string, init?: RequestInit) =>
      init?.method === "PUT"
        ? new Response("", { status: 409 })
        : answer?.(url, init)
    );
    const wrapper = await mountView({ entwurfId: ENTWURF_ID });

    await save(wrapper);

    expect(useSnackbarStore().queue.at(-1)?.text).toBe(
      "Die Abrechnung ist bereits abgeschlossen und lässt sich nicht mehr ändern."
    );
    expect(wrapper.findComponent(BerechnungDialog).props("modelValue")).toBe(
      false
    );
  });
});
