import { shallowMount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";
import AdresseFields from "@/components/common/AdresseFields.vue";
import { createProjektAdresse } from "@/util/projektAdresseForm";

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
