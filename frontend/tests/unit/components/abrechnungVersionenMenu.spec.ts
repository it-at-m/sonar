import type { AbrechnungVersionResponseDTO } from "@/api/generated/sonar-backend";

import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import AbrechnungVersionenMenu from "@/components/AbrechnungVersionenMenu.vue";
import vuetify from "@/plugins/vuetify";

const PROJEKT_ID = "123e4567-e89b-12d3-a456-426614174000";
const ABRECHNUNG_ID = "123e4567-e89b-12d3-a456-426614174001";
const VORGAENGER_ID = "123e4567-e89b-12d3-a456-426614174002";

Object.defineProperty(globalThis, "visualViewport", {
  value: undefined,
  writable: true,
});

function stubFetch(
  versionen: AbrechnungVersionResponseDTO[]
): ReturnType<typeof vi.fn> {
  const fetchSpy = vi.fn().mockResolvedValue(
    new Response(JSON.stringify(versionen), {
      status: 200,
      headers: { "Content-Type": "application/json" },
    })
  );
  vi.stubGlobal("fetch", fetchSpy);
  return fetchSpy;
}

function mountMenu() {
  return mount(AbrechnungVersionenMenu, {
    props: { projektId: PROJEKT_ID, abrechnungId: ABRECHNUNG_ID },
    global: { plugins: [vuetify] },
    attachTo: document.body,
  });
}

async function openMenu(wrapper: ReturnType<typeof mountMenu>) {
  await wrapper.find("button").trigger("click");
  await flushPromises();
}

async function reopenMenu(wrapper: ReturnType<typeof mountMenu>) {
  const menu = wrapper.findComponent({ name: "VMenu" });
  await menu.vm.$emit("update:modelValue", false);
  await menu.vm.$emit("update:modelValue", true);
  await flushPromises();
}

function listItems(wrapper: ReturnType<typeof mountMenu>) {
  return wrapper.findAllComponents({ name: "VListItem" });
}

describe("AbrechnungVersionenMenu.vue", () => {
  beforeEach(() => {
    // jsdom has none, and the overlay observes its content to position itself.
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
    document.body.innerHTML = "";
  });

  it("givenMenu_thenNameItsButtonForScreenReaders", () => {
    stubFetch([]);
    const wrapper = mountMenu();

    expect(wrapper.find("button").attributes("aria-label")).toBe(
      "Versionen der Abrechnung anzeigen"
    );
  });

  it("givenClosedMenu_thenRequestNothing", () => {
    const fetchSpy = stubFetch([]);
    mountMenu();

    expect(fetchSpy).not.toHaveBeenCalled();
  });

  it("givenOpenedMenu_thenRequestTheVersionenOfThatAbrechnung", async () => {
    const fetchSpy = stubFetch([
      { id: ABRECHNUNG_ID, versionsnummer: 2 },
      { id: VORGAENGER_ID, versionsnummer: 1 },
    ]);
    const wrapper = mountMenu();

    await openMenu(wrapper);

    expect(String(fetchSpy.mock.calls[0]?.[0])).toContain(
      `/projekt/${PROJEKT_ID}/abrechnung/${ABRECHNUNG_ID}/version`
    );
  });

  it("givenVersionen_thenLinkEachOneToItsAnsehenRoute", async () => {
    stubFetch([
      { id: ABRECHNUNG_ID, versionsnummer: 2 },
      { id: VORGAENGER_ID, versionsnummer: 1 },
    ]);
    const wrapper = mountMenu();

    await openMenu(wrapper);

    expect(listItems(wrapper).map((item) => item.props("to"))).toEqual([
      `/projekte/${PROJEKT_ID}/abrechnungen/${ABRECHNUNG_ID}/ansehen`,
      `/projekte/${PROJEKT_ID}/abrechnungen/${VORGAENGER_ID}/ansehen`,
    ]);
  });

  it("givenAktuelleVersion_thenMarkItAsAktuell", async () => {
    stubFetch([
      { id: ABRECHNUNG_ID, versionsnummer: 2 },
      { id: VORGAENGER_ID, versionsnummer: 1 },
    ]);
    const wrapper = mountMenu();

    await openMenu(wrapper);

    expect(listItems(wrapper).map((item) => item.props("title"))).toEqual([
      "Version 2 (aktuell)",
      "Version 1",
    ]);
  });

  it("givenPendingRequest_thenAnnounceThatTheVersionenAreLoading", async () => {
    const nieBeantwortet = new Promise<Response>(() => undefined);
    vi.stubGlobal("fetch", vi.fn().mockReturnValue(nieBeantwortet));
    const wrapper = mountMenu();

    await openMenu(wrapper);

    expect(listItems(wrapper).map((item) => item.props("title"))).toEqual([
      "Die Versionen werden geladen.",
    ]);
  });

  it("givenFailingRequest_thenReportThatTheVersionenCouldNotBeLoaded", async () => {
    vi.stubGlobal("fetch", vi.fn().mockRejectedValue(new Error("offline")));
    const wrapper = mountMenu();

    await openMenu(wrapper);

    expect(listItems(wrapper).map((item) => item.props("title"))).toEqual([
      "Die Versionen konnten nicht geladen werden.",
    ]);
  });

  it("givenReopenedMenuAfterAFailure_thenRequestTheVersionenAgain", async () => {
    const fetchSpy = vi.fn().mockRejectedValue(new Error("offline"));
    vi.stubGlobal("fetch", fetchSpy);
    const wrapper = mountMenu();

    await openMenu(wrapper);
    await reopenMenu(wrapper);

    expect(fetchSpy).toHaveBeenCalledTimes(2);
  });

  it("givenReopenedMenu_thenRequestTheVersionenOnlyOnce", async () => {
    const fetchSpy = stubFetch([
      { id: ABRECHNUNG_ID, versionsnummer: 2 },
      { id: VORGAENGER_ID, versionsnummer: 1 },
    ]);
    const wrapper = mountMenu();

    await openMenu(wrapper);
    await reopenMenu(wrapper);

    expect(fetchSpy).toHaveBeenCalledTimes(1);
  });
});
