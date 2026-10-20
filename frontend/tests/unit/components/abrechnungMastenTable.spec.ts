import type { AbrechnungMastForm } from "@/types/abrechnung/AbrechnungMastForm";

import { shallowMount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";

import AbrechnungMastenTable from "@/components/AbrechnungMastenTable.vue";
import SonarDateField from "@/components/common/SonarDateField.vue";

function mountTable(
  masten: AbrechnungMastForm[],
  removable = true,
  readonly = false
) {
  return shallowMount(AbrechnungMastenTable, {
    props: {
      modelValue: masten,
      idPrefix: "berechnung-nutzungsobjekt-0-mast",
      readonly,
      removable,
    },
    global: { renderStubDefaultSlot: true },
  });
}

function addButton(wrapper: ReturnType<typeof mountTable>) {
  return wrapper
    .findAllComponents({ name: "v-btn" })
    .find((button) => button.text().includes("Mast hinzufügen"));
}

function removeButton(wrapper: ReturnType<typeof mountTable>, nummer: number) {
  return wrapper
    .findAllComponents({ name: "v-btn" })
    .find(
      (button) => button.attributes("aria-label") === `Mast ${nummer} entfernen`
    );
}

function endeField(wrapper: ReturnType<typeof mountTable>, nummer: number) {
  return wrapper
    .findAllComponents(SonarDateField)
    .find(
      (field) => field.attributes("aria-label") === `Ende von Mast ${nummer}`
    );
}

describe("AbrechnungMastenTable.vue", () => {
  it("givenClickedHinzufuegenOnAnEmptyTable_thenAppendTwoMastenWithAnEmptyZeitraum", async () => {
    const masten: AbrechnungMastForm[] = [];
    const wrapper = mountTable(masten);

    await addButton(wrapper)?.trigger("click");

    expect(masten).toHaveLength(2);
    expect(masten[0]?.beginn).toBe("");
    expect(masten[0]?.ende).toBe("");
    expect(masten[1]?.beginn).toBe("");
    expect(masten[1]?.ende).toBe("");
  });

  it("givenClickedHinzufuegenWithAMastPresent_thenAppendOneMastWithAnEmptyZeitraum", async () => {
    const masten: AbrechnungMastForm[] = [
      { id: "mast-0001", beginn: "2026-01-01", ende: "2026-01-31" },
    ];
    const wrapper = mountTable(masten);

    await addButton(wrapper)?.trigger("click");

    expect(masten).toHaveLength(2);
    expect(masten[1]?.beginn).toBe("");
    expect(masten[1]?.ende).toBe("");
  });

  it("givenAMast_thenRunItsZeitraumInWholeMonate", () => {
    const wrapper = mountTable([
      { id: "mast-0001", beginn: "2026-01-01", ende: "2026-01-31" },
    ]);

    expect(endeField(wrapper, 1)?.props("zeitraumBeginn")).toBe("2026-01-01");
    expect(endeField(wrapper, 1)?.props("zeitraumEinheit")).toBe("monat");
  });

  it("givenClickedEntfernen_thenDropThatMast", async () => {
    const masten: AbrechnungMastForm[] = [
      { id: "mast-0001", beginn: "2026-01-01", ende: "2026-01-31" },
      { id: "mast-0002", beginn: "2026-02-01", ende: "2026-02-28" },
    ];
    const wrapper = mountTable(masten);

    await removeButton(wrapper, 1)?.trigger("click");

    expect(masten).toHaveLength(1);
    expect(masten[0]?.id).toBe("mast-0002");
  });

  it("givenNothingLeftToRemove_thenDisableEntfernen", () => {
    const wrapper = mountTable(
      [{ id: "mast-0001", beginn: "2026-01-01", ende: "2026-01-31" }],
      false
    );

    expect(removeButton(wrapper, 1)?.props("disabled")).toBe(true);
  });

  it("givenReadonly_thenOfferNeitherToAddNorToRemove", () => {
    const wrapper = mountTable(
      [{ id: "mast-0001", beginn: "2026-01-01", ende: "2026-01-31" }],
      true,
      true
    );

    expect(addButton(wrapper)).toBeUndefined();
    expect(removeButton(wrapper, 1)).toBeUndefined();
  });
});
