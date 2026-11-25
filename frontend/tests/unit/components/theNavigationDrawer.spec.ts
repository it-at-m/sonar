import { shallowMount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";

import AppearanceDialog from "@/components/AppearanceDialog.vue";
import TheNavigationDrawer from "@/components/TheNavigationDrawer.vue";
import vuetify from "@/plugins/vuetify";

vi.mock("@/composables/useHasAnyRole", () => ({
  default: () => false,
}));

describe("TheNavigationDrawer", () => {
  it("givenAppearanceEntryClick_thenCloseDrawerAndOpenAppearanceDialog", async () => {
    const wrapper = shallowMount(TheNavigationDrawer, {
      props: { modelValue: true },
      global: {
        plugins: [vuetify],
        renderStubDefaultSlot: true,
        stubs: {
          VListItem: false,
          VNavigationDrawer: {
            template: '<div><slot /><slot name="append" /></div>',
          },
        },
      },
    });

    try {
      expect(wrapper.findComponent(AppearanceDialog).props("modelValue")).toBe(
        false
      );
      const appearanceItem = wrapper
        .findAllComponents({ name: "VListItem" })
        .find((item) => item.props("title") === "Darstellung");
      if (!appearanceItem) throw new Error("Appearance entry is missing");

      await appearanceItem.trigger("click");

      expect(wrapper.emitted("update:modelValue")).toEqual([[false]]);
      expect(wrapper.findComponent(AppearanceDialog).props("modelValue")).toBe(
        true
      );
    } finally {
      wrapper.unmount();
    }
  });
});
