import { createTestingPinia } from "@pinia/testing";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import vuetify from "@/plugins/vuetify";
import { useSnackbarStore } from "@/stores/snackbar";
import WiderspruchAnlegenView from "@/views/WiderspruchAnlegenView.vue";

const PROJEKT_ID = "123e4567-e89b-12d3-a456-426614174000";
const ABRECHNUNG_ID = "123e4567-e89b-12d3-a456-426614174001";

const push = vi.fn();

vi.mock("vue-router", () => ({
  onBeforeRouteLeave: vi.fn(),
  useRouter: () => ({ push }),
}));

function stubFetch(status = 201): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockImplementation(
    () =>
      new Response(
        JSON.stringify({
          id: "123e4567-e89b-12d3-a456-426614174002",
          abrechnungId: ABRECHNUNG_ID,
          datumEingang: "2026-04-01",
        }),
        { status, headers: { "Content-Type": "application/json" } }
      )
  );
  vi.stubGlobal("fetch", fetchSpy);
  return fetchSpy;
}

function requestedUrl(fetchSpy: ReturnType<typeof vi.fn>): string {
  return fetchSpy.mock.calls.at(-1)?.[0] as string;
}

function requestedBody(fetchSpy: ReturnType<typeof vi.fn>): unknown {
  const init = fetchSpy.mock.calls.at(-1)?.[1] as RequestInit;
  return JSON.parse(String(init.body));
}

function mountView() {
  return mount(WiderspruchAnlegenView, {
    props: { abrechnungId: ABRECHNUNG_ID, projektId: PROJEKT_ID },
    global: {
      plugins: [
        vuetify,
        createTestingPinia({ createSpy: vi.fn, stubActions: false }),
      ],
      stubs: { YesNoDialog: true },
    },
  });
}

async function fillDatumEingang(
  wrapper: ReturnType<typeof mountView>,
  value: string
): Promise<void> {
  await wrapper.find("#widerspruch-datum-eingang").setValue(value);
}

async function submit(wrapper: ReturnType<typeof mountView>): Promise<void> {
  await wrapper.find("form").trigger("submit");
  await flushPromises();
}

describe("WiderspruchAnlegenView.vue", () => {
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
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("givenMount_thenShowEveryFieldOfTheWiderspruch", () => {
    const wrapper = mountView();

    expect(wrapper.find("#widerspruch-datum-eingang").exists()).toBe(true);
    expect(wrapper.find("#widerspruch-datum-ruecknahme").exists()).toBe(true);
    expect(wrapper.find("#widerspruch-datum-vorlage-regierung").exists()).toBe(
      true
    );
    expect(
      wrapper.find("#widerspruch-datum-ablehnung-regierung").exists()
    ).toBe(true);
    expect(
      wrapper.find("#widerspruch-entscheidung-durchfuehrung").exists()
    ).toBe(true);
    expect(wrapper.find("#widerspruch-bemerkung").exists()).toBe(true);
    expect(wrapper.text()).toContain("SOLL abgesetzt");
    expect(wrapper.text()).toContain("Neue Teilabrechnung anlegen");
    expect(wrapper.text()).toContain("Speichern");
    expect(wrapper.text()).toContain("Abbrechen");
  });

  it("givenFilledDatumEingang_thenPostItBelowTheAbrechnung", async () => {
    const fetchSpy = stubFetch();
    const wrapper = mountView();

    await fillDatumEingang(wrapper, "2026-04-01");
    await submit(wrapper);

    expect(requestedUrl(fetchSpy)).toContain(
      `/abrechnung/${ABRECHNUNG_ID}/widerspruch`
    );
    expect(requestedBody(fetchSpy)).toMatchObject({
      datumEingang: "2026-04-01",
      sollAbgesetzt: false,
      neueTeilabrechnungAnlegen: false,
    });
  });

  it("givenSavedWiderspruch_thenReturnToTheAbrechnungen", async () => {
    stubFetch();
    const wrapper = mountView();

    await fillDatumEingang(wrapper, "2026-04-01");
    await submit(wrapper);

    expect(push).toHaveBeenCalledWith(`/projekte/${PROJEKT_ID}/abrechnungen`);
  });

  it("givenMissingDatumEingang_thenSendNoRequest", async () => {
    const fetchSpy = stubFetch();
    const wrapper = mountView();

    await submit(wrapper);

    expect(fetchSpy).not.toHaveBeenCalled();
  });

  it("givenExistingWiderspruch_thenReportTheConflict", async () => {
    stubFetch(409);
    const wrapper = mountView();

    await fillDatumEingang(wrapper, "2026-04-01");
    await submit(wrapper);

    expect(useSnackbarStore().queue[0]?.text).toBe(
      "Für diese Abrechnung besteht bereits ein Widerspruch."
    );
    expect(push).not.toHaveBeenCalled();
  });

  it("givenUnknownAbrechnung_thenReportItAsNotFound", async () => {
    stubFetch(404);
    const wrapper = mountView();

    await fillDatumEingang(wrapper, "2026-04-01");
    await submit(wrapper);

    expect(useSnackbarStore().queue[0]?.text).toBe(
      "Die Abrechnung wurde nicht gefunden."
    );
  });

  it("givenAbbrechenClicked_thenReturnToTheAbrechnungenWithoutSaving", async () => {
    const fetchSpy = stubFetch();
    const wrapper = mountView();

    const abbrechen = wrapper
      .findAll("button")
      .find((button) => button.text() === "Abbrechen");
    await abbrechen?.trigger("click");

    expect(fetchSpy).not.toHaveBeenCalled();
    expect(push).toHaveBeenCalledWith(`/projekte/${PROJEKT_ID}/abrechnungen`);
  });
});
