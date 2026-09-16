import { describe, expect, it } from "vitest";

import { toWiderspruchRequestDTO } from "@/util/widerspruchMapper";

describe("widerspruchMapper.ts", () => {
  describe("toWiderspruchRequestDTO", () => {
    it("givenEveryFieldFilled_thenMapAllOfThem", () => {
      const result = toWiderspruchRequestDTO({
        datumEingang: "2026-04-01",
        datumRuecknahme: "2026-04-15",
        datumVorlageRegierung: "2026-05-01",
        datumAblehnungRegierung: "2026-06-01",
        entscheidungDurchfuehrung: "Abrechnung wird durchgeführt",
        sollAbgesetzt: true,
        neueTeilabrechnungAnlegen: true,
        bemerkung: "Bemerkung",
      });

      expect(result.datumEingang).toEqual(new Date("2026-04-01"));
      expect(result.datumRuecknahme).toEqual(new Date("2026-04-15"));
      expect(result.datumVorlageRegierung).toEqual(new Date("2026-05-01"));
      expect(result.datumAblehnungRegierung).toEqual(new Date("2026-06-01"));
      expect(result.entscheidungDurchfuehrung).toBe(
        "Abrechnung wird durchgeführt"
      );
      expect(result.sollAbgesetzt).toBe(true);
      expect(result.neueTeilabrechnungAnlegen).toBe(true);
      expect(result.bemerkung).toBe("Bemerkung");
    });

    it("givenOnlyDatumEingang_thenLeaveTheOptionalFieldsOut", () => {
      const result = toWiderspruchRequestDTO({
        datumEingang: "2026-04-01",
        datumRuecknahme: "",
        datumVorlageRegierung: "",
        datumAblehnungRegierung: "",
        entscheidungDurchfuehrung: "",
        sollAbgesetzt: false,
        neueTeilabrechnungAnlegen: false,
        bemerkung: "",
      });

      expect(result.datumEingang).toEqual(new Date("2026-04-01"));
      expect(result.datumRuecknahme).toBeUndefined();
      expect(result.datumVorlageRegierung).toBeUndefined();
      expect(result.datumAblehnungRegierung).toBeUndefined();
      expect(result.entscheidungDurchfuehrung).toBeUndefined();
      expect(result.sollAbgesetzt).toBe(false);
      expect(result.neueTeilabrechnungAnlegen).toBe(false);
      expect(result.bemerkung).toBeUndefined();
    });

    it("givenBlankTexts_thenLeaveThemOut", () => {
      const result = toWiderspruchRequestDTO({
        datumEingang: "2026-04-01",
        datumRuecknahme: "",
        datumVorlageRegierung: "",
        datumAblehnungRegierung: "",
        entscheidungDurchfuehrung: "   ",
        sollAbgesetzt: false,
        neueTeilabrechnungAnlegen: false,
        bemerkung: "   ",
      });

      expect(result.entscheidungDurchfuehrung).toBeUndefined();
      expect(result.bemerkung).toBeUndefined();
    });

    it("givenPaddedTexts_thenTrimThem", () => {
      const result = toWiderspruchRequestDTO({
        datumEingang: "2026-04-01",
        datumRuecknahme: "",
        datumVorlageRegierung: "",
        datumAblehnungRegierung: "",
        entscheidungDurchfuehrung: "  wird durchgeführt  ",
        sollAbgesetzt: false,
        neueTeilabrechnungAnlegen: false,
        bemerkung: "  Bemerkung  ",
      });

      expect(result.entscheidungDurchfuehrung).toBe("wird durchgeführt");
      expect(result.bemerkung).toBe("Bemerkung");
    });

    it("givenNoDatumEingang_thenThrow", () => {
      expect(() =>
        toWiderspruchRequestDTO({
          datumEingang: "",
          datumRuecknahme: "",
          datumVorlageRegierung: "",
          datumAblehnungRegierung: "",
          entscheidungDurchfuehrung: "",
          sollAbgesetzt: false,
          neueTeilabrechnungAnlegen: false,
          bemerkung: "",
        })
      ).toThrow("Das Datum des Eingangs fehlt.");
    });
  });
});
