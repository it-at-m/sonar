import type { BerechnungResponseDTO } from "@/api/generated/sonar-backend";
import type { AbrechnungForm } from "@/types/abrechnung/AbrechnungForm";

import { createTestingPinia } from "@pinia/testing";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { h } from "vue";

import { AbrechnungRequestDTOAbrechnungsArtEnum } from "@/api/generated/sonar-backend";
import BerechnungDurchfuehrenDialog from "@/components/berechnung/BerechnungDurchfuehrenDialog.vue";
import { useAbrechnungForm } from "@/composables/abrechnungForm";
import vuetify from "@/plugins/vuetify";
import { useSnackbarStore } from "@/stores/snackbar";

const PROJEKT_ID = "123e4567-e89b-12d3-a456-426614174000";
const ABRECHNUNG_ID = "123e4567-e89b-12d3-a456-426614174001";
const GESCHAEFTSPARTNER_ID = "1000000001";

const ERGEBNIS: BerechnungResponseDTO = {
  lfdNr: 1,
  abrechnungszeitraumVon: new Date("2025-01-06"),
  abrechnungszeitraumBis: new Date("2025-01-26"),
  zusammenfassung: "Abrechnung 1 von Bauprojekt P-0001",
  berechnungslog: "====>>>> Die Gesamtgebühren betragen 225,00€",
  gebuehrFlaechen: 225,
  gebuehrUeberspannungen: 0,
  gebuehrVerwaltung: 0,
  gebuehrGesamt: 225,
  gebuehrZahlung: 225,
  bescheiddatenFlaechen: [],
  abrechnungenUeberspannungen: [],
};

function jsonResponse(body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: { "Content-Type": "application/json" },
  });
}

function stubFetch(
  previewResponse: () => Response | Promise<Response> = () =>
    jsonResponse(ERGEBNIS)
): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockImplementation((url: string) => {
    if (url.includes("/berechnung/preview")) {
      return previewResponse();
    }
    if (url.includes("/geschaeftspartner/")) {
      return jsonResponse({ name1: "Bau GmbH" });
    }
    if (url.includes("/abrechnung/")) {
      return jsonResponse({
        geschaeftspartnerId: GESCHAEFTSPARTNER_ID,
        nutzungsobjekte: [],
      });
    }
    return jsonResponse({ projektnummer: "P-0001", adressen: [] });
  });
  vi.stubGlobal("fetch", fetchSpy);
  return fetchSpy;
}

function previewUrl(fetchSpy: ReturnType<typeof vi.fn>): string | undefined {
  return fetchSpy.mock.calls
    .map((call) => call[0] as string)
    .find((url) => url.includes("/berechnung/preview"));
}

function lastPreviewRequest(
  fetchSpy: ReturnType<typeof vi.fn>
): [string, RequestInit] | undefined {
  return fetchSpy.mock.calls
    .filter((call) => (call[0] as string).includes("/berechnung/preview"))
    .map((call): [string, RequestInit] => [
      call[0] as string,
      call[1] as RequestInit,
    ])
    .at(-1);
}

function itemAt<T>(items: readonly T[], index = 0): T {
  const item = items[index];
  if (item === undefined) {
    throw new Error(`Kein Element an Position ${index}.`);
  }
  return item;
}

function mountDialog() {
  return mount(BerechnungDurchfuehrenDialog, {
    props: {
      abrechnungId: ABRECHNUNG_ID,
      geschaeftspartnerId: GESCHAEFTSPARTNER_ID,
      projektId: PROJEKT_ID,
    },
    attachTo: document.body,
    global: {
      plugins: [
        vuetify,
        createTestingPinia({ createSpy: vi.fn, stubActions: false }),
      ],
    },
  });
}

