import type { AbrechnungTableRow } from "@/types/AbrechnungTableRow";

import { mdiChatAlert, mdiChatPlus } from "@mdi/js";
import { mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import AbrechnungTable from "@/components/AbrechnungTable.vue";
import vuetify from "@/plugins/vuetify";

const PROJEKT_ID = "123e4567-e89b-12d3-a456-426614174000";

function mountTable(rows: AbrechnungTableRow[] = []) {
  return mount(AbrechnungTable, {
    props: {
      page: 1,
      itemsPerPage: 10,
      sortBy: [{ key: "zeitraumVon", order: "desc" as const }],
      projektId: PROJEKT_ID,
      rows,
      totalAbrechnungen: rows.length,
      loading: false,
    },
    global: { plugins: [vuetify] },
  });
}

function widerspruchButton(wrapper: ReturnType<typeof mountTable>) {
  return wrapper.find('[aria-label*="Widerspruch"]');
}

function widerspruchIconPath(wrapper: ReturnType<typeof mountTable>) {
  return widerspruchButton(wrapper).find(".v-icon path").attributes("d");
}

describe("AbrechnungTable.vue", () => {
  beforeEach(() => {
    // jsdom has none, and the data table observes its container to lay the table out.
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

  it("givenSortedColumn_thenOnlyItCarriesASortBadge", () => {
    const wrapper = mountTable();

    const sortableHeaders = wrapper.findAll("th.v-data-table__th--sortable");
    const headersWithBadge = sortableHeaders.filter((header) =>
      header
        .find(".v-data-table-header__content .v-data-table-header__sort-badge")
        .exists()
    );

    expect(sortableHeaders).toHaveLength(4);
    expect(headersWithBadge).toHaveLength(1);
    expect(headersWithBadge[0]?.text()).toContain("Zeitraum von");
  });

  it("givenColumnThatCannotBeSorted_thenItIsNotMarkedSortable", () => {
    const wrapper = mountTable();

    const nutzungsobjekte = wrapper
      .findAll("th")
      .find((header) => header.text().startsWith("Nutzungsobjekte"));

    expect(nutzungsobjekte?.classes()).not.toContain(
      "v-data-table__th--sortable"
    );
  });

  it("givenWiderspruchColumn_thenItIsNotMarkedSortable", () => {
    const wrapper = mountTable();

    const widerspruch = wrapper
      .findAll("th")
      .find((header) => header.text().startsWith("Widerspruch"));

    expect(widerspruch?.classes()).not.toContain("v-data-table__th--sortable");
  });

  it("givenAbrechnungWithoutWiderspruch_thenOfferToAnlegenIt", () => {
    const wrapper = mountTable([
      {
        id: "123e4567-e89b-12d3-a456-426614174001",
        geschaeftspartnerId: "1000000001",
        zeitraumVon: "01.01.2026",
        zeitraumBis: "31.03.2026",
        abrechnungsArt: "Endabrechnung",
        anzahlNutzungsobjekte: 1,
        widerspruchVorhanden: false,
      },
    ]);

    const button = widerspruchButton(wrapper);

    expect(button.exists()).toBe(true);
    expect(button.classes()).not.toContain("v-btn--disabled");
    expect(button.attributes("aria-label")).toBe(
      "Widerspruch zu Abrechnung 1000000001 anlegen"
    );
  });

  it("givenAbrechnungWithWiderspruch_thenDisableTheButton", () => {
    const wrapper = mountTable([
      {
        id: "123e4567-e89b-12d3-a456-426614174001",
        geschaeftspartnerId: "1000000001",
        zeitraumVon: "01.01.2026",
        zeitraumBis: "31.03.2026",
        abrechnungsArt: "Endabrechnung",
        anzahlNutzungsobjekte: 1,
        widerspruchVorhanden: true,
      },
    ]);

    expect(widerspruchButton(wrapper).classes()).toContain("v-btn--disabled");
  });

  it("givenAbrechnungWithoutWiderspruch_thenTheButtonCarriesTheAnlegenIcon", () => {
    const wrapper = mountTable([
      {
        id: "123e4567-e89b-12d3-a456-426614174001",
        geschaeftspartnerId: "1000000001",
        zeitraumVon: "01.01.2026",
        zeitraumBis: "31.03.2026",
        abrechnungsArt: "Endabrechnung",
        anzahlNutzungsobjekte: 1,
        widerspruchVorhanden: false,
      },
    ]);

    expect(widerspruchIconPath(wrapper)).toBe(mdiChatPlus);
  });

  it("givenAbrechnungWithWiderspruch_thenTheButtonCarriesTheVorhandenIcon", () => {
    const wrapper = mountTable([
      {
        id: "123e4567-e89b-12d3-a456-426614174001",
        geschaeftspartnerId: "1000000001",
        zeitraumVon: "01.01.2026",
        zeitraumBis: "31.03.2026",
        abrechnungsArt: "Endabrechnung",
        anzahlNutzungsobjekte: 1,
        widerspruchVorhanden: true,
      },
    ]);

    expect(widerspruchIconPath(wrapper)).toBe(mdiChatAlert);
  });

  it("givenAbrechnungWithWiderspruch_thenExplainWhyItIsDisabled", () => {
    const wrapper = mountTable([
      {
        id: "123e4567-e89b-12d3-a456-426614174001",
        geschaeftspartnerId: "1000000001",
        zeitraumVon: "01.01.2026",
        zeitraumBis: "31.03.2026",
        abrechnungsArt: "Endabrechnung",
        anzahlNutzungsobjekte: 1,
        widerspruchVorhanden: true,
      },
    ]);

    expect(wrapper.findComponent({ name: "VTooltip" }).props("text")).toBe(
      "Es besteht bereits ein Widerspruch."
    );
  });
});
