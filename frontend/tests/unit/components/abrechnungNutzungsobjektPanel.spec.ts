import type { AbrechnungNutzungsobjektForm } from "@/types/abrechnung/AbrechnungNutzungsobjektForm";
import type { NutzungsobjektSuggestion } from "@/types/abrechnung/NutzungsobjektSuggestion";
import type { ProjektAdresseSuggestion } from "@/types/projekt/ProjektAdresseSuggestion";

import { shallowMount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungEnum,
} from "@/api/generated/sonar-backend";
import AbrechnungNutzungsobjektPanel from "@/components/AbrechnungNutzungsobjektPanel.vue";
import AbrechnungPositionenTable from "@/components/AbrechnungPositionenTable.vue";
import AdresseFields from "@/components/common/AdresseFields.vue";
import UnerlaubteNutzungFields from "@/components/common/UnerlaubteNutzungFields.vue";
import { createAbrechnungNutzungsobjekt } from "@/util/abrechnung/abrechnungNutzungsobjektForm";

const NUTZUNGSOBJEKT_ID = "7c6b5a4d-3e2f-4a1b-9c8d-7e6f5a4b3c2d";

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

const NUTZUNGSOBJEKT_SUGGESTION: NutzungsobjektSuggestion = {
  id: NUTZUNGSOBJEKT_ID,
  art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
  adresse: "Sendlinger Straße",
  hausnummerVon: "1",
  hausnummerBis: "3",
  flurstueck: "",
  gemarkung: "",
  nutzung: ProjektAdresseRequestDTONutzungEnum.NUTZUNG_B,
  unerlaubteNutzungVon: "2026-02-01",
  unerlaubteNutzungBis: "2026-02-28",
  tageUnerlaubteNutzung: 28,
  bemerkung: "Aus der Erstabrechnung",
  aufschlag50prozent: true,
};

function mountPanel(
  nutzungsobjekt: AbrechnungNutzungsobjektForm,
  suggestions: ProjektAdresseSuggestion[] = [],
  readonly = false,
  nutzungsobjektSuggestions: NutzungsobjektSuggestion[] = []
) {
  return shallowMount(AbrechnungNutzungsobjektPanel, {
    props: {
      modelValue: nutzungsobjekt,
      idPrefix: "berechnung-nutzungsobjekt-0",
      label: "Adresse 1",
      nutzungsobjektSuggestions,
      readonly,
      removable: true,
      suggestions,
    },
    global: { renderStubDefaultSlot: true },
  });
}

function entriesOf(wrapper: ReturnType<typeof mountPanel>, listId: string) {
  const list = wrapper.findComponent(`#berechnung-nutzungsobjekt-0-${listId}`);
  return list.exists() ? list.findAllComponents({ name: "v-list-item" }) : [];
}

function suggestionEntries(wrapper: ReturnType<typeof mountPanel>) {
  return entriesOf(wrapper, "projekt-adressen");
}

function nutzungsobjektEntries(wrapper: ReturnType<typeof mountPanel>) {
  return entriesOf(wrapper, "nutzungsobjekte");
}

