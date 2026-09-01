import { describe, expect, it } from "vitest";

import { toProjektSort } from "@/util/projektSortMapper";

describe("projektSortMapper.ts", () => {
  describe("toProjektSort", () => {
    it("givenNoSortItem_thenReturnEmptySort", () => {
      expect(toProjektSort([])).toEqual({});
    });

    it("givenColumnWithoutBackendSort_thenReturnEmptySort", () => {
      expect(toProjektSort([{ key: "anzahlAdressen", order: "asc" }])).toEqual(
        {}
      );
    });

    it("givenAscendingColumn_thenMapKeyAndDirection", () => {
      expect(
        toProjektSort([{ key: "abrechnungBeginn", order: "asc" }])
      ).toEqual({ sortBy: "ABRECHNUNG_BEGINN", sortDirection: "ASC" });
    });

    it("givenDescendingColumn_thenMapKeyAndDirection", () => {
      expect(toProjektSort([{ key: "abrechnungEnde", order: "desc" }])).toEqual(
        {
          sortBy: "ABRECHNUNG_ENDE",
          sortDirection: "DESC",
        }
      );
    });

    it("givenColumnWithoutOrder_thenSortDescending", () => {
      expect(toProjektSort([{ key: "projektnummer" }])).toEqual({
        sortBy: "PROJEKTNUMMER",
        sortDirection: "DESC",
      });
    });

    it("givenSeveralSortItems_thenUseOnlyTheFirst", () => {
      const sortBy = [
        { key: "projektnummer", order: "asc" as const },
        { key: "abrechnungEnde", order: "desc" as const },
      ];

      expect(toProjektSort(sortBy)).toEqual({
        sortBy: "PROJEKTNUMMER",
        sortDirection: "ASC",
      });
    });
  });
});
