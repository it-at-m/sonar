import { shallowMount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungenEnum,
} from "@/api/generated/sonar-backend";
import AdresseFields from "@/components/common/AdresseFields.vue";
import { createProjektAdresse } from "@/util/projekt/projektAdresseForm";

function fieldsFor(art: ProjektAdresseRequestDTOArtEnum) {
  return shallowMount(AdresseFields, {
    props: {
      modelValue: { ...createProjektAdresse(), art },
      idPrefix: "adresse-0",
    },
    global: { renderStubDefaultSlot: true },
  });
}

function labelsFor(
  art: ProjektAdresseRequestDTOArtEnum
): (string | undefined)[] {
  return fieldsFor(art)
    .findAllComponents({ name: "v-text-field" })
    .map((field) => field.props("label") as string | undefined);
}

describe("AdresseFields.vue", () => {
  it("givenArtAdresse_thenOfferAdresseAndHausnummern", () => {
    const labels = labelsFor(ProjektAdresseRequestDTOArtEnum.ADRESSE);

    expect(labels).toContain("Adresse");
    expect(labels).toContain("Hausnummer von");
    expect(labels).toContain("Hausnummer bis");
    expect(labels).not.toContain("Flurstück");
    expect(labels).not.toContain("Gemarkung");
  });

  it("givenArtFlurstueck_thenOfferFlurstueckAndGemarkung", () => {
    const labels = labelsFor(ProjektAdresseRequestDTOArtEnum.FLURSTUECK);

    expect(labels).toContain("Flurstück");
    expect(labels).toContain("Gemarkung");
    expect(labels).not.toContain("Adresse");
    expect(labels).not.toContain("Hausnummer von");
  });

  it("givenNutzungSonstiges_thenOfferTheBeschreibung", () => {
    const adresse = {
      ...createProjektAdresse(),
      nutzungen: [ProjektAdresseRequestDTONutzungenEnum.SONSTIGES],
    };
    const wrapper = shallowMount(AdresseFields, {
      props: { modelValue: adresse, idPrefix: "adresse-0" },
      global: { renderStubDefaultSlot: true },
    });

    const beschreibung = wrapper.find("#adresse-0-nutzung-sonstiges");

    expect(beschreibung.exists()).toBe(true);
  });

  it("givenAndereNutzung_thenHideTheBeschreibung", () => {
    const adresse = {
      ...createProjektAdresse(),
      nutzungen: [ProjektAdresseRequestDTONutzungenEnum.BAUZAUN],
    };
    const wrapper = shallowMount(AdresseFields, {
      props: { modelValue: adresse, idPrefix: "adresse-0" },
      global: { renderStubDefaultSlot: true },
    });

    expect(wrapper.find("#adresse-0-nutzung-sonstiges").exists()).toBe(false);
  });

  it("givenNutzungChangedAwayFromSonstiges_thenDropTheBeschreibung", async () => {
    const adresse = {
      ...createProjektAdresse(),
      nutzungen: [ProjektAdresseRequestDTONutzungenEnum.SONSTIGES],
      nutzungSonstiges: "Gerüst über dem Gehweg",
    };
    const wrapper = shallowMount(AdresseFields, {
      props: { modelValue: adresse, idPrefix: "adresse-0" },
      global: { renderStubDefaultSlot: true },
    });

    await wrapper
      .findComponent({ name: "v-select" })
      .vm.$emit("update:modelValue", [
        ProjektAdresseRequestDTONutzungenEnum.BAUZAUN,
      ]);

    expect(adresse.nutzungSonstiges).toBe("");
  });

  it("givenSonstigesKeptAmongSeveral_thenKeepTheBeschreibung", async () => {
    const adresse = {
      ...createProjektAdresse(),
      nutzungen: [ProjektAdresseRequestDTONutzungenEnum.SONSTIGES],
      nutzungSonstiges: "Gerüst über dem Gehweg",
    };
    const wrapper = shallowMount(AdresseFields, {
      props: { modelValue: adresse, idPrefix: "adresse-0" },
      global: { renderStubDefaultSlot: true },
    });

    await wrapper
      .findComponent({ name: "v-select" })
      .vm.$emit("update:modelValue", [
        ProjektAdresseRequestDTONutzungenEnum.SONSTIGES,
        ProjektAdresseRequestDTONutzungenEnum.BAUZAUN,
      ]);

    expect(adresse.nutzungSonstiges).toBe("Gerüst über dem Gehweg");
    expect(wrapper.find("#adresse-0-nutzung-sonstiges").exists()).toBe(true);
  });

  it("givenNutzung_thenOfferEveryOptionForSeveralChoices", () => {
    const select = fieldsFor(
      ProjektAdresseRequestDTOArtEnum.ADRESSE
    ).findComponent({ name: "v-select" });

    expect(select.props("multiple")).toBe(true);
    expect(select.props("items")).toHaveLength(13);
  });

  it("givenIdPrefix_thenIdEveryField", () => {
    const ids = fieldsFor(ProjektAdresseRequestDTOArtEnum.ADRESSE)
      .findAllComponents({ name: "v-text-field" })
      .map((field) => field.attributes("id"));

    expect(ids).toStrictEqual([
      "adresse-0-adresse",
      "adresse-0-hausnummer-von",
      "adresse-0-hausnummer-bis",
    ]);
  });
});
