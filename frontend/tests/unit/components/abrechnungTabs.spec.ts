import type { AbrechnungForm } from "@/composables/abrechnungForm";

import { enableAutoUnmount, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";
import AbrechnungBerechnung from "@/components/AbrechnungBerechnung.vue";
import AbrechnungTabs from "@/components/AbrechnungTabs.vue";
import vuetify from "@/plugins/vuetify";
import { TABS } from "@/util/abrechnungTabs";

function mountTabs(
  abrechnung: AbrechnungForm,
  props: { invalidNutzungsobjekte?: number[]; readonly?: boolean } = {}
) {
  return mount(AbrechnungTabs, {
    props: { modelValue: abrechnung, ...props },
    global: { plugins: [vuetify] },
  });
}

type Tabs = ReturnType<typeof mountTabs>;

function selectedTab(wrapper: Tabs) {
  return wrapper.findAllComponents({ name: "VTabs" })[0]?.props("modelValue");
}

function selectedNutzungsobjekt(wrapper: Tabs) {
  return wrapper.findAllComponents({ name: "VTabs" })[1]?.props("modelValue");
}

enableAutoUnmount(afterEach);

describe("AbrechnungTabs.vue", () => {
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
  });

  it("givenErrorOnTheOtherTab_thenSelectThatTab", async () => {
    const wrapper = mountTabs({
      geschaeftspartnerId: "",
      zustellungsbevollmaechtigterGenutzt: false,
      zustellungsbevollmaechtigterId: "",
      zustellungsbevollmaechtigterTyp: null,
      zeitraumVon: "",
      zeitraumBis: "",
      abrechnungsArt: null,
      nutzungsobjekte: [
        {
          id: "nutzungsobjekt-0",
          art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
          adresse: "",
          hausnummerVon: "",
          hausnummerBis: "",
          flurstueck: "",
          gemarkung: "",
          nutzung: null,
          unerlaubteNutzungVon: "",
          unerlaubteNutzungBis: "",
          tageUnerlaubteNutzung: null,
          bemerkung: "",
          positionen: [],
        },
      ],
    });

    wrapper.vm.showError("berechnung-zeitraum-von");
    await wrapper.vm.$nextTick();

    expect(selectedTab(wrapper)).toBe(TABS.BERECHNUNG);
  });

  it("givenErrorOutsideTheTabs_thenKeepTheSelection", async () => {
    const wrapper = mountTabs({
      geschaeftspartnerId: "",
      zustellungsbevollmaechtigterGenutzt: false,
      zustellungsbevollmaechtigterId: "",
      zustellungsbevollmaechtigterTyp: null,
      zeitraumVon: "",
      zeitraumBis: "",
      abrechnungsArt: null,
      nutzungsobjekte: [
        {
          id: "nutzungsobjekt-0",
          art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
          adresse: "",
          hausnummerVon: "",
          hausnummerBis: "",
          flurstueck: "",
          gemarkung: "",
          nutzung: null,
          unerlaubteNutzungVon: "",
          unerlaubteNutzungBis: "",
          tageUnerlaubteNutzung: null,
          bemerkung: "",
          positionen: [],
        },
      ],
    });

    wrapper.vm.showError("irgendein-feld");
    await wrapper.vm.$nextTick();

    expect(selectedTab(wrapper)).toBe(TABS.BASIS);
  });

  it("givenErrorInAHiddenNutzungsobjekt_thenSelectItsTabAsWell", async () => {
    const wrapper = mountTabs({
      geschaeftspartnerId: "",
      zustellungsbevollmaechtigterGenutzt: false,
      zustellungsbevollmaechtigterId: "",
      zustellungsbevollmaechtigterTyp: null,
      zeitraumVon: "",
      zeitraumBis: "",
      abrechnungsArt: null,
      nutzungsobjekte: [
        {
          id: "nutzungsobjekt-0",
          art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
          adresse: "Musterstraße",
          hausnummerVon: "1",
          hausnummerBis: "",
          flurstueck: "",
          gemarkung: "",
          nutzung: null,
          unerlaubteNutzungVon: "",
          unerlaubteNutzungBis: "",
          tageUnerlaubteNutzung: null,
          bemerkung: "",
          positionen: [],
        },
        {
          id: "nutzungsobjekt-1",
          art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
          adresse: "",
          hausnummerVon: "",
          hausnummerBis: "",
          flurstueck: "",
          gemarkung: "",
          nutzung: null,
          unerlaubteNutzungVon: "",
          unerlaubteNutzungBis: "",
          tageUnerlaubteNutzung: null,
          bemerkung: "",
          positionen: [],
        },
      ],
    });

    wrapper.vm.showError("berechnung-nutzungsobjekt-1-adresse");
    await wrapper.vm.$nextTick();

    expect(selectedTab(wrapper)).toBe(TABS.BERECHNUNG);
    expect(selectedNutzungsobjekt(wrapper)).toBe("nutzungsobjekt-1");
  });

  it("givenInvalidNutzungsobjekte_thenPassThemOnToTheBerechnung", () => {
    const wrapper = mountTabs(
      {
        geschaeftspartnerId: "",
        zustellungsbevollmaechtigterGenutzt: false,
        zustellungsbevollmaechtigterId: "",
        zustellungsbevollmaechtigterTyp: null,
        zeitraumVon: "",
        zeitraumBis: "",
        abrechnungsArt: null,
        nutzungsobjekte: [
          {
            id: "nutzungsobjekt-0",
            art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
            adresse: "",
            hausnummerVon: "",
            hausnummerBis: "",
            flurstueck: "",
            gemarkung: "",
            nutzung: null,
            unerlaubteNutzungVon: "",
            unerlaubteNutzungBis: "",
            tageUnerlaubteNutzung: null,
            bemerkung: "",
            positionen: [],
          },
        ],
      },
      { invalidNutzungsobjekte: [0] }
    );

    expect(
      wrapper
        .findComponent(AbrechnungBerechnung)
        .props("invalidNutzungsobjekte")
    ).toEqual([0]);
  });

  it("givenReadonly_thenPassItOnToTheBerechnung", () => {
    const wrapper = mountTabs(
      {
        geschaeftspartnerId: "",
        zustellungsbevollmaechtigterGenutzt: false,
        zustellungsbevollmaechtigterId: "",
        zustellungsbevollmaechtigterTyp: null,
        zeitraumVon: "",
        zeitraumBis: "",
        abrechnungsArt: null,
        nutzungsobjekte: [
          {
            id: "nutzungsobjekt-0",
            art: ProjektAdresseRequestDTOArtEnum.ADRESSE,
            adresse: "",
            hausnummerVon: "",
            hausnummerBis: "",
            flurstueck: "",
            gemarkung: "",
            nutzung: null,
            unerlaubteNutzungVon: "",
            unerlaubteNutzungBis: "",
            tageUnerlaubteNutzung: null,
            bemerkung: "",
            positionen: [],
          },
        ],
      },
      { readonly: true }
    );

    expect(wrapper.findComponent(AbrechnungBerechnung).props("readonly")).toBe(
      true
    );
  });
});
