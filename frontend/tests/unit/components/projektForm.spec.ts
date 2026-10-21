import { mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import SonarDateField from "@/components/common/SonarDateField.vue";
import ProjektForm from "@/components/projekt/ProjektForm.vue";
import vuetify from "@/plugins/vuetify";

function mountForm() {
  return mount(ProjektForm, {
    props: { saving: false },
    global: { plugins: [vuetify] },
  });
}

function dateField(wrapper: ReturnType<typeof mountForm>, label: string) {
  return wrapper
    .findAllComponents(SonarDateField)
    .find(
      (field) =>
        field.findComponent({ name: "VDateInput" }).props("label") === label
    );
}

function allowedEnden(
  wrapper: ReturnType<typeof mountForm>
): (date: unknown) => boolean {
  return dateField(wrapper, "Abrechnung Ende")
    ?.findComponent({ name: "VDateInput" })
    .props("allowedDates") as (date: unknown) => boolean;
}

describe("ProjektForm.vue", () => {
  beforeEach(() => {
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

  it("givenAnAbrechnungBeginn_thenTakeEveryLaterDayAsEnde", async () => {
    const wrapper = mountForm();

    await dateField(wrapper, "Abrechnung Beginn")?.vm.$emit(
      "update:modelValue",
      "2026-01-05"
    );

    expect(allowedEnden(wrapper)(new Date(2026, 0, 15))).toBe(true);
    expect(allowedEnden(wrapper)(new Date(2026, 0, 5))).toBe(true);
  });

  it("givenAnAbrechnungBeginn_thenRejectAnEarlierEnde", async () => {
    const wrapper = mountForm();

    await dateField(wrapper, "Abrechnung Beginn")?.vm.$emit(
      "update:modelValue",
      "2026-01-05"
    );

    expect(allowedEnden(wrapper)(new Date(2026, 0, 4))).toBe(false);
  });
});
