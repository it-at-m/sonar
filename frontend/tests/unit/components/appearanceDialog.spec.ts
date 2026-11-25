import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { VApp } from "vuetify/components";

import AppearanceDialog from "@/components/AppearanceDialog.vue";
import vuetify from "@/plugins/vuetify";
import {
  DEFAULT_THEME_COLOR,
  THEME_COLOR_STORAGE_KEY,
} from "@/util/themeColor";

const ColorPickerStub = {
  name: "VColorPicker",
  props: { modelValue: { type: String, default: DEFAULT_THEME_COLOR } },
  emits: ["update:modelValue"],
  template: `<button aria-label="Testfarbe auswählen"
    @click="$emit('update:modelValue', '#abcdef')">Farbe auswählen</button>`,
};

function mountDialog() {
  return mount(
    {
      components: { VApp, AppearanceDialog },
      data: () => ({ showAppearance: false }),
      template: '<v-app><appearance-dialog v-model="showAppearance" /></v-app>',
    },
    {
      global: {
        plugins: [vuetify],
        stubs: {
          VColorPicker: ColorPickerStub,
        },
      },
      attachTo: document.body,
    }
  );
}

describe("AppearanceDialog", () => {
  let wrapper: ReturnType<typeof mountDialog>;

  beforeEach(() => {
    localStorage.clear();
    vuetify.theme.themes.value.light.colors.primary = DEFAULT_THEME_COLOR;
    vi.stubGlobal("visualViewport", undefined);
    vi.stubGlobal(
      "ResizeObserver",
      class {
        observe = vi.fn();
        unobserve = vi.fn();
        disconnect = vi.fn();
      }
    );
    wrapper = mountDialog();
  });

  afterEach(() => {
    wrapper.unmount();
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
    localStorage.clear();
    vuetify.theme.themes.value.light.colors.primary = DEFAULT_THEME_COLOR;
    document.body.innerHTML = "";
  });

  async function openSettings() {
    await wrapper.setData({ showAppearance: true });
    await flushPromises();
  }

  async function clickButton(label: string) {
    const button = wrapper
      .findAllComponents({ name: "VBtn" })
      .find((button) => button.text() === label);
    if (!button) throw new Error(`Button is missing: ${label}`);
    await button.trigger("click");
    await flushPromises();
  }

  async function openCustomColor() {
    await openSettings();
    await clickButton("Eigene Farbe");
  }

  it("givenOpenDialog_thenShowPresetsWithoutCustomPicker", async () => {
    expect(wrapper.findComponent(ColorPickerStub).exists()).toBe(false);

    await openSettings();

    expect(wrapper.findComponent({ name: "VDialog" }).props("modelValue")).toBe(
      true
    );
    expect(wrapper.findComponent(ColorPickerStub).exists()).toBe(false);
    expect(document.body.textContent).toContain(
      "In diesem Browser gespeichert."
    );
    const swatches = wrapper
      .findAllComponents({ name: "VBtn" })
      .filter((button) => button.attributes("aria-pressed") !== undefined);
    expect(swatches).toHaveLength(6);
    expect(swatches[0].attributes("aria-pressed")).toBe("true");
  });

  it("givenCustomColorClick_thenRevealPickerWithCurrentColor", async () => {
    await openCustomColor();

    expect(wrapper.findComponent(ColorPickerStub).props("modelValue")).toBe(
      DEFAULT_THEME_COLOR
    );

    await clickButton("Eigene Farbe");
    expect(wrapper.findComponent(ColorPickerStub).exists()).toBe(false);
  });

  it("givenPresetClick_thenUpdateThemeAndStorageAndMarkSelection", async () => {
    await openSettings();
    const blueSwatch = wrapper
      .findAllComponents({ name: "VBtn" })
      .find((button) => button.attributes("aria-label") === "Blau");
    if (!blueSwatch) throw new Error("Blue preset is missing");
    await blueSwatch.trigger("click");

    expect(vuetify.theme.themes.value.light.colors.primary).toBe("#1565C0");
    expect(localStorage.getItem(THEME_COLOR_STORAGE_KEY)).toBe("#1565C0");
    expect(blueSwatch.attributes("aria-pressed")).toBe("true");
  });

  it("givenColorSelection_thenUpdateGlobalThemeAndStorageWithoutClosingDialog", async () => {
    await openCustomColor();
    await wrapper.findComponent(ColorPickerStub).trigger("click");
    await flushPromises();

    expect(vuetify.theme.themes.value.light.colors.primary).toBe("#ABCDEF");
    expect(localStorage.getItem(THEME_COLOR_STORAGE_KEY)).toBe("#ABCDEF");
    expect(wrapper.findComponent(ColorPickerStub).props("modelValue")).toBe(
      "#ABCDEF"
    );
    expect(wrapper.findComponent({ name: "VDialog" }).props("modelValue")).toBe(
      true
    );
  });

  it("givenInvalidColorSelection_thenKeepCurrentTheme", async () => {
    await openCustomColor();
    wrapper.findComponent(ColorPickerStub).vm.$emit("update:modelValue", null);
    await flushPromises();

    expect(vuetify.theme.themes.value.light.colors.primary).toBe(
      DEFAULT_THEME_COLOR
    );
    expect(localStorage.getItem(THEME_COLOR_STORAGE_KEY)).toBeNull();
  });

  it("givenResetClick_thenRestoreAndPersistDefaultColor", async () => {
    await openCustomColor();
    await wrapper.findComponent(ColorPickerStub).trigger("click");
    await clickButton("Standard wiederherstellen");

    expect(vuetify.theme.themes.value.light.colors.primary).toBe(
      DEFAULT_THEME_COLOR
    );
    expect(localStorage.getItem(THEME_COLOR_STORAGE_KEY)).toBe(
      DEFAULT_THEME_COLOR
    );
  });

  it("givenBlockedStorage_thenStillApplySelectedColor", async () => {
    vi.spyOn(Storage.prototype, "setItem").mockImplementation(() => {
      throw new Error("Storage is blocked");
    });
    await openCustomColor();
    await wrapper.findComponent(ColorPickerStub).trigger("click");

    expect(vuetify.theme.themes.value.light.colors.primary).toBe("#ABCDEF");
  });

  it("givenLightColor_thenDeriveDarkForeground", async () => {
    await openCustomColor();
    wrapper
      .findComponent(ColorPickerStub)
      .vm.$emit("update:modelValue", "#FFFFFF");
    await flushPromises();

    expect(vuetify.theme.computedThemes.value.light.colors["on-primary"]).toBe(
      "#000"
    );
  });

  it("givenCloseClick_thenCloseDialogAndKeepSelectedColor", async () => {
    await openCustomColor();
    await wrapper.findComponent(ColorPickerStub).trigger("click");
    await clickButton("Schließen");

    expect(wrapper.findComponent({ name: "VDialog" }).props("modelValue")).toBe(
      false
    );
    expect(vuetify.theme.themes.value.light.colors.primary).toBe("#ABCDEF");
  });

  it("givenReopenedDialog_thenCollapseCustomPickerAndKeepSelectedColor", async () => {
    await openCustomColor();
    await wrapper.findComponent(ColorPickerStub).trigger("click");
    await clickButton("Schließen");

    await openSettings();

    expect(wrapper.findComponent(ColorPickerStub).exists()).toBe(false);
    expect(vuetify.theme.themes.value.light.colors.primary).toBe("#ABCDEF");
    await clickButton("Eigene Farbe");
    expect(wrapper.findComponent(ColorPickerStub).props("modelValue")).toBe(
      "#ABCDEF"
    );
  });

  it("givenSavedColor_thenRestoreItWhenVuetifyInitializes", async () => {
    localStorage.setItem(THEME_COLOR_STORAGE_KEY, "#123abc");
    vi.resetModules();

    const { default: restoredVuetify } = await import("@/plugins/vuetify");

    expect(restoredVuetify.theme.themes.value.light.colors.primary).toBe(
      "#123ABC"
    );
  });
});
