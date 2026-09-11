import { describe, expect, it } from "vitest";

import { toAbrechnungTableRow } from "@/util/abrechnungTableRowMapper";
import { toDateString } from "@/util/formatter";

describe("abrechnungTableRowMapper.ts", () => {
  describe("toAbrechnungTableRow", () => {
    it("givenAbrechnung_thenKeepIdAndGeschaeftspartner", () => {
      const row = toAbrechnungTableRow({
        id: "123e4567-e89b-12d3-a456-426614174000",
        geschaeftspartnerId: "1000000001",
      });

      expect(row.id).toBe("123e4567-e89b-12d3-a456-426614174000");
      expect(row.geschaeftspartnerId).toBe("1000000001");
    });

    it("givenZeitraum_thenFormatItForDisplay", () => {
      const row = toAbrechnungTableRow({
        zeitraumVon: new Date("2026-01-01"),
        zeitraumBis: new Date("2026-03-31"),
      });

      expect(row.zeitraumVon).toBe(toDateString(new Date("2026-01-01")));
      expect(row.zeitraumBis).toBe(toDateString(new Date("2026-03-31")));
    });

    it("givenMissingZeitraum_thenLeaveItEmpty", () => {
      const row = toAbrechnungTableRow({
        zeitraumVon: undefined,
        zeitraumBis: undefined,
      });

      expect(row.zeitraumVon).toBe("");
      expect(row.zeitraumBis).toBe("");
    });

    it("givenAbrechnungsArt_thenShowItsGermanLabel", () => {
      expect(
        toAbrechnungTableRow({ abrechnungsArt: "ENDABRECHNUNG" }).abrechnungsArt
      ).toBe("Endabrechnung");
      expect(
        toAbrechnungTableRow({ abrechnungsArt: "ZWISCHENABRECHNUNG" })
          .abrechnungsArt
      ).toBe("Zwischenabrechnung");
    });

    it("givenMissingAbrechnungsArt_thenLeaveItEmpty", () => {
      const row = toAbrechnungTableRow({ abrechnungsArt: undefined });

      expect(row.abrechnungsArt).toBe("");
    });

    it("givenNutzungsobjekte_thenCountThem", () => {
      const row = toAbrechnungTableRow({
        nutzungsobjekte: [{ adresse: "A" }, { adresse: "B" }],
      });

      expect(row.anzahlNutzungsobjekte).toBe(2);
    });

    it("givenNoNutzungsobjekte_thenCountZero", () => {
      const row = toAbrechnungTableRow({ nutzungsobjekte: undefined });

      expect(row.anzahlNutzungsobjekte).toBe(0);
    });

    it("givenWiderspruchVorhanden_thenKeepTheFlag", () => {
      expect(
        toAbrechnungTableRow({ widerspruchVorhanden: true })
          .widerspruchVorhanden
      ).toBe(true);
      expect(
        toAbrechnungTableRow({ widerspruchVorhanden: false })
          .widerspruchVorhanden
      ).toBe(false);
    });

    it("givenMissingWiderspruchVorhanden_thenTreatItAsNone", () => {
      const row = toAbrechnungTableRow({ widerspruchVorhanden: undefined });

      expect(row.widerspruchVorhanden).toBe(false);
    });
  });
});
