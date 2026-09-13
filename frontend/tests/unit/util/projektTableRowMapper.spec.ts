import { describe, expect, it } from "vitest";

import { toDateString } from "@/util/formatter";
import { toProjektTableRow } from "@/util/projektTableRowMapper";

describe("projektTableRowMapper.ts", () => {
  describe("toProjektTableRow", () => {
    it("givenProjekt_thenKeepIdAndProjektnummer", () => {
      const row = toProjektTableRow({
        id: "123e4567-e89b-12d3-a456-426614174000",
        projektnummer: "2026-0001",
      });

      expect(row.id).toBe("123e4567-e89b-12d3-a456-426614174000");
      expect(row.projektnummer).toBe("2026-0001");
    });

    it("givenDates_thenFormatThemForDisplay", () => {
      const row = toProjektTableRow({
        abrechnungBeginn: new Date("2026-01-01"),
        abrechnungEnde: new Date("2026-03-31"),
      });

      expect(row.abrechnungBeginn).toBe(toDateString(new Date("2026-01-01")));
      expect(row.abrechnungEnde).toBe(toDateString(new Date("2026-03-31")));
    });

    it("givenMissingDates_thenLeaveThemEmpty", () => {
      const row = toProjektTableRow({
        abrechnungBeginn: undefined,
        abrechnungEnde: undefined,
      });

      expect(row.abrechnungBeginn).toBe("");
      expect(row.abrechnungEnde).toBe("");
    });

    it("givenAdressen_thenCountThem", () => {
      const row = toProjektTableRow({
        adressen: [{ adresse: "A" }, { adresse: "B" }],
      });

      expect(row.anzahlAdressen).toBe(2);
    });

    it("givenNoAdressen_thenCountZero", () => {
      const row = toProjektTableRow({ adressen: undefined });

      expect(row.anzahlAdressen).toBe(0);
    });
  });
});
