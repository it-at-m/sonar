import type { Projekt } from "@/types/Projekt";
import type { ProjektAdresseForm } from "@/types/ProjektAdresseForm";

import { describe, expect, it } from "vitest";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungEnum,
} from "@/api/generated/sonar-backend";
import { createProjektAdresse } from "@/util/projektAdresseForm";
import { toProjektRequestDTO } from "@/util/projektMapper";

function adresseWith(
  overrides: Partial<ProjektAdresseForm>
): ProjektAdresseForm {
  return { ...createProjektAdresse(), ...overrides };
}

function projektWith(overrides: Partial<Projekt>): Projekt {
  return {
    projektnummer: "2026-0001",
    abrechnungBeginn: "2026-01-01",
    abrechnungEnde: "2026-03-31",
    adressen: [adresseWith({ adresse: "Marienplatz", hausnummerVon: "8" })],
    ...overrides,
  };
}

/**
 * `noUncheckedIndexedAccess` makes every index access possibly undefined. Failing here keeps the
 * non-null assertion out of each single test.
 */
function itemAt<T>(items: readonly T[], index = 0): T {
  const item = items[index];
  if (item === undefined) {
    throw new Error(`Kein Element an Position ${index}.`);
  }
  return item;
}

describe("projektMapper.ts", () => {
  describe("toProjektRequestDTO", () => {
    it("givenFilledForm_thenMapDatesAsUtcCalendarDays", () => {
      const projekt = projektWith({
        projektnummer: " 2026-0001 ",
        adressen: [
          adresseWith({
            adresse: " Marienplatz ",
            hausnummerVon: "8",
            nutzung: ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A,
            unerlaubteNutzungVon: "2026-01-05",
            unerlaubteNutzungBis: "2026-01-10",
            anzahlMahnungen: 2,
            sondernutzungErlaubt: true,
          }),
        ],
      });

      const dto = toProjektRequestDTO(projekt);

      expect(dto.projektnummer).toBe("2026-0001");
      // the serializer cuts the ISO string at 10 characters, so the UTC day must not shift
      expect(dto.abrechnungBeginn.toISOString()).toBe(
        "2026-01-01T00:00:00.000Z"
      );
      expect(dto.abrechnungEnde.toISOString()).toBe("2026-03-31T00:00:00.000Z");
      expect(dto.adressen).toHaveLength(1);
      expect(itemAt(dto.adressen).adresse).toBe("Marienplatz");
      expect(itemAt(dto.adressen).hausnummerVon).toBe("8");
      expect(itemAt(dto.adressen).nutzung).toBe(
        ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A
      );
      expect(itemAt(dto.adressen).unerlaubteNutzungVon?.toISOString()).toBe(
        "2026-01-05T00:00:00.000Z"
      );
      expect(itemAt(dto.adressen).anzahlMahnungen).toBe(2);
      expect(itemAt(dto.adressen).sondernutzungErlaubt).toBe(true);
    });

    it("givenArtFlurstueck_thenLeaveOutAdresseAndHausnummern", () => {
      const projekt = projektWith({
        adressen: [
          adresseWith({
            art: ProjektAdresseRequestDTOArtEnum.FLURSTUECK,
            adresse: "Marienplatz",
            hausnummerVon: "8",
            flurstueck: "1234/5",
            gemarkung: "Sendling",
          }),
        ],
      });

      const dto = toProjektRequestDTO(projekt);

      expect(itemAt(dto.adressen).adresse).toBeUndefined();
      expect(itemAt(dto.adressen).hausnummerVon).toBeUndefined();
      expect(itemAt(dto.adressen).flurstueck).toBe("1234/5");
      expect(itemAt(dto.adressen).gemarkung).toBe("Sendling");
    });

    it("givenTageWithoutZeitraum_thenSendTheTage", () => {
      const projekt = projektWith({
        adressen: [
          adresseWith({
            adresse: "Marienplatz",
            hausnummerVon: "8",
            tageUnerlaubteNutzung: 12,
          }),
        ],
      });

      const dto = toProjektRequestDTO(projekt);

      expect(itemAt(dto.adressen).tageUnerlaubteNutzung).toBe(12);
      expect(itemAt(dto.adressen).unerlaubteNutzungVon).toBeUndefined();
    });

    it("givenZeitraum_thenOmitTheTageSoTheBackendDerivesThem", () => {
      const projekt = projektWith({
        adressen: [
          adresseWith({
            adresse: "Marienplatz",
            hausnummerVon: "8",
            unerlaubteNutzungVon: "2026-01-05",
            unerlaubteNutzungBis: "2026-01-10",
            tageUnerlaubteNutzung: 99,
          }),
        ],
      });

      const dto = toProjektRequestDTO(projekt);

      expect(itemAt(dto.adressen).tageUnerlaubteNutzung).toBeUndefined();
      expect(itemAt(dto.adressen).unerlaubteNutzungVon).toBeDefined();
    });

    it("givenEmptyOptionalFields_thenOmitThem", () => {
      const projekt = projektWith({});

      const dto = toProjektRequestDTO(projekt);

      expect(itemAt(dto.adressen).nutzung).toBeUndefined();
      expect(itemAt(dto.adressen).hausnummerBis).toBeUndefined();
      expect(itemAt(dto.adressen).unerlaubteNutzungVon).toBeUndefined();
      expect(itemAt(dto.adressen).unerlaubteNutzungBis).toBeUndefined();
    });
  });
});
