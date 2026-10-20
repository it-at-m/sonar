import { shallowMount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";

import SonarDateField from "@/components/common/SonarDateField.vue";

function mountField(props: Record<string, unknown>) {
  return shallowMount(SonarDateField, {
    props,
    global: { renderStubDefaultSlot: true },
  });
}

type Wrapper = ReturnType<typeof mountField>;

function dateInput(wrapper: Wrapper) {
  return wrapper.findComponent({ name: "VDateInput" });
}

async function pick(wrapper: Wrapper, date: Date | null): Promise<void> {
  await dateInput(wrapper).vm.$emit("update:modelValue", date);
}

function pickedDate(wrapper: Wrapper): string | undefined {
  return wrapper.emitted("update:modelValue")?.at(-1)?.[0] as string;
}

function allowedDates(wrapper: Wrapper): (date: unknown) => boolean {
  return dateInput(wrapper).props("allowedDates") as (date: unknown) => boolean;
}

function appendedRule(wrapper: Wrapper): (value: unknown) => boolean | string {
  const rules = dateInput(wrapper).props("rules") as ((
    value: unknown
  ) => boolean | string)[];
  return rules[rules.length - 1] as (value: unknown) => boolean | string;
}

describe("SonarDateField.vue", () => {
  it("givenNoZeitraumBeginn_thenKeepThePickedDate", async () => {
    const wrapper = mountField({ modelValue: "" });

    await pick(wrapper, new Date(2026, 0, 15));

    expect(pickedDate(wrapper)).toBe("2026-01-15");
  });

  it("givenADateWithinAWoche_thenSnapToTheEndeOfThatWoche", async () => {
    const wrapper = mountField({
      modelValue: "",
      zeitraumBeginn: "2026-01-05",
      zeitraumEinheit: "woche",
    });

    await pick(wrapper, new Date(2026, 0, 15));

    expect(pickedDate(wrapper)).toBe("2026-01-18");
  });

  it("givenNoEinheit_thenTakeEveryDateFromTheBeginnOn", async () => {
    const wrapper = mountField({
      modelValue: "",
      zeitraumBeginn: "2026-01-05",
    });

    await pick(wrapper, new Date(2026, 0, 15));

    expect(pickedDate(wrapper)).toBe("2026-01-15");
  });

  it("givenNoEinheitAndADateBeforeTheBeginn_thenSnapToTheBeginn", async () => {
    const wrapper = mountField({
      modelValue: "",
      zeitraumBeginn: "2026-01-05",
    });

    await pick(wrapper, new Date(2025, 11, 20));

    expect(pickedDate(wrapper)).toBe("2026-01-05");
  });

  it("givenTheEinheitMonat_thenSnapToTheEndeOfThatMonat", async () => {
    const wrapper = mountField({
      modelValue: "",
      zeitraumBeginn: "2026-01-15",
      zeitraumEinheit: "monat",
    });

    await pick(wrapper, new Date(2026, 1, 20));

    expect(pickedDate(wrapper)).toBe("2026-03-14");
  });

  it("givenAClearedField_thenKeepItAnEmptyString", async () => {
    const wrapper = mountField({ modelValue: "2026-01-15" });

    await pick(wrapper, null);

    expect(pickedDate(wrapper)).toBe("");
  });

  it("givenAMovedBeginn_thenMoveTheEndeByTheSameDays", async () => {
    const wrapper = mountField({
      modelValue: "2026-01-18",
      zeitraumBeginn: "2026-01-05",
      zeitraumEinheit: "woche",
    });

    await wrapper.setProps({ zeitraumBeginn: "2026-01-06" });

    expect(pickedDate(wrapper)).toBe("2026-01-19");
  });

  it("givenNoEinheitAndAMovedBeginn_thenLeaveTheEndeAlone", async () => {
    const wrapper = mountField({
      modelValue: "2026-12-31",
      zeitraumBeginn: "2026-01-01",
    });

    await wrapper.setProps({ zeitraumBeginn: "2026-01-02" });

    expect(pickedDate(wrapper)).toBeUndefined();
  });

  it("givenNoEinheitAndABeginnMovedPastTheEnde_thenTakeTheBeginn", async () => {
    const wrapper = mountField({
      modelValue: "2026-01-15",
      zeitraumBeginn: "2026-01-05",
    });

    await wrapper.setProps({ zeitraumBeginn: "2026-01-20" });

    expect(pickedDate(wrapper)).toBe("2026-01-20");
  });

  it("givenAMovedBeginnAndAnEndeThatClosesNoPeriod_thenSnapToTheNextOne", async () => {
    const wrapper = mountField({
      modelValue: "2026-01-15",
      zeitraumBeginn: "",
      zeitraumEinheit: "woche",
    });

    await wrapper.setProps({ zeitraumBeginn: "2026-01-05" });

    expect(pickedDate(wrapper)).toBe("2026-01-18");
  });

  it("givenZeitraumBeginn_thenOfferOnlyTheEndenOfWholeWochenToTheCalendar", () => {
    const wrapper = mountField({
      modelValue: "",
      zeitraumBeginn: "2026-01-05",
      zeitraumEinheit: "woche",
    });

    expect(allowedDates(wrapper)(new Date(2026, 0, 18))).toBe(true);
    expect(allowedDates(wrapper)(new Date(2026, 0, 15))).toBe(false);
  });

  it("givenNoEinheit_thenOfferEveryDayFromTheBeginnOnToTheCalendar", () => {
    const wrapper = mountField({
      modelValue: "",
      zeitraumBeginn: "2026-01-05",
    });

    expect(allowedDates(wrapper)(new Date(2026, 0, 15))).toBe(true);
    expect(allowedDates(wrapper)(new Date(2026, 0, 4))).toBe(false);
  });

  it("givenTheEinheitMonat_thenOfferOnlyTheEndenOfWholeMonateToTheCalendar", () => {
    const wrapper = mountField({
      modelValue: "",
      zeitraumBeginn: "2026-01-15",
      zeitraumEinheit: "monat",
    });

    expect(allowedDates(wrapper)(new Date(2026, 1, 14))).toBe(true);
    expect(allowedDates(wrapper)(new Date(2026, 1, 20))).toBe(false);
  });

  it("givenNoZeitraumBeginn_thenOfferEveryDay", () => {
    expect(allowedDates(mountField({ modelValue: "" }))).toBeUndefined();
  });

  it("givenZeitraumBeginn_thenOfferTheEarliestEndeToThePicker", () => {
    const tage = mountField({ modelValue: "", zeitraumBeginn: "2026-01-05" });
    const wochen = mountField({
      modelValue: "",
      zeitraumBeginn: "2026-01-05",
      zeitraumEinheit: "woche",
    });
    const monate = mountField({
      modelValue: "",
      zeitraumBeginn: "2026-01-15",
      zeitraumEinheit: "monat",
    });

    expect(dateInput(tage).props("min")).toBe("2026-01-05");
    expect(dateInput(wochen).props("min")).toBe("2026-01-11");
    expect(dateInput(monate).props("min")).toBe("2026-02-14");
  });

  it("givenNoZeitraumBeginn_thenLeaveTheEarliestEndeOpen", () => {
    expect(
      dateInput(mountField({ modelValue: "" })).props("min")
    ).toBeUndefined();
  });

  it("givenZeitraumBeginn_thenKeepTheGivenRulesAndRejectAPartialPeriod", () => {
    const wrapper = mountField({
      modelValue: "",
      rules: [() => "Pflichtfeld"],
      zeitraumBeginn: "2026-01-05",
      zeitraumEinheit: "woche",
    });

    expect(dateInput(wrapper).props("rules")).toHaveLength(2);
    expect(appendedRule(wrapper)("2026-01-15")).toBe(
      "Der Zeitraum muss volle Wochen umfassen."
    );
    expect(appendedRule(wrapper)("2026-01-18")).toBe(true);
  });

  it("givenTheAdapterValidatesADate_thenReadItAsAnIsoDate", () => {
    const wrapper = mountField({
      modelValue: "",
      zeitraumBeginn: "2026-01-05",
      zeitraumEinheit: "woche",
    });

    expect(appendedRule(wrapper)(new Date(2026, 0, 18))).toBe(true);
    expect(appendedRule(wrapper)(new Date(2026, 0, 15))).toBe(
      "Der Zeitraum muss volle Wochen umfassen."
    );
  });

  it("givenReadonly_thenMakeTheCalendarInert", () => {
    const wrapper = mountField({ modelValue: "", readonly: true });

    expect(dateInput(wrapper).props("pickerProps")).toEqual({ disabled: true });
  });

  it("givenNoReadonly_thenLeaveTheCalendarToTheForm", () => {
    expect(
      dateInput(mountField({ modelValue: "" })).props("pickerProps")
    ).toBeUndefined();
  });

  it("givenAnyField_thenKeepTheCalendarIconInsideIt", () => {
    const wrapper = mountField({ modelValue: "" });

    expect(dateInput(wrapper).props("prependIcon")).toBe("");
    expect(dateInput(wrapper).props("prependInnerIcon")).toBe("$calendar");
  });
});
