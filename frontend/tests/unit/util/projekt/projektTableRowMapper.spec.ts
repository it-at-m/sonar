import { describe, expect, it } from "vitest";

import { ProjektResponseDTOStadtbezirkEnum } from "@/api/generated/sonar-backend";
import { toDateString } from "@/util/formatter";
import { toProjektTableRow } from "@/util/projekt/projektTableRowMapper";

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

    it("givenStadtbezirk_thenShowItsNummerAndName", () => {
      const row = toProjektTableRow({
        stadtbezirk:
          ProjektResponseDTOStadtbezirkEnum.BEZIRK_19_THALKIRCHEN_OBERSENDLING_FORSTENRIED_FUERSTENRIED_SOLLN,
      });

      expect(row.stadtbezirk).toBe(
        "19 Thalkirchen-Obersendling-Forstenried-Fürstenried-Solln"
      );
    });

    it("givenNoStadtbezirk_thenLeaveItEmpty", () => {
      const row = toProjektTableRow({ stadtbezirk: undefined });

      expect(row.stadtbezirk).toBe("");
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
