import { mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import AbrechnungTable from "@/components/AbrechnungTable.vue";
import vuetify from "@/plugins/vuetify";

function mountTable() {
  return mount(AbrechnungTable, {
    props: {
      page: 1,
      itemsPerPage: 10,
      sortBy: [{ key: "zeitraumVon", order: "desc" as const }],
      rows: [],
      totalAbrechnungen: 0,
      loading: false,
    },
    global: { plugins: [vuetify] },
  });
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
});
