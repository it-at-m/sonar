import type { AbrechnungForm } from "@/types/abrechnung/AbrechnungForm";
import type { RouteLocationNormalized } from "vue-router";

import { createTestingPinia } from "@pinia/testing";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { onBeforeRouteLeave } from "vue-router";

import { AbrechnungRequestDTOAbrechnungsArtEnum } from "@/api/generated/sonar-backend";
import AbrechnungTabs from "@/components/AbrechnungTabs.vue";
import { useAbrechnungForm } from "@/composables/abrechnungForm";
import vuetify from "@/plugins/vuetify";
import AbrechnungAnlegenView from "@/views/AbrechnungAnlegenView.vue";

const PROJEKT_ID = "123e4567-e89b-12d3-a456-426614174000";
const VORGAENGER_ID = "123e4567-e89b-12d3-a456-426614174001";

const push = vi.fn();

vi.mock("vue-router", () => ({
  onBeforeRouteLeave: vi.fn(),
  useRouter: () => ({ push }),
}));

function jsonResponse(body: unknown, status = 200): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

function stubFetch(
  vorgaenger: unknown = {},
  saveResponse?: () => Response | Promise<Response>
): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockImplementation((url: string) => {
    if (url.includes("/berechnung/preview")) {
      return jsonResponse({
        lfdNr: 1,
        abrechnungszeitraumVon: "2026-01-05",
        abrechnungszeitraumBis: "2026-01-25",
        zusammenfassung: "Abrechnung 1 von Bauprojekt P-0001",
        berechnungslog: "====>>>> Die Gesamtgebühren betragen 162,00€",
        gebuehrFlaechen: 162,
        gebuehrUeberspannungen: 0,
        gebuehrVerwaltung: 0,
        gebuehrGesamt: 162,
        gebuehrZahlung: 162,
        bescheiddatenFlaechen: [],
        abrechnungenUeberspannungen: [],
      });
    }
    if (url.endsWith("/abrechnung/nutzungsobjekt")) {
      return jsonResponse([]);
    }
    if (url.includes(`/abrechnung/${VORGAENGER_ID}`)) {
      return jsonResponse(vorgaenger);
    }
    if (
      saveResponse !== undefined &&
      url.endsWith(`/projekt/${PROJEKT_ID}/abrechnung`)
    ) {
      return saveResponse();
    }
    if (url.includes("/geschaeftspartner/")) {
      return jsonResponse({ name1: "Bau GmbH" });
    }
    return jsonResponse({ projektnummer: "P-0001", adressen: [] });
  });
  vi.stubGlobal("fetch", fetchSpy);
  return fetchSpy;
}

function requests(
  fetchSpy: ReturnType<typeof vi.fn>
): { url: string; method: string | undefined; body: unknown }[] {
  return fetchSpy.mock.calls.map((call) => {
    const init = call[1] as RequestInit | undefined;
    return {
      url: call[0] as string,
      method: init?.method,
      body:
        init?.body === undefined ? undefined : JSON.parse(String(init.body)),
    };
  });
}

function itemAt<T>(items: readonly T[], index = 0): T {
  const item = items[index];
  if (item === undefined) {
    throw new Error(`Kein Element an Position ${index}.`);
  }
  return item;
}

function mountView(props: {
  projektId: string;
  vorgaengerAbrechnungId?: string;
}) {
  return mount(AbrechnungAnlegenView, {
    props,
    attachTo: document.body,
    global: {
      plugins: [
        vuetify,
        createTestingPinia({ createSpy: vi.fn, stubActions: false }),
      ],
      stubs: { YesNoDialog: true },
    },
  });
}

async function enterAbrechnung(
  wrapper: ReturnType<typeof mountView>,
  abrechnung: AbrechnungForm
): Promise<void> {
  wrapper
    .findComponent(AbrechnungTabs)
    .vm.$emit("update:modelValue", abrechnung);
  await flushPromises();
}

function berechnenButton(wrapper: ReturnType<typeof mountView>) {
  return wrapper
    .findAll("button")
    .find((button) => button.text() === "Berechnen");
}

