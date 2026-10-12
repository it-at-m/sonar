import type { AbrechnungNutzungsobjektForm } from "@/types/abrechnung/AbrechnungNutzungsobjektForm";
import type { ProjektAdresseSuggestion } from "@/types/projekt/ProjektAdresseSuggestion";

import { shallowMount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungEnum,
} from "@/api/generated/sonar-backend";
import AbrechnungMastenTable from "@/components/AbrechnungMastenTable.vue";
import AbrechnungNutzungsobjektPanel from "@/components/AbrechnungNutzungsobjektPanel.vue";
import AbrechnungPositionenTable from "@/components/AbrechnungPositionenTable.vue";
import { createAbrechnungNutzungsobjekt } from "@/util/abrechnung/abrechnungNutzungsobjektForm";

const SUGGESTION: ProjektAdresseSuggestion = {
  art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
  adresse: "Marienplatz",
  hausnummerVon: "8",
  hausnummerBis: "",
  flurstueck: "",
  gemarkung: "",
  nutzung: ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A,
  unerlaubteNutzungVon: "2026-01-01",
  unerlaubteNutzungBis: "2026-01-31",
  tageUnerlaubteNutzung: 31,
};

function mountPanel(
  nutzungsobjekt: AbrechnungNutzungsobjektForm,
  suggestions: ProjektAdresseSuggestion[] = [],
  readonly = false
) {
  return shallowMount(AbrechnungNutzungsobjektPanel, {
    props: {
      modelValue: nutzungsobjekt,
      idPrefix: "berechnung-nutzungsobjekt-0",
      label: "Adresse 1",
      readonly,
      removable: true,
      suggestions,
    },
    global: { renderStubDefaultSlot: true },
  });
}

function suggestionEntries(wrapper: ReturnType<typeof mountPanel>) {
  return wrapper.findAllComponents({ name: "v-list-item" });
}

describe("AbrechnungNutzungsobjektPanel.vue", () => {
  it("givenLabel_thenNameTheEntryWithIt", () => {
    const wrapper = mountPanel(createAbrechnungNutzungsobjekt());

    expect(wrapper.text()).toContain("Adresse 1");
  });

  it("givenClickedRemove_thenAskTheParentToDropTheEntry", async () => {
    const wrapper = mountPanel(createAbrechnungNutzungsobjekt());

    await wrapper.find('[aria-label="Adresse 1 entfernen"]').trigger("click");

    expect(wrapper.emitted("remove")).toHaveLength(1);
  });

  it("givenProjektWithoutAdressen_thenOfferNothingToTakeOver", () => {
    const wrapper = mountPanel(createAbrechnungNutzungsobjekt());

    expect(suggestionEntries(wrapper)).toHaveLength(0);
  });

  it("givenProjektAdressen_thenOfferThemWithTheNutzungTheyBring", () => {
    const wrapper = mountPanel(createAbrechnungNutzungsobjekt(), [SUGGESTION]);

    const entries = suggestionEntries(wrapper);
    expect(entries).toHaveLength(1);
    expect(entries[0]?.props("title")).toBe("Marienplatz 8");
    expect(entries[0]?.props("subtitle")).toBe("Nutzung A");
  });

  it("givenTakenOverProjektAdresse_thenFillTheEntry", async () => {
    const nutzungsobjekt = createAbrechnungNutzungsobjekt();
    const wrapper = mountPanel(nutzungsobjekt, [SUGGESTION]);

    await suggestionEntries(wrapper)[0]?.vm.$emit("click");

    expect(nutzungsobjekt.adresse).toBe("Marienplatz");
    expect(nutzungsobjekt.hausnummerVon).toBe("8");
    expect(nutzungsobjekt.nutzung).toBe(
      ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A
    );
    expect(nutzungsobjekt.unerlaubteNutzungVon).toBe("2026-01-01");
    expect(nutzungsobjekt.tageUnerlaubteNutzung).toBe(31);
  });

  it("givenTakenOverProjektAdresse_thenKeepBemerkungAndPositionen", async () => {
    const nutzungsobjekt = createAbrechnungNutzungsobjekt();
    nutzungsobjekt.bemerkung = "Zweite Mahnung";
    const positionen = nutzungsobjekt.positionen;
    const wrapper = mountPanel(nutzungsobjekt, [SUGGESTION]);

    await suggestionEntries(wrapper)[0]?.vm.$emit("click");

    expect(nutzungsobjekt.bemerkung).toBe("Zweite Mahnung");
    expect(nutzungsobjekt.positionen).toBe(positionen);
  });

  it("givenReadonly_thenOfferNeitherToRemoveTheEntryNorToTakeOverAnAdresse", () => {
    const wrapper = mountPanel(
      createAbrechnungNutzungsobjekt(),
      [SUGGESTION],
      true
    );

    expect(wrapper.find('[aria-label="Adresse 1 entfernen"]').exists()).toBe(
      false
    );
    expect(suggestionEntries(wrapper)).toHaveLength(0);
  });

  it("givenReadonly_thenPassItOnToThePositionen", () => {
    const wrapper = mountPanel(createAbrechnungNutzungsobjekt(), [], true);

    expect(
      wrapper.findComponent(AbrechnungPositionenTable).props("readonly")
    ).toBe(true);
  });

  it("givenReadonly_thenPassItOnToTheMasten", () => {
    const wrapper = mountPanel(createAbrechnungNutzungsobjekt(), [], true);

    expect(wrapper.findComponent(AbrechnungMastenTable).props("readonly")).toBe(
      true
    );
  });

  it("givenOnePositionAndNoMasten_thenKeepTheLastRow", () => {
    const wrapper = mountPanel(createAbrechnungNutzungsobjekt());

    expect(
      wrapper.findComponent(AbrechnungPositionenTable).props("removable")
    ).toBe(false);
    expect(
      wrapper.findComponent(AbrechnungMastenTable).props("removable")
    ).toBe(false);
  });

  it("givenOnePositionAndOneMast_thenAllowRemovingBoth", () => {
    const nutzungsobjekt = createAbrechnungNutzungsobjekt();
    nutzungsobjekt.masten.push({
      id: "123e4567-e89b-12d3-a456-426614174004",
      beginn: "2026-01-01",
      ende: "2026-01-31",
    });
    const wrapper = mountPanel(nutzungsobjekt);

    expect(
      wrapper.findComponent(AbrechnungPositionenTable).props("removable")
    ).toBe(true);
    expect(
      wrapper.findComponent(AbrechnungMastenTable).props("removable")
    ).toBe(true);
  });

  it("givenNutzungsobjekt_thenOfferItsMasten", () => {
    const nutzungsobjekt = createAbrechnungNutzungsobjekt();
    nutzungsobjekt.masten.push({
      id: "123e4567-e89b-12d3-a456-426614174004",
      beginn: "2026-01-01",
      ende: "2026-01-31",
    });
    const wrapper = mountPanel(nutzungsobjekt);

    expect(
      wrapper.findComponent(AbrechnungMastenTable).props("modelValue")
    ).toStrictEqual(nutzungsobjekt.masten);
  });
});