function mountDialogWithActivator(unsavedAbrechnung: AbrechnungForm) {
  return mount(BerechnungDurchfuehrenDialog, {
    props: {
      projektId: PROJEKT_ID,
      unsavedAbrechnung,
    },
    slots: {
      activator: ({ open }: { open: () => void }) =>
        h(
          "button",
          { id: "berechnen", type: "button", onClick: open },
          "Berechnen"
        ),
    },
    attachTo: document.body,
    global: {
      plugins: [
        vuetify,
        createTestingPinia({ createSpy: vi.fn, stubActions: false }),
      ],
    },
  });
}

function isOpen(wrapper: ReturnType<typeof mountDialog>): boolean {
  return wrapper.findComponent({ name: "VDialog" }).props("modelValue");
}

async function openDialog(
  wrapper: ReturnType<typeof mountDialog>
): Promise<void> {
  await wrapper
    .find('[aria-label*="Berechnung der Abrechnung"]')
    .trigger("click");
  await flushPromises();
}

async function openDialogThroughActivator(
  wrapper: ReturnType<typeof mountDialogWithActivator>
): Promise<void> {
  await wrapper.find("#berechnen").trigger("click");
  await flushPromises();
}

function abrechnungsdatumInput(): HTMLInputElement {
  return document.querySelector(
    "#berechnung-abrechnungsdatum"
  ) as HTMLInputElement;
}

async function fillAbrechnungsdatum(
  wrapper: ReturnType<typeof mountDialog>,
  datum: Date | null
): Promise<void> {
  await wrapper
    .findComponent({ name: "VDateInput" })
    .vm.$emit("update:modelValue", datum);
  await flushPromises();
}

async function submit(): Promise<void> {
  const form = document.querySelector("form") as HTMLFormElement;
  form.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
  await flushPromises();
}

async function showTab(selector: string): Promise<void> {
  document.querySelector<HTMLElement>(selector)?.click();
  await flushPromises();
}

function abbrechenButton(): HTMLButtonElement | undefined {
  return [...document.querySelectorAll<HTMLButtonElement>("button")].find(
    (button) => button.textContent?.includes("Abbrechen")
  );
}

async function cancel(): Promise<void> {
  abbrechenButton()?.click();
  await flushPromises();
}

async function closeErgebnis(): Promise<void> {
  const buttons = [...document.querySelectorAll<HTMLElement>("button")];
  buttons.find((button) => button.textContent?.includes("Schließen"))?.click();
  await flushPromises();
}

