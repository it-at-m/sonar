import { describe, expect, it } from "vitest";

import {
  AbrechnungRequestDTOAbrechnungsArtEnum,
  AbrechnungRequestDTOZustellungsbevollmaechtigterTypEnum,
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungEnum,
} from "@/api/generated/sonar-backend";
import { useAbrechnungForm } from "@/composables/abrechnungForm";
import { toAbrechnungRequestDTO } from "@/util/abrechnungMapper";

function itemAt<T>(items: readonly T[], index = 0): T {
  const item = items[index];
  if (item === undefined) {
    throw new Error(`Kein Element an Position ${index}.`);
  }
  return item;
}

describe("abrechnungMapper.ts", () => {
  describe("toAbrechnungRequestDTO", () => {
    it("givenFilledForm_thenTrimTheTextAndKeepTheValues", () => {
      const { abrechnung } = useAbrechnungForm();
      abrechnung.value.geschaeftspartnerId = " 1000000001 ";
      abrechnung.value.zeitraumVon = "2026-01-01";
      abrechnung.value.zeitraumBis = "2026-03-31";
      abrechnung.value.abrechnungsArt =
        AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG;

      const nutzungsobjekt = itemAt(abrechnung.value.nutzungsobjekte);
      nutzungsobjekt.adresse = " Marienplatz ";
      nutzungsobjekt.hausnummerVon = "8";
      nutzungsobjekt.nutzung = ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A;

      const position = itemAt(nutzungsobjekt.positionen);
      position.beginn = "2026-01-01";
      position.ende = "2026-03-31";
      position.laenge = 12;
      position.breite = 3;
      position.flaeche = 36;
      position.anteilAnFlaeche = 30;

      const requestDTO = toAbrechnungRequestDTO(abrechnung.value);

      expect(requestDTO.geschaeftspartnerId).toBe("1000000001");
      expect(requestDTO.abrechnungsArt).toBe(
        AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG
      );
      expect(requestDTO.zeitraumVon).toEqual(new Date("2026-01-01"));
      expect(itemAt(requestDTO.nutzungsobjekte).adresse).toBe("Marienplatz");
      expect(itemAt(requestDTO.nutzungsobjekte).nutzung).toBe(
        ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A
      );
    });

    it("givenPosition_thenSendEveryMeasurement", () => {
      const { abrechnung } = useAbrechnungForm();
      abrechnung.value.geschaeftspartnerId = " 1000000001 ";
      abrechnung.value.zeitraumVon = "2026-01-01";
      abrechnung.value.zeitraumBis = "2026-03-31";
      abrechnung.value.abrechnungsArt =
        AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG;

      const nutzungsobjekt = itemAt(abrechnung.value.nutzungsobjekte);
      nutzungsobjekt.adresse = " Marienplatz ";
      nutzungsobjekt.hausnummerVon = "8";
      nutzungsobjekt.nutzung = ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A;

      const position = itemAt(nutzungsobjekt.positionen);
      position.beginn = "2026-01-01";
      position.ende = "2026-03-31";
      position.laenge = 12;
      position.breite = 3;
      position.flaeche = 36;
      position.anteilAnFlaeche = 30;

      const positionRequestDTO = itemAt(
        itemAt(toAbrechnungRequestDTO(abrechnung.value).nutzungsobjekte)
          .positionen
      );

      expect(positionRequestDTO.laenge).toBe(12);
      expect(positionRequestDTO.breite).toBe(3);
      expect(positionRequestDTO.flaeche).toBe(36);
      expect(positionRequestDTO.anteilAnFlaeche).toBe(30);
    });

    it("givenArtAdresse_thenLeaveOutFlurstueckAndGemarkung", () => {
      const { abrechnung } = useAbrechnungForm();
      abrechnung.value.geschaeftspartnerId = " 1000000001 ";
      abrechnung.value.zeitraumVon = "2026-01-01";
      abrechnung.value.zeitraumBis = "2026-03-31";
      abrechnung.value.abrechnungsArt =
        AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG;

      const nutzungsobjekt = itemAt(abrechnung.value.nutzungsobjekte);
      nutzungsobjekt.adresse = " Marienplatz ";
      nutzungsobjekt.hausnummerVon = "8";
      nutzungsobjekt.nutzung = ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A;
      nutzungsobjekt.flurstueck = "1234/5";
      nutzungsobjekt.gemarkung = "Sendling";

      const position = itemAt(nutzungsobjekt.positionen);
      position.beginn = "2026-01-01";
      position.ende = "2026-03-31";
      position.laenge = 12;
      position.breite = 3;
      position.flaeche = 36;
      position.anteilAnFlaeche = 30;

      const nutzungsobjektRequestDTO = itemAt(
        toAbrechnungRequestDTO(abrechnung.value).nutzungsobjekte
      );

      expect(nutzungsobjektRequestDTO.flurstueck).toBeUndefined();
      expect(nutzungsobjektRequestDTO.gemarkung).toBeUndefined();
      expect(nutzungsobjektRequestDTO.adresse).toBe("Marienplatz");
    });

    it("givenArtFlurstueck_thenLeaveOutAdresseAndHausnummern", () => {
      const { abrechnung } = useAbrechnungForm();
      abrechnung.value.geschaeftspartnerId = " 1000000001 ";
      abrechnung.value.zeitraumVon = "2026-01-01";
      abrechnung.value.zeitraumBis = "2026-03-31";
      abrechnung.value.abrechnungsArt =
        AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG;

      const nutzungsobjekt = itemAt(abrechnung.value.nutzungsobjekte);
      nutzungsobjekt.art = ProjektAdresseRequestDTOArtEnum.FLURSTUECK;
      nutzungsobjekt.adresse = " Marienplatz ";
      nutzungsobjekt.hausnummerVon = "8";
      nutzungsobjekt.nutzung = ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A;
      nutzungsobjekt.flurstueck = "1234/5";
      nutzungsobjekt.gemarkung = "Sendling";

      const position = itemAt(nutzungsobjekt.positionen);
      position.beginn = "2026-01-01";
      position.ende = "2026-03-31";
      position.laenge = 12;
      position.breite = 3;
      position.flaeche = 36;
      position.anteilAnFlaeche = 30;

      const nutzungsobjektRequestDTO = itemAt(
        toAbrechnungRequestDTO(abrechnung.value).nutzungsobjekte
      );

      expect(nutzungsobjektRequestDTO.adresse).toBeUndefined();
      expect(nutzungsobjektRequestDTO.hausnummerVon).toBeUndefined();
      expect(nutzungsobjektRequestDTO.hausnummerBis).toBeUndefined();
      expect(nutzungsobjektRequestDTO.flurstueck).toBe("1234/5");
    });

    it("givenEmptyHausnummerBis_thenLeaveItOut", () => {
      const { abrechnung } = useAbrechnungForm();
      abrechnung.value.geschaeftspartnerId = " 1000000001 ";
      abrechnung.value.zeitraumVon = "2026-01-01";
      abrechnung.value.zeitraumBis = "2026-03-31";
      abrechnung.value.abrechnungsArt =
        AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG;

      const nutzungsobjekt = itemAt(abrechnung.value.nutzungsobjekte);
      nutzungsobjekt.adresse = " Marienplatz ";
      nutzungsobjekt.hausnummerVon = "8";
      nutzungsobjekt.nutzung = ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A;

      const position = itemAt(nutzungsobjekt.positionen);
      position.beginn = "2026-01-01";
      position.ende = "2026-03-31";
      position.laenge = 12;
      position.breite = 3;
      position.flaeche = 36;
      position.anteilAnFlaeche = 30;

      const nutzungsobjektRequestDTO = itemAt(
        toAbrechnungRequestDTO(abrechnung.value).nutzungsobjekte
      );

      expect(nutzungsobjektRequestDTO.hausnummerBis).toBeUndefined();
    });

    it("givenZeitraumDerUnerlaubtenNutzung_thenLeaveOutTheTage", () => {
      const { abrechnung } = useAbrechnungForm();
      abrechnung.value.geschaeftspartnerId = " 1000000001 ";
      abrechnung.value.zeitraumVon = "2026-01-01";
      abrechnung.value.zeitraumBis = "2026-03-31";
      abrechnung.value.abrechnungsArt =
        AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG;

      const nutzungsobjekt = itemAt(abrechnung.value.nutzungsobjekte);
      nutzungsobjekt.adresse = " Marienplatz ";
      nutzungsobjekt.hausnummerVon = "8";
      nutzungsobjekt.nutzung = ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A;
      nutzungsobjekt.unerlaubteNutzungVon = "2026-01-01";
      nutzungsobjekt.unerlaubteNutzungBis = "2026-01-31";
      nutzungsobjekt.tageUnerlaubteNutzung = 31;

      const position = itemAt(nutzungsobjekt.positionen);
      position.beginn = "2026-01-01";
      position.ende = "2026-03-31";
      position.laenge = 12;
      position.breite = 3;
      position.flaeche = 36;
      position.anteilAnFlaeche = 30;

      const nutzungsobjektRequestDTO = itemAt(
        toAbrechnungRequestDTO(abrechnung.value).nutzungsobjekte
      );

      expect(nutzungsobjektRequestDTO.unerlaubteNutzungVon).toEqual(
        new Date("2026-01-01")
      );
      expect(nutzungsobjektRequestDTO.tageUnerlaubteNutzung).toBeUndefined();
    });

    it("givenOnlyTageUnerlaubteNutzung_thenSendThem", () => {
      const { abrechnung } = useAbrechnungForm();
      abrechnung.value.geschaeftspartnerId = " 1000000001 ";
      abrechnung.value.zeitraumVon = "2026-01-01";
      abrechnung.value.zeitraumBis = "2026-03-31";
      abrechnung.value.abrechnungsArt =
        AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG;

      const nutzungsobjekt = itemAt(abrechnung.value.nutzungsobjekte);
      nutzungsobjekt.adresse = " Marienplatz ";
      nutzungsobjekt.hausnummerVon = "8";
      nutzungsobjekt.nutzung = ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A;
      nutzungsobjekt.tageUnerlaubteNutzung = 12;

      const position = itemAt(nutzungsobjekt.positionen);
      position.beginn = "2026-01-01";
      position.ende = "2026-03-31";
      position.laenge = 12;
      position.breite = 3;
      position.flaeche = 36;
      position.anteilAnFlaeche = 30;

      const nutzungsobjektRequestDTO = itemAt(
        toAbrechnungRequestDTO(abrechnung.value).nutzungsobjekte
      );

      expect(nutzungsobjektRequestDTO.tageUnerlaubteNutzung).toBe(12);
      expect(nutzungsobjektRequestDTO.unerlaubteNutzungVon).toBeUndefined();
    });

    it("givenNoZustellungsbevollmaechtigter_thenLeaveOutIdAndTyp", () => {
      const { abrechnung } = useAbrechnungForm();
      abrechnung.value.geschaeftspartnerId = " 1000000001 ";
      abrechnung.value.zustellungsbevollmaechtigterId = "2000000002";
      abrechnung.value.zustellungsbevollmaechtigterTyp =
        AbrechnungRequestDTOZustellungsbevollmaechtigterTypEnum.VORMUND;
      abrechnung.value.zeitraumVon = "2026-01-01";
      abrechnung.value.zeitraumBis = "2026-03-31";
      abrechnung.value.abrechnungsArt =
        AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG;

      const nutzungsobjekt = itemAt(abrechnung.value.nutzungsobjekte);
      nutzungsobjekt.adresse = " Marienplatz ";
      nutzungsobjekt.hausnummerVon = "8";
      nutzungsobjekt.nutzung = ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A;

      const position = itemAt(nutzungsobjekt.positionen);
      position.beginn = "2026-01-01";
      position.ende = "2026-03-31";
      position.laenge = 12;
      position.breite = 3;
      position.flaeche = 36;
      position.anteilAnFlaeche = 30;

      const requestDTO = toAbrechnungRequestDTO(abrechnung.value);

      expect(requestDTO.zustellungsbevollmaechtigterGenutzt).toBe(false);
      expect(requestDTO.zustellungsbevollmaechtigterId).toBeUndefined();
      expect(requestDTO.zustellungsbevollmaechtigterTyp).toBeUndefined();
    });

    it("givenZustellungsbevollmaechtigter_thenSendIdAndTyp", () => {
      const { abrechnung } = useAbrechnungForm();
      abrechnung.value.geschaeftspartnerId = " 1000000001 ";
      abrechnung.value.zustellungsbevollmaechtigterGenutzt = true;
      abrechnung.value.zustellungsbevollmaechtigterId = " 2000000002 ";
      abrechnung.value.zustellungsbevollmaechtigterTyp =
        AbrechnungRequestDTOZustellungsbevollmaechtigterTypEnum.VORMUND;
      abrechnung.value.zeitraumVon = "2026-01-01";
      abrechnung.value.zeitraumBis = "2026-03-31";
      abrechnung.value.abrechnungsArt =
        AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG;

      const nutzungsobjekt = itemAt(abrechnung.value.nutzungsobjekte);
      nutzungsobjekt.adresse = " Marienplatz ";
      nutzungsobjekt.hausnummerVon = "8";
      nutzungsobjekt.nutzung = ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A;

      const position = itemAt(nutzungsobjekt.positionen);
      position.beginn = "2026-01-01";
      position.ende = "2026-03-31";
      position.laenge = 12;
      position.breite = 3;
      position.flaeche = 36;
      position.anteilAnFlaeche = 30;

      const requestDTO = toAbrechnungRequestDTO(abrechnung.value);

      expect(requestDTO.zustellungsbevollmaechtigterId).toBe("2000000002");
      expect(requestDTO.zustellungsbevollmaechtigterTyp).toBe(
        AbrechnungRequestDTOZustellungsbevollmaechtigterTypEnum.VORMUND
      );
    });

    it("givenMissingAbrechnungsArt_thenThrow", () => {
      const { abrechnung } = useAbrechnungForm();
      abrechnung.value.geschaeftspartnerId = " 1000000001 ";
      abrechnung.value.zeitraumVon = "2026-01-01";
      abrechnung.value.zeitraumBis = "2026-03-31";
      abrechnung.value.abrechnungsArt = null;

      const nutzungsobjekt = itemAt(abrechnung.value.nutzungsobjekte);
      nutzungsobjekt.adresse = " Marienplatz ";
      nutzungsobjekt.hausnummerVon = "8";
      nutzungsobjekt.nutzung = ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A;

      const position = itemAt(nutzungsobjekt.positionen);
      position.beginn = "2026-01-01";
      position.ende = "2026-03-31";
      position.laenge = 12;
      position.breite = 3;
      position.flaeche = 36;
      position.anteilAnFlaeche = 30;

      expect(() => toAbrechnungRequestDTO(abrechnung.value)).toThrow();
    });
  });
});
