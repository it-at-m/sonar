import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import AbrechnungBerechnungDialog from "@/components/AbrechnungBerechnungDialog.vue";
import vuetify from "@/plugins/vuetify";

const PROJEKT_ID = "123e4567-e89b-12d3-a456-426614174000";
const ABRECHNUNG_ID = "123e4567-e89b-12d3-a456-426614174001";
const GESCHAEFTSPARTNER_ID = "1000000001";

function mountDialog() {
  return mount(AbrechnungBerechnungDialog, {
    props: {
      abrechnungId: ABRECHNUNG_ID,
      geschaeftspartnerId: GESCHAEFTSPARTNER_ID,
      projektId: PROJEKT_ID,
    },
    attachTo: document.body,
    global: { plugins: [vuetify] },
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

function abrechnungsdatumInput(): HTMLInputElement {
  return document.querySelector(
    "#berechnung-abrechnungsdatum"
  ) as HTMLInputElement;
}

async function fillAbrechnungsdatum(datum: string): Promise<void> {
  abrechnungsdatumInput().value = datum;
  abrechnungsdatumInput().dispatchEvent(new Event("input"));
  await flushPromises();
}

async function submit(): Promise<void> {
  const form = document.querySelector("form") as HTMLFormElement;
  form.dispatchEvent(new Event("submit", { bubbles: true, cancelable: true }));
  await flushPromises();
}

async function cancel(): Promise<void> {
  const buttons = [...document.querySelectorAll<HTMLElement>("button")];
  buttons.find((button) => button.textContent?.includes("Abbrechen"))?.click();
  await flushPromises();
}

describe("AbrechnungBerechnungDialog.vue", () => {
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
    // The overlay of the dialog positions itself against the visual viewport, which jsdom has none of.
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
    // Before 02:00 in Munich, where the UTC date is still the day before.
    vi.setSystemTime(new Date(2026, 8, 30, 1, 30));
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.unstubAllGlobals();
    document.body.innerHTML = "";
  });

  it("givenMount_thenShowOnlyTheButton", () => {
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
    const wrapper = mountDialog();

    await openDialog(wrapper);

    expect(isOpen(wrapper)).toBe(true);
    expect(document.body.textContent).toContain("Berechnung erstellen");
    expect(abrechnungsdatumInput()).not.toBeNull();
  });

  it("givenOpenedDialog_thenOfferTodayAsAbrechnungsdatum", async () => {
    const wrapper = mountDialog();

    await openDialog(wrapper);

    expect(abrechnungsdatumInput().value).toBe("2026-09-30");
  });

  it("givenEnteredAbrechnungsdatum_thenCloseTheDialog", async () => {
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum("2026-03-31");
    await submit();

    expect(isOpen(wrapper)).toBe(false);
  });

  it("givenMissingAbrechnungsdatum_thenKeepTheDialogOpen", async () => {
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum("");
    await submit();

    expect(isOpen(wrapper)).toBe(true);
    expect(document.body.textContent).toContain("Pflichtfeld");
  });

  it("givenAbbrechen_thenCloseTheDialog", async () => {
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await cancel();

    expect(isOpen(wrapper)).toBe(false);
  });

  it("givenReopenedDialog_thenStartOnTodayAgain", async () => {
    const wrapper = mountDialog();

    await openDialog(wrapper);
    await fillAbrechnungsdatum("2026-03-31");
    await cancel();
    await openDialog(wrapper);

    expect(abrechnungsdatumInput().value).toBe("2026-09-30");
  });
});
