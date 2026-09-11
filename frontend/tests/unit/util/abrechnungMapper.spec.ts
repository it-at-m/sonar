import { describe, expect, it } from "vitest";

import {
  AbrechnungRequestDTOAbrechnungsArtEnum,
  AbrechnungRequestDTOZustellungsbevollmaechtigterTypEnum,
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungEnum,
} from "@/api/generated/sonar-backend";
import { useAbrechnungForm } from "@/composables/abrechnungForm";
import {
  toAbrechnungForm,
  toAbrechnungRequestDTO,
} from "@/util/abrechnungMapper";

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

  describe("toAbrechnungForm", () => {
    it("givenAbrechnung_thenFillTheFormWithItsData", () => {
      const form = toAbrechnungForm({
        id: "123e4567-e89b-12d3-a456-426614174001",
        versionsnummer: 1,
        geschaeftspartnerId: "1000000001",
        zustellungsbevollmaechtigterGenutzt: true,
        zustellungsbevollmaechtigterId: "2000000002",
        zustellungsbevollmaechtigterTyp: "VORMUND",
        zeitraumVon: new Date("2026-01-01"),
        zeitraumBis: new Date("2026-03-31"),
        abrechnungsArt: "ZWISCHENABRECHNUNG",
        nutzungsobjekte: [
          {
            id: "123e4567-e89b-12d3-a456-426614174002",
            art: "ADRESSE",
            adresse: "Marienplatz",
            hausnummerVon: "8",
            hausnummerBis: "12",
            nutzung: "NUTZUNG_A",
            unerlaubteNutzungVon: new Date("2026-01-01"),
            unerlaubteNutzungBis: new Date("2026-01-10"),
            tageUnerlaubteNutzung: 10,
            bemerkung: "Erste Fassung",
            positionen: [
              {
                id: "123e4567-e89b-12d3-a456-426614174003",
                beginn: new Date("2026-01-01"),
                ende: new Date("2026-03-31"),
                laenge: 12,
                breite: 3,
                flaeche: 36,
                haelfte: true,
                anteilAnFlaeche: 30,
              },
            ],
          },
        ],
      });

      expect(form.geschaeftspartnerId).toBe("1000000001");
      expect(form.zustellungsbevollmaechtigterGenutzt).toBe(true);
      expect(form.zustellungsbevollmaechtigterId).toBe("2000000002");
      expect(form.zustellungsbevollmaechtigterTyp).toBe(
        AbrechnungRequestDTOZustellungsbevollmaechtigterTypEnum.VORMUND
      );
      expect(form.zeitraumVon).toBe("2026-01-01");
      expect(form.zeitraumBis).toBe("2026-03-31");
      expect(form.abrechnungsArt).toBe(
        AbrechnungRequestDTOAbrechnungsArtEnum.ZWISCHENABRECHNUNG
      );

      const nutzungsobjekt = itemAt(form.nutzungsobjekte);
      expect(nutzungsobjekt.art).toBe(ProjektAdresseRequestDTOArtEnum.ADRESSE);
      expect(nutzungsobjekt.adresse).toBe("Marienplatz");
      expect(nutzungsobjekt.hausnummerBis).toBe("12");
      expect(nutzungsobjekt.nutzung).toBe(
        ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A
      );
      expect(nutzungsobjekt.unerlaubteNutzungVon).toBe("2026-01-01");
      expect(nutzungsobjekt.tageUnerlaubteNutzung).toBe(10);
      expect(nutzungsobjekt.bemerkung).toBe("Erste Fassung");

      const position = itemAt(nutzungsobjekt.positionen);
      expect(position.beginn).toBe("2026-01-01");
      expect(position.ende).toBe("2026-03-31");
      expect(position.laenge).toBe(12);
      expect(position.haelfte).toBe(true);
      expect(position.anteilAnFlaeche).toBe(30);
    });

    it("givenFormOfAnAbrechnung_thenSendTheSameValuesBack", () => {
      const form = toAbrechnungForm({
        geschaeftspartnerId: "1000000001",
        zeitraumVon: new Date("2026-01-01"),
        zeitraumBis: new Date("2026-03-31"),
        abrechnungsArt: "ENDABRECHNUNG",
        nutzungsobjekte: [
          {
            art: "FLURSTUECK",
            flurstueck: "1234/5",
            gemarkung: "Sendling",
            positionen: [
              {
                beginn: new Date("2026-01-01"),
                ende: new Date("2026-03-31"),
                laenge: 12,
                breite: 3,
                flaeche: 36,
                haelfte: false,
                anteilAnFlaeche: 36,
              },
            ],
          },
        ],
      });

      const requestDTO = toAbrechnungRequestDTO(form);

      expect(requestDTO.geschaeftspartnerId).toBe("1000000001");
      expect(requestDTO.zeitraumVon).toEqual(new Date("2026-01-01"));
      expect(requestDTO.zeitraumBis).toEqual(new Date("2026-03-31"));
      expect(itemAt(requestDTO.nutzungsobjekte).flurstueck).toBe("1234/5");
      expect(itemAt(requestDTO.nutzungsobjekte).gemarkung).toBe("Sendling");
      expect(
        itemAt(itemAt(requestDTO.nutzungsobjekte).positionen).flaeche
      ).toBe(36);
    });

    it("givenAbrechnungWithoutAnyDetails_thenStartWithAnEmptyNutzungsobjekt", () => {
      const form = toAbrechnungForm({});

      expect(form.geschaeftspartnerId).toBe("");
      expect(form.zeitraumVon).toBe("");
      expect(form.abrechnungsArt).toBeNull();
      expect(form.nutzungsobjekte).toHaveLength(1);
      expect(itemAt(form.nutzungsobjekte).adresse).toBe("");
      expect(itemAt(form.nutzungsobjekte).positionen).toHaveLength(1);
    });
  });
});