async function clickBerechnen(
  wrapper: ReturnType<typeof mountView>
): Promise<void> {
  await berechnenButton(wrapper)?.trigger("click");
  await flushPromises();
}

async function submitAbrechnungsdatum(): Promise<void> {
  document
    .querySelector("#berechnung-abrechnungsdatum")
    ?.closest("form")
    ?.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
  await flushPromises();
}

describe("AbrechnungAnlegenView.vue", () => {
  beforeEach(() => {
    push.mockClear();
    vi.mocked(onBeforeRouteLeave).mockClear();
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
    vi.useFakeTimers({ toFake: ["Date"] });
    vi.setSystemTime(new Date(2026, 8, 30, 1, 30));
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.unstubAllGlobals();
    document.body.innerHTML = "";
  });

  it("givenAnInvalidForm_thenFocusTheFirstErrorInsteadOfAskingForTheAbrechnungsdatum", async () => {
    const fetchSpy = stubFetch();
    const wrapper = mountView({ projektId: PROJEKT_ID });
    await flushPromises();

    await clickBerechnen(wrapper);

    expect(document.activeElement?.id).toBe("basis-geschaeftspartner-id");
    expect(document.querySelector("#berechnung-abrechnungsdatum")).toBeNull();
    expect(
      requests(fetchSpy).filter(({ url }) => url.includes("/berechnung/"))
    ).toEqual([]);
  });

  it("givenAValidForm_thenCalculateItWithoutSaving", async () => {
    const fetchSpy = stubFetch();
    const wrapper = mountView({ projektId: PROJEKT_ID });
    await flushPromises();
    const { abrechnung } = useAbrechnungForm();
    abrechnung.value.geschaeftspartnerId = "1000000001";
    abrechnung.value.zeitraumVon = "2026-01-05";
    abrechnung.value.zeitraumBis = "2026-01-25";
    abrechnung.value.abrechnungsArt =
      AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG;
    const nutzungsobjekt = itemAt(abrechnung.value.nutzungsobjekte);
    nutzungsobjekt.adresse = "Marienplatz";
    nutzungsobjekt.hausnummerVon = "8";
    const position = itemAt(nutzungsobjekt.positionen);
    position.beginn = "2026-01-05";
    position.ende = "2026-01-25";
    position.laenge = 12;
    position.breite = 3;
    position.flaeche = 36;
    await enterAbrechnung(wrapper, abrechnung.value);

    await clickBerechnen(wrapper);
    await submitAbrechnungsdatum();

    const previews = requests(fetchSpy).filter(({ url }) =>
      url.includes("/berechnung/preview")
    );
    expect(previews).toHaveLength(1);
    expect(itemAt(previews).url).toContain(
      `/projekt/${PROJEKT_ID}/abrechnung/berechnung/preview?berechnungsdatum=2026-09-30`
    );
    expect(itemAt(previews).method).toBe("POST");
    expect(
      requests(fetchSpy).filter(({ url }) =>
        url.endsWith(`/projekt/${PROJEKT_ID}/abrechnung`)
      )
    ).toEqual([]);
    expect(push).not.toHaveBeenCalled();
    expect(document.body.textContent).toContain("Ergebnis der Berechnung");
  });

  it("givenACalculatedForm_thenStillAskBeforeLeaving", async () => {
    stubFetch();
    const wrapper = mountView({ projektId: PROJEKT_ID });
    await flushPromises();
    const { abrechnung } = useAbrechnungForm();
    abrechnung.value.geschaeftspartnerId = "1000000001";
    abrechnung.value.zeitraumVon = "2026-01-05";
    abrechnung.value.zeitraumBis = "2026-01-25";
    abrechnung.value.abrechnungsArt =
      AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG;
    const nutzungsobjekt = itemAt(abrechnung.value.nutzungsobjekte);
    nutzungsobjekt.adresse = "Marienplatz";
    nutzungsobjekt.hausnummerVon = "8";
    const position = itemAt(nutzungsobjekt.positionen);
    position.beginn = "2026-01-05";
    position.ende = "2026-01-25";
    position.laenge = 12;
    position.breite = 3;
    position.flaeche = 36;
    await enterAbrechnung(wrapper, abrechnung.value);
    await clickBerechnen(wrapper);
    await submitAbrechnungsdatum();

    const next = vi.fn();
    const leaveGuard = vi.mocked(onBeforeRouteLeave).mock.calls[0]?.[0];
    await leaveGuard?.(
      {} as RouteLocationNormalized,
      {} as RouteLocationNormalized,
      next
    );
    await flushPromises();

    expect(next).not.toHaveBeenCalled();
    expect(
      wrapper.findComponent({ name: "YesNoDialog" }).props("modelValue")
    ).toBe(true);
  });

  it("givenANewVersion_thenCalculateItWithoutSavingIt", async () => {
    const fetchSpy = stubFetch({
      id: VORGAENGER_ID,
      projektId: PROJEKT_ID,
      versionsnummer: 1,
      geschaeftspartnerId: "1000000001",
      zustellungsbevollmaechtigterGenutzt: false,
      zeitraumVon: "2026-01-05",
      zeitraumBis: "2026-01-25",
      abrechnungsArt: "ENDABRECHNUNG",
      neuereVersionVorhanden: false,
      nutzungsobjekte: [
        {
          id: "123e4567-e89b-12d3-a456-426614174002",
          art: "ADRESSE",
          adresse: "Marienplatz",
          hausnummerVon: "8",
          nutzungen: ["BAUZAUN"],
          aufschlag50prozent: true,
          positionen: [
            {
              id: "123e4567-e89b-12d3-a456-426614174003",
              beginn: "2026-01-05",
              ende: "2026-01-25",
              laenge: 12,
              breite: 3,
              flaeche: 36,
            },
          ],
          masten: [],
        },
      ],
    });
    const wrapper = mountView({
      projektId: PROJEKT_ID,
      vorgaengerAbrechnungId: VORGAENGER_ID,
    });
    await flushPromises();

    await clickBerechnen(wrapper);
    await submitAbrechnungsdatum();

    const previews = requests(fetchSpy).filter(({ url }) =>
      url.includes("/berechnung/preview")
    );
    expect(previews).toHaveLength(1);
    expect(itemAt(previews).url).toContain(
      `/projekt/${PROJEKT_ID}/abrechnung/berechnung/preview`
    );
    expect(itemAt(previews).body).toMatchObject({
      nutzungsobjekte: [{ adresse: "Marienplatz", aufschlag50prozent: true }],
    });
    expect(
      requests(fetchSpy).filter(({ url }) => url.endsWith("/version"))
    ).toEqual([]);
    expect(push).not.toHaveBeenCalled();
  });

  it("givenARunningSave_thenDisableTheBerechnenButton", async () => {
    let answerSave: (response: Response) => void = () => undefined;
    stubFetch(
      {},
      () =>
        new Promise<Response>((resolve) => {
          answerSave = resolve;
        })
    );
    const wrapper = mountView({ projektId: PROJEKT_ID });
    await flushPromises();
    const { abrechnung } = useAbrechnungForm();
    abrechnung.value.geschaeftspartnerId = "1000000001";
    abrechnung.value.zeitraumVon = "2026-01-05";
    abrechnung.value.zeitraumBis = "2026-01-25";
    abrechnung.value.abrechnungsArt =
      AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG;
    const nutzungsobjekt = itemAt(abrechnung.value.nutzungsobjekte);
    nutzungsobjekt.adresse = "Marienplatz";
    nutzungsobjekt.hausnummerVon = "8";
    const position = itemAt(nutzungsobjekt.positionen);
    position.beginn = "2026-01-05";
    position.ende = "2026-01-25";
    position.laenge = 12;
    position.breite = 3;
    position.flaeche = 36;
    await enterAbrechnung(wrapper, abrechnung.value);

    await wrapper.find("form").trigger("submit");
    await flushPromises();

    expect(berechnenButton(wrapper)?.attributes("disabled")).toBeDefined();

    answerSave(
      jsonResponse(
        { id: "123e4567-e89b-12d3-a456-426614174004", versionsnummer: 1 },
        201
      )
    );
    await flushPromises();

    expect(berechnenButton(wrapper)?.attributes("disabled")).toBeUndefined();
  });
});
