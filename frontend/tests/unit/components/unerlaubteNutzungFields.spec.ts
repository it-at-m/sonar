import type { UnerlaubteNutzung } from "@/types/common/UnerlaubteNutzung";

import { shallowMount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";

import SonarDateField from "@/components/common/SonarDateField.vue";
import UnerlaubteNutzungFields from "@/components/common/UnerlaubteNutzungFields.vue";

function mountFields(unerlaubteNutzung: Partial<UnerlaubteNutzung> = {}) {
  return shallowMount(UnerlaubteNutzungFields, {
    props: {
      modelValue: {
        unerlaubteNutzungVon: "",
        unerlaubteNutzungBis: "",
        tageUnerlaubteNutzung: null,
        ...unerlaubteNutzung,
      },
      idPrefix: "adresse-0",
    },
    global: { renderStubDefaultSlot: true },
  });
}

function tageInput(wrapper: ReturnType<typeof mountFields>) {
  return wrapper.findComponent({ name: "v-number-input" });
}

describe("UnerlaubteNutzungFields.vue", () => {
  it("givenZeitraum_thenShowTheDaysItSpansAndLockThem", () => {
    const wrapper = mountFields({
      unerlaubteNutzungVon: "2026-01-01",
      unerlaubteNutzungBis: "2026-01-31",
    });

    expect(tageInput(wrapper).props("modelValue")).toBe(31);
    expect(tageInput(wrapper).props("disabled")).toBe(true);
  });

  it("givenNoZeitraum_thenTakeTheDaysAsEntered", async () => {
    const unerlaubteNutzung: UnerlaubteNutzung = {
      unerlaubteNutzungVon: "",
      unerlaubteNutzungBis: "",
      tageUnerlaubteNutzung: null,
    };
    const wrapper = shallowMount(UnerlaubteNutzungFields, {
      props: { modelValue: unerlaubteNutzung, idPrefix: "adresse-0" },
      global: { renderStubDefaultSlot: true },
    });

    await tageInput(wrapper).vm.$emit("update:modelValue", 7);

    expect(unerlaubteNutzung.tageUnerlaubteNutzung).toBe(7);
    expect(tageInput(wrapper).props("disabled")).toBeNull();
  });

  it("givenDisabled_thenDisableEveryField", () => {
    const wrapper = shallowMount(UnerlaubteNutzungFields, {
      props: {
        modelValue: {
          unerlaubteNutzungVon: "",
          unerlaubteNutzungBis: "",
          tageUnerlaubteNutzung: null,
        },
        idPrefix: "adresse-0",
        disabled: true,
      },
      global: { renderStubDefaultSlot: true },
    });

    expect(
      wrapper
        .findAllComponents(SonarDateField)
        .map((field) => field.attributes("disabled"))
    ).toStrictEqual(["true", "true"]);
    expect(tageInput(wrapper).props("disabled")).toBe(true);
  });
});
