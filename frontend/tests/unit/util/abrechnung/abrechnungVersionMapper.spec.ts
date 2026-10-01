import { describe, expect, it } from "vitest";

import { toAbrechnungVersionen } from "@/util/abrechnung/abrechnungVersionMapper";

describe("abrechnungVersionMapper.ts", () => {
  describe("toAbrechnungVersionen", () => {
    it("givenVersionen_thenSortThemNewestFirst", () => {
      const versionen = toAbrechnungVersionen([
        { id: "id-2", versionsnummer: 2 },
        { id: "id-3", versionsnummer: 3 },
        { id: "id-1", versionsnummer: 1 },
      ]);

      expect(versionen.map((version) => version.id)).toEqual([
        "id-3",
        "id-2",
        "id-1",
      ]);
    });

    it("givenVersionen_thenMarkOnlyTheHighestAsAktuell", () => {
      const versionen = toAbrechnungVersionen([
        { id: "id-1", versionsnummer: 1 },
        { id: "id-3", versionsnummer: 3 },
        { id: "id-2", versionsnummer: 2 },
      ]);

      expect(versionen.map((version) => version.aktuell)).toEqual([
        true,
        false,
        false,
      ]);
    });

    it("givenVersionWithoutId_thenDropItBecauseItLeadsNowhere", () => {
      const versionen = toAbrechnungVersionen([
        { id: "id-1", versionsnummer: 1 },
        { versionsnummer: 2 },
      ]);

      expect(versionen.map((version) => version.id)).toEqual(["id-1"]);
    });

    it("givenVersionWithoutVersionsnummer_thenTreatItAsTheFirst", () => {
      const versionen = toAbrechnungVersionen([{ id: "id-1" }]);

      expect(versionen[0]?.versionsnummer).toBe(1);
    });

    it("givenNoVersionen_thenReturnAnEmptyList", () => {
      expect(toAbrechnungVersionen([])).toEqual([]);
    });

    it("givenSingleVersion_thenMarkItAsAktuell", () => {
      const versionen = toAbrechnungVersionen([
        { id: "id-1", versionsnummer: 1 },
      ]);

      expect(versionen[0]?.aktuell).toBe(true);
    });
  });
});
