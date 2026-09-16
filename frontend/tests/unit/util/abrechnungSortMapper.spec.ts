import { describe, expect, it } from "vitest";

import { toAbrechnungSort } from "@/util/abrechnungSortMapper";

describe("abrechnungSortMapper.ts", () => {
  describe("toAbrechnungSort", () => {
    it("givenNoSortItem_thenReturnEmptySort", () => {
      expect(toAbrechnungSort([])).toEqual({});
    });

    it("givenColumnWithoutBackendSort_thenReturnEmptySort", () => {
      expect(
        toAbrechnungSort([{ key: "anzahlNutzungsobjekte", order: "asc" }])
      ).toEqual({});
    });

    it("givenAscendingColumn_thenMapKeyAndDirection", () => {
      expect(toAbrechnungSort([{ key: "zeitraumVon", order: "asc" }])).toEqual({
        sortBy: ["ZEITRAUM_VON"],
        sortDirection: ["ASC"],
      });
    });

    it("givenDescendingColumn_thenMapKeyAndDirection", () => {
      expect(toAbrechnungSort([{ key: "zeitraumBis", order: "desc" }])).toEqual(
        {
          sortBy: ["ZEITRAUM_BIS"],
          sortDirection: ["DESC"],
        }
      );
    });

    it("givenColumnWithoutOrder_thenSortDescending", () => {
      expect(toAbrechnungSort([{ key: "abrechnungsArt" }])).toEqual({
        sortBy: ["ABRECHNUNGS_ART"],
        sortDirection: ["DESC"],
      });
    });

    it("givenSeveralSortItems_thenKeepTheirOrder", () => {
      const sortItems = [
        { key: "geschaeftspartnerId", order: "asc" as const },
        { key: "zeitraumBis", order: "desc" as const },
      ];

      expect(toAbrechnungSort(sortItems)).toEqual({
        sortBy: ["GESCHAEFTSPARTNER_ID", "ZEITRAUM_BIS"],
        sortDirection: ["ASC", "DESC"],
      });
    });

    it("givenColumnWithoutBackendSortBetweenOthers_thenSkipItAndKeepThePairs", () => {
      const sortItems = [
        { key: "abrechnungsArt", order: "asc" as const },
        { key: "anzahlNutzungsobjekte", order: "asc" as const },
        { key: "zeitraumVon", order: "desc" as const },
      ];

      expect(toAbrechnungSort(sortItems)).toEqual({
        sortBy: ["ABRECHNUNGS_ART", "ZEITRAUM_VON"],
        sortDirection: ["ASC", "DESC"],
      });
    });
  });
});
