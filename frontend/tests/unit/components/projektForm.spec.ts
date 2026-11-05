import type { StadtbezirkOption } from "@/types/projekt/StadtbezirkOption";

import { mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { ProjektRequestDTOStadtbezirkEnum } from "@/api/generated/sonar-backend";
import SonarDateField from "@/components/common/SonarDateField.vue";
import ProjektForm from "@/components/projekt/ProjektForm.vue";
import vuetify from "@/plugins/vuetify";

function mountForm(stadtbezirkOptions: StadtbezirkOption[] = []) {
  return mount(ProjektForm, {
    props: { saving: false, stadtbezirkOptions },
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

  it("givenStadtbezirkOptions_thenOfferOnlyThoseForSelection", () => {
    const sendling = {
      title: "06 Sendling",
      value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_06_SENDLING,
    };
    const wrapper = mountForm([sendling]);

    expect(
      wrapper.findComponent({ name: "VAutocomplete" }).props("items")
    ).toEqual([sendling]);
  });
});
