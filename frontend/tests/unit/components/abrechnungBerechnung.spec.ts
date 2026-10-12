import type { NutzungsobjektSuggestion } from "@/types/abrechnung/NutzungsobjektSuggestion";

import { shallowMount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";
import AbrechnungBerechnung from "@/components/AbrechnungBerechnung.vue";
import AbrechnungNutzungsobjektPanel from "@/components/AbrechnungNutzungsobjektPanel.vue";
import { useAbrechnungForm } from "@/composables/abrechnungForm";

function mountBerechnung(
  invalidNutzungsobjekte: number[] = [],
  readonly = false,
  nutzungsobjektSuggestions: NutzungsobjektSuggestion[] = []
) {
  const { abrechnung } = useAbrechnungForm();
  const wrapper = shallowMount(AbrechnungBerechnung, {
    props: {
      modelValue: abrechnung.value,
      suggestions: [],
      invalidNutzungsobjekte,
      nutzungsobjektSuggestions,
      readonly,
    },
    global: { renderStubDefaultSlot: true },
  });
  return { abrechnung, wrapper };
}

type Wrapper = ReturnType<typeof mountBerechnung>["wrapper"];

function activeTab(wrapper: Wrapper) {
  return wrapper.findComponent({ name: "v-tabs" }).props("modelValue");
}

async function addNutzungsobjekt(wrapper: Wrapper): Promise<void> {
  const button = wrapper
    .findAllComponents({ name: "v-btn" })
    .find((candidate) =>
      candidate.text().includes("Adresse/Flurstück hinzufügen")
    );
  await button?.trigger("click");
}

async function removeNutzungsobjekt(
  wrapper: Wrapper,
  index: number
): Promise<void> {
  await wrapper
    .findAllComponents(AbrechnungNutzungsobjektPanel)
    [index]?.vm.$emit("remove");
}

describe("AbrechnungBerechnung.vue", () => {
  it("givenAddedNutzungsobjekt_thenSelectItsTab", async () => {
    const { abrechnung, wrapper } = mountBerechnung();

    await addNutzungsobjekt(wrapper);

    expect(abrechnung.value.nutzungsobjekte).toHaveLength(2);
    expect(activeTab(wrapper)).toBe(abrechnung.value.nutzungsobjekte[1]?.id);
  });

  it("givenRemovedShownNutzungsobjekt_thenSelectARemainingTab", async () => {
    const { abrechnung, wrapper } = mountBerechnung();
    await addNutzungsobjekt(wrapper);
    const remaining = abrechnung.value.nutzungsobjekte[0]?.id;

    await removeNutzungsobjekt(wrapper, 1);

    expect(abrechnung.value.nutzungsobjekte).toHaveLength(1);
    expect(activeTab(wrapper)).toBe(remaining);
  });

  it("givenRemovedHiddenNutzungsobjekt_thenKeepTheSelection", async () => {
    const { abrechnung, wrapper } = mountBerechnung();
    await addNutzungsobjekt(wrapper);
    const shown = abrechnung.value.nutzungsobjekte[1]?.id;

    await removeNutzungsobjekt(wrapper, 0);

    expect(abrechnung.value.nutzungsobjekte).toHaveLength(1);
    expect(activeTab(wrapper)).toBe(shown);
  });

  it("givenSingleNutzungsobjekt_thenItCannotBeRemoved", () => {
    const { wrapper } = mountBerechnung();

    expect(
      wrapper.findComponent(AbrechnungNutzungsobjektPanel).props("removable")
    ).toBe(false);
  });

  it("givenErrorInHiddenNutzungsobjekt_thenSelectItsTab", async () => {
    const { abrechnung, wrapper } = mountBerechnung();
    await addNutzungsobjekt(wrapper);

    wrapper.vm.showError("berechnung-nutzungsobjekt-0-adresse");
    await wrapper.vm.$nextTick();

    expect(activeTab(wrapper)).toBe(abrechnung.value.nutzungsobjekte[0]?.id);
  });

  it("givenErrorOutsideTheNutzungsobjekte_thenKeepTheSelection", async () => {
    const { abrechnung, wrapper } = mountBerechnung();
    await addNutzungsobjekt(wrapper);
    const shown = abrechnung.value.nutzungsobjekte[1]?.id;

    wrapper.vm.showError("berechnung-zeitraum-von");
    await wrapper.vm.$nextTick();

    expect(activeTab(wrapper)).toBe(shown);
  });

  it("givenInvalidNutzungsobjekt_thenMarkOnlyItsTab", async () => {
    const { wrapper } = mountBerechnung([1]);
    await addNutzungsobjekt(wrapper);

    const tabs = wrapper.findAllComponents({ name: "v-tab" });
    expect(tabs[0]?.findComponent({ name: "v-icon" }).exists()).toBe(false);
    expect(tabs[1]?.findComponent({ name: "v-icon" }).exists()).toBe(true);
    expect(tabs[1]?.attributes("aria-label")).toBe("Adresse 2 enthält Fehler");
  });

  it("givenReadonly_thenOfferNoNutzungsobjektToAdd", () => {
    const { wrapper } = mountBerechnung([], true);

    const button = wrapper
      .findAllComponents({ name: "v-btn" })
      .find((candidate) =>
        candidate.text().includes("Adresse/Flurstück hinzufügen")
      );

    expect(button).toBeUndefined();
  });

  it("givenReadonly_thenPassItOnToTheNutzungsobjekte", () => {
    const { wrapper } = mountBerechnung([], true);

    expect(
      wrapper.findComponent(AbrechnungNutzungsobjektPanel).props("readonly")
    ).toBe(true);
  });

  it("givenNutzungsobjektTakenOverByOneEntry_thenOfferItToNoOtherEntry", async () => {
    const erstes: NutzungsobjektSuggestion = {
      id: "7c6b5a4d-3e2f-4a1b-9c8d-7e6f5a4b3c2d",
      art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
      adresse: "Marienplatz",
      hausnummerVon: "8",
      hausnummerBis: "",
      flurstueck: "",
      gemarkung: "",
      nutzung: null,
      unerlaubteNutzungVon: "",
      unerlaubteNutzungBis: "",
      tageUnerlaubteNutzung: null,
      bemerkung: "",
      aufschlag50prozent: false,
    };
    const zweites: NutzungsobjektSuggestion = {
      id: "2f3e4d5c-6b7a-4891-a0b1-c2d3e4f5a6b7",
      art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
      adresse: "Sendlinger Straße",
      hausnummerVon: "1",
      hausnummerBis: "",
      flurstueck: "",
      gemarkung: "",
      nutzung: null,
      unerlaubteNutzungVon: "",
      unerlaubteNutzungBis: "",
      tageUnerlaubteNutzung: null,
      bemerkung: "",
      aufschlag50prozent: false,
    };
    const { abrechnung, wrapper } = mountBerechnung([], false, [
      erstes,
      zweites,
    ]);
    await addNutzungsobjekt(wrapper);

    const ersterEintrag = abrechnung.value.nutzungsobjekte[0];
    if (ersterEintrag === undefined) {
      throw new Error("Die Abrechnung hat kein Nutzungsobjekt.");
    }
    ersterEintrag.uebernommenesNutzungsobjektId = erstes.id;
    await wrapper.vm.$nextTick();

    const panels = wrapper.findAllComponents(AbrechnungNutzungsobjektPanel);
    expect(panels[0]?.props("nutzungsobjektSuggestions")).toEqual([
      erstes,
      zweites,
    ]);
    expect(panels[1]?.props("nutzungsobjektSuggestions")).toEqual([zweites]);
  });
});
