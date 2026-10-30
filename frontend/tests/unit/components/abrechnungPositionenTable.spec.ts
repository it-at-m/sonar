import type { AbrechnungPositionForm } from "@/types/abrechnung/AbrechnungPositionForm";

import { shallowMount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";

import AbrechnungPositionenTable from "@/components/AbrechnungPositionenTable.vue";
import { createAbrechnungPosition } from "@/util/abrechnung/abrechnungPositionForm";

function mountTable(positionen: AbrechnungPositionForm[], readonly = false) {
  return shallowMount(AbrechnungPositionenTable, {
    props: {
      modelValue: positionen,
      idPrefix: "berechnung-nutzungsobjekt-0-position",
      readonly,
      removable: true,
    },
    global: { renderStubDefaultSlot: true },
  });
}

function isDisabled(
  wrapper: ReturnType<typeof mountTable>,
  ariaLabel: string
): boolean {
  const field = wrapper
    .findAll("sonar-date-field-stub")
    .find((candidate) => candidate.attributes("aria-label") === ariaLabel);
  if (field === undefined) {
    throw new Error(`Kein Datumsfeld "${ariaLabel}".`);
  }
  return field.attributes("disabled") === "true";
}

describe("AbrechnungPositionenTable.vue", () => {
  it("givenEditableForm_thenOnlyLeaveTheFurtherBeginneOpen", () => {
    const wrapper = mountTable([
      createAbrechnungPosition(),
      createAbrechnungPosition(),
    ]);

    expect(isDisabled(wrapper, "Beginn der Position 1")).toBe(true);
    expect(isDisabled(wrapper, "Ende der Position 1")).toBe(true);
    expect(isDisabled(wrapper, "Beginn der Position 2")).toBe(false);
    expect(isDisabled(wrapper, "Ende der Position 2")).toBe(true);
  });

  it("givenReadonly_thenDisableNoDate", () => {
    const wrapper = mountTable(
      [createAbrechnungPosition(), createAbrechnungPosition()],
      true
    );

    expect(isDisabled(wrapper, "Beginn der Position 1")).toBe(false);
    expect(isDisabled(wrapper, "Ende der Position 1")).toBe(false);
    expect(isDisabled(wrapper, "Beginn der Position 2")).toBe(false);
    expect(isDisabled(wrapper, "Ende der Position 2")).toBe(false);
  });
});