describe("BerechnungDurchfuehrenDialog.vue", () => {
  beforeEach(() => {
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

  it("givenMount_thenShowOnlyTheButton", () => {
    stubFetch();
    const wrapper = mountDialog();

    expect(
      wrapper
        .find('[aria-label*="Berechnung der Abrechnung"]')
        .attributes("aria-label")
    ).toBe("Berechnung der Abrechnung 1000000001 erstellen");
    expect(isOpen(wrapper)).toBe(false);
    expect(abrechnungsdatumInput()).toBeNull();
  });

  it("givenClickedButton_thenAskForTheAbrechnungsdatum", async () => {
    stubFetch();
    const wrapper = mountDialog();

    await openDialog(wrapper);

    expect(isOpen(wrapper)).toBe(true);
    expect(document.body.textContent).toContain("Berechnung erstellen");
    expect(abrechnungsdatumInput()).not.toBeNull();
  });

  it("givenOpenedDialog_thenOfferTodayAsAbrechnungsdatum", async () => {
    stubFetch();
    const wrapper = mountDialog();

    await openDialog(wrapper);

    expect(abrechnungsdatumInput().value).toBe("30.09.2026");
  });

  it("givenAPickedAbrechnungsdatum_thenKeepItAnIsoDate", async () => {
    stubFetch();
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));

    expect(
      wrapper.findComponent({ name: "VDateInput" }).props("modelValue")
    ).toBe("2026-03-31");
  });

  it("givenEnteredAbrechnungsdatum_thenRequestThePreviewForThatDay", async () => {
    const fetchSpy = stubFetch();
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await submit();

    expect(previewUrl(fetchSpy)).toContain(
      `/projekt/${PROJEKT_ID}/abrechnung/${ABRECHNUNG_ID}/berechnung/preview`
    );
    expect(previewUrl(fetchSpy)).toContain("berechnungsdatum=2026-03-31");
  });

  it("givenEnteredAbrechnungsdatum_thenCloseTheDialog", async () => {
    stubFetch();
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await submit();

    expect(isOpen(wrapper)).toBe(false);
  });

  it("givenAPreview_thenShowTheErgebnisStartingOnTheKontrolldaten", async () => {
    stubFetch();
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await submit();

    expect(document.body.textContent).toContain("Ergebnis der Berechnung");
    expect(document.body.textContent).toContain("Projekt: P-0001");
    expect(document.body.textContent).toContain(
      "Geschäftspartner: Bau GmbH (1000000001)"
    );
  });

  it("givenTheZusammenfassungTab_thenShowTheZusammenfassung", async () => {
    stubFetch();
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await submit();
    await showTab("#ergebnis-tab-zusammenfassung");

    expect(document.body.textContent).toContain(
      "Abrechnung 1 von Bauprojekt P-0001"
    );
  });

  it("givenTheBerechnungslogTab_thenShowTheGesamtgebuehren", async () => {
    stubFetch();
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await submit();
    await showTab("#ergebnis-tab-berechnungslog");

    expect(document.body.textContent).toContain(
      "Die Gesamtgebühren betragen 225,00€"
    );
  });

  it("givenTheBescheiddatenTab_thenShowTheGebuehrenInEuro", async () => {
    stubFetch();
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await submit();
    await showTab("#ergebnis-tab-bescheiddaten");

    expect(document.body.textContent).toContain("225,00 €");
  });

  it("givenAFailingPreview_thenReportItAndKeepTheDialogOpen", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("offline")));
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await submit();

    expect(useSnackbarStore().queue[0]?.text).toBe(
      "Die Berechnung konnte nicht durchgeführt werden."
    );
    expect(isOpen(wrapper)).toBe(true);
  });

  it("givenAnUnknownAbrechnung_thenReportItAsNotFound", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(new Response("", { status: 404 }))
    );
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await submit();

    expect(useSnackbarStore().queue[0]?.text).toBe(
      "Die Abrechnung wurde nicht gefunden."
    );
  });

  it("givenAProjektOutsideTheStadtbezirkeOfTheUser_thenReportTheMissingZustaendigkeit", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(new Response("", { status: 403 }))
    );
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await submit();

    expect(useSnackbarStore().queue[0]?.text).toBe(
      "Sie sind für den Stadtbezirk dieses Projekts nicht zuständig."
    );
  });

  it("givenMissingAbrechnungsdatum_thenKeepTheDialogOpen", async () => {
    const fetchSpy = stubFetch();
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, null);
    await submit();

    expect(isOpen(wrapper)).toBe(true);
    expect(document.body.textContent).toContain("Pflichtfeld");
    expect(previewUrl(fetchSpy)).toBeUndefined();
  });

  it("givenAbbrechen_thenCloseTheDialog", async () => {
    stubFetch();
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await cancel();

    expect(isOpen(wrapper)).toBe(false);
  });

  it("givenReopenedDialog_thenStartOnTodayAgain", async () => {
    stubFetch();
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await cancel();
    await openDialog(wrapper);

    expect(abrechnungsdatumInput().value).toBe("30.09.2026");
  });

  it("givenARunningBerechnung_thenKeepTheDialogOpenUntilItIsDone", async () => {
    let answerPreview: (response: Response) => void = () => undefined;
    stubFetch(
      () =>
        new Promise<Response>((resolve) => {
          answerPreview = resolve;
        })
    );
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await submit();

    expect(wrapper.findComponent({ name: "VDialog" }).props("persistent")).toBe(
      true
    );
    expect(abbrechenButton()?.disabled).toBe(true);

    answerPreview(jsonResponse(ERGEBNIS));
    await flushPromises();

    expect(isOpen(wrapper)).toBe(false);
    expect(document.body.textContent).toContain("Ergebnis der Berechnung");
  });

  it("givenASecondSubmitWhileCalculating_thenRequestThePreviewOnlyOnce", async () => {
    let answerPreview: (response: Response) => void = () => undefined;
    const fetchSpy = stubFetch(
      () =>
        new Promise<Response>((resolve) => {
          answerPreview = resolve;
        })
    );
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await submit();
    await submit();
    answerPreview(jsonResponse(ERGEBNIS));
    await flushPromises();

    expect(
      fetchSpy.mock.calls.filter((call) =>
        (call[0] as string).includes("/berechnung/preview")
      )
    ).toHaveLength(1);
  });

  it("givenARejectedAbrechnungsdatum_thenReportItAsInvalid", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(new Response("", { status: 400 }))
    );
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await submit();

    expect(useSnackbarStore().queue[0]?.text).toBe(
      "Das Abrechnungsdatum ist ungültig."
    );
  });

  it("givenAnActivator_thenOpenTheDialogFromItInsteadOfTheIconButton", async () => {
    stubFetch();
    const { abrechnung } = useAbrechnungForm();
    const wrapper = mountDialogWithActivator(abrechnung.value);

    expect(
      wrapper.find('[aria-label*="Berechnung der Abrechnung"]').exists()
    ).toBe(false);

    await openDialogThroughActivator(wrapper);

    expect(isOpen(wrapper)).toBe(true);
    expect(abrechnungsdatumInput().value).toBe("30.09.2026");
  });

  it("givenAnUnsavedAbrechnung_thenPostItForThePreviewOfTheEnteredDay", async () => {
    const fetchSpy = stubFetch();
    const { abrechnung } = useAbrechnungForm();
    abrechnung.value.geschaeftspartnerId = GESCHAEFTSPARTNER_ID;
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
    const wrapper = mountDialogWithActivator(abrechnung.value);

    await openDialogThroughActivator(wrapper);
    await fillAbrechnungsdatum(wrapper, new Date(2026, 2, 31));
    await submit();

    const [url, init] = lastPreviewRequest(fetchSpy) ?? [];
    expect(url).toContain(
      `/projekt/${PROJEKT_ID}/abrechnung/berechnung/preview`
    );
    expect(url).toMatch(/berechnungsdatum=2026-03-31(&|$)/);
    expect(init?.method).toBe("POST");
    expect(JSON.parse(String(init?.body))).toMatchObject({
      geschaeftspartnerId: GESCHAEFTSPARTNER_ID,
      nutzungsobjekte: [
        {
          adresse: "Marienplatz",
          hausnummerVon: "8",
          positionen: [{ flaeche: 36 }],
        },
      ],
    });
  });

  it("givenAnUnsavedAbrechnung_thenShowItsOwnDataAsKontrolldaten", async () => {
    const fetchSpy = stubFetch();
    const { abrechnung } = useAbrechnungForm();
    abrechnung.value.geschaeftspartnerId = GESCHAEFTSPARTNER_ID;
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
    const wrapper = mountDialogWithActivator(abrechnung.value);

    await openDialogThroughActivator(wrapper);
    await submit();

    expect(document.body.textContent).toContain("Projekt: P-0001");
    expect(document.body.textContent).toContain(
      "Geschäftspartner: Bau GmbH (1000000001)"
    );
    expect(document.body.textContent).toContain(
      "Marienplatz 8 (Aufschlag: false)"
    );
    expect(
      fetchSpy.mock.calls
        .map((call) => call[0] as string)
        .filter((url) => /\/abrechnung\/[0-9a-f-]{36}(\?|$)/.test(url))
    ).toEqual([]);
  });

  it("givenAFormChangedAfterThePreview_thenKeepTheKontrolldatenOfThePreview", async () => {
    stubFetch();
    const { abrechnung } = useAbrechnungForm();
    abrechnung.value.geschaeftspartnerId = GESCHAEFTSPARTNER_ID;
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
    const wrapper = mountDialogWithActivator(abrechnung.value);

    await openDialogThroughActivator(wrapper);
    await submit();
    itemAt(abrechnung.value.nutzungsobjekte).adresse = "Sendlinger Straße";
    await flushPromises();

    expect(itemAt(abrechnung.value.nutzungsobjekte).adresse).toBe(
      "Sendlinger Straße"
    );
    expect(document.body.textContent).toContain(
      "Marienplatz 8 (Aufschlag: false)"
    );
    expect(document.body.textContent).not.toContain("Sendlinger Straße");
  });

  it("givenACalculationAfterAChange_thenPostAndShowTheChangedAbrechnung", async () => {
    const fetchSpy = stubFetch();
    const { abrechnung } = useAbrechnungForm();
    abrechnung.value.geschaeftspartnerId = GESCHAEFTSPARTNER_ID;
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
    const wrapper = mountDialogWithActivator(abrechnung.value);

    await openDialogThroughActivator(wrapper);
    await submit();
    await closeErgebnis();
    itemAt(abrechnung.value.nutzungsobjekte).adresse = "Sendlinger Straße";
    await openDialogThroughActivator(wrapper);
    await submit();

    const [, init] = lastPreviewRequest(fetchSpy) ?? [];
    expect(JSON.parse(String(init?.body))).toMatchObject({
      nutzungsobjekte: [{ adresse: "Sendlinger Straße", hausnummerVon: "8" }],
    });
    expect(document.body.textContent).toContain(
      "Sendlinger Straße 8 (Aufschlag: false)"
    );
    expect(document.body.textContent).not.toContain("Marienplatz 8");
  });

  it("givenARejectedUnsavedAbrechnung_thenAskToCheckTheEntries", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(new Response("", { status: 400 }))
    );
    const { abrechnung } = useAbrechnungForm();
    abrechnung.value.geschaeftspartnerId = GESCHAEFTSPARTNER_ID;
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
    const wrapper = mountDialogWithActivator(abrechnung.value);

    await openDialogThroughActivator(wrapper);
    await submit();

    expect(useSnackbarStore().queue[0]?.text).toBe(
      "Die Berechnung konnte nicht durchgeführt werden. Bitte prüfen Sie Ihre Eingaben."
    );
    expect(isOpen(wrapper)).toBe(true);
  });

  it("givenAnUnknownProjektOrNutzungsobjekt_thenReportItAsNotFound", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(new Response("", { status: 404 }))
    );
    const { abrechnung } = useAbrechnungForm();
    abrechnung.value.geschaeftspartnerId = GESCHAEFTSPARTNER_ID;
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
    const wrapper = mountDialogWithActivator(abrechnung.value);

    await openDialogThroughActivator(wrapper);
    await submit();

    expect(useSnackbarStore().queue[0]?.text).toBe(
      "Das Projekt oder ein übernommenes Nutzungsobjekt wurde nicht gefunden."
    );
  });

  it("givenAnUnsavedAbrechnungOfAProjektOutsideTheStadtbezirkeOfTheUser_thenReportTheMissingZustaendigkeit", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(new Response("", { status: 403 }))
    );
    const { abrechnung } = useAbrechnungForm();
    abrechnung.value.geschaeftspartnerId = GESCHAEFTSPARTNER_ID;
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
    const wrapper = mountDialogWithActivator(abrechnung.value);

    await openDialogThroughActivator(wrapper);
    await submit();

    expect(useSnackbarStore().queue[0]?.text).toBe(
      "Sie sind für den Stadtbezirk dieses Projekts nicht zuständig."
    );
  });
});