function aufhebenButton(wrapper: ReturnType<typeof mountPanel>) {
  return wrapper.find("#berechnung-nutzungsobjekt-0-uebernahme-aufheben");
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

  it("givenProjektWithoutNutzungsobjekte_thenOfferNothingToTakeOver", () => {
    const wrapper = mountPanel(createAbrechnungNutzungsobjekt());

    expect(nutzungsobjektEntries(wrapper)).toHaveLength(0);
  });

  it("givenNutzungsobjekteOfTheProjekt_thenOfferThemWithTheNutzungTheyBring", () => {
    const wrapper = mountPanel(createAbrechnungNutzungsobjekt(), [], false, [
      NUTZUNGSOBJEKT_SUGGESTION,
    ]);

    const entries = nutzungsobjektEntries(wrapper);
    expect(entries).toHaveLength(1);
    expect(entries[0]?.props("title")).toBe("Sendlinger Straße 1–3");
    expect(entries[0]?.props("subtitle")).toBe("Nutzung B");
  });

  it("givenTakenOverNutzungsobjekt_thenFillTheEntryAndNameTheBilledOne", async () => {
    const nutzungsobjekt = createAbrechnungNutzungsobjekt();
    const wrapper = mountPanel(nutzungsobjekt, [], false, [
      NUTZUNGSOBJEKT_SUGGESTION,
    ]);

    await nutzungsobjektEntries(wrapper)[0]?.vm.$emit("click");

    expect(nutzungsobjekt.uebernommenesNutzungsobjektId).toBe(
      NUTZUNGSOBJEKT_ID
    );
    expect(nutzungsobjekt.adresse).toBe("Sendlinger Straße");
    expect(nutzungsobjekt.bemerkung).toBe("Aus der Erstabrechnung");
    expect(nutzungsobjekt.aufschlag50prozent).toBe(true);
  });

  it("givenTakenOverNutzungsobjekt_thenKeepThePositionen", async () => {
    const nutzungsobjekt = createAbrechnungNutzungsobjekt();
    const positionen = nutzungsobjekt.positionen;
    const wrapper = mountPanel(nutzungsobjekt, [], false, [
      NUTZUNGSOBJEKT_SUGGESTION,
    ]);

    await nutzungsobjektEntries(wrapper)[0]?.vm.$emit("click");

    expect(nutzungsobjekt.positionen).toBe(positionen);
  });

  it("givenReleasedNutzungsobjekt_thenEnterANewOneAgain", async () => {
    const nutzungsobjekt = createAbrechnungNutzungsobjekt();
    nutzungsobjekt.uebernommenesNutzungsobjektId = NUTZUNGSOBJEKT_ID;
    const wrapper = mountPanel(nutzungsobjekt, [], false, [
      NUTZUNGSOBJEKT_SUGGESTION,
    ]);

    await aufhebenButton(wrapper).trigger("click");

    expect(nutzungsobjekt.uebernommenesNutzungsobjektId).toBeNull();
    expect(wrapper.findComponent(AdresseFields).props("readonly")).toBe(false);
    expect(nutzungsobjektEntries(wrapper)).toHaveLength(1);
  });

  it("givenChosenNutzungsobjekt_thenLockEveryFieldButThePositionen", () => {
    const nutzungsobjekt = createAbrechnungNutzungsobjekt();
    nutzungsobjekt.uebernommenesNutzungsobjektId = NUTZUNGSOBJEKT_ID;

    const wrapper = mountPanel(nutzungsobjekt, [], false, [
      NUTZUNGSOBJEKT_SUGGESTION,
    ]);

    expect(wrapper.findComponent(AdresseFields).props("readonly")).toBe(true);
    expect(
      wrapper.findComponent(UnerlaubteNutzungFields).props("readonly")
    ).toBe(true);
    expect(
      wrapper.findComponent(AbrechnungPositionenTable).props("readonly")
    ).toBe(false);
  });

  it("givenChosenNutzungsobjekt_thenOfferNoTakeOverButARelease", () => {
    const nutzungsobjekt = createAbrechnungNutzungsobjekt();
    nutzungsobjekt.uebernommenesNutzungsobjektId = NUTZUNGSOBJEKT_ID;

    const wrapper = mountPanel(nutzungsobjekt, [SUGGESTION], false, [
      NUTZUNGSOBJEKT_SUGGESTION,
    ]);

    expect(suggestionEntries(wrapper)).toHaveLength(0);
    expect(nutzungsobjektEntries(wrapper)).toHaveLength(0);
    expect(aufhebenButton(wrapper).exists()).toBe(true);
  });

  it("givenReadonly_thenOfferNeitherTheTakeOverNorTheRelease", () => {
    const nutzungsobjekt = createAbrechnungNutzungsobjekt();
    nutzungsobjekt.uebernommenesNutzungsobjektId = NUTZUNGSOBJEKT_ID;

    const wrapper = mountPanel(nutzungsobjekt, [], true, [
      NUTZUNGSOBJEKT_SUGGESTION,
    ]);

    expect(nutzungsobjektEntries(wrapper)).toHaveLength(0);
    expect(aufhebenButton(wrapper).exists()).toBe(false);
  });
});
