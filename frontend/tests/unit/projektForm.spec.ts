import type { AdresseForm } from "@/composables/projektForm";

import { describe, expect, it } from "vitest";

import {
  createAdresse,
  requiredRule,
  tageUnerlaubteNutzung,
  useProjektForm,
} from "@/composables/projektForm";

function adresseWith(overrides: Partial<AdresseForm>): AdresseForm {
  return { ...createAdresse(), ...overrides };
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

describe("projektForm.ts", () => {
  describe("requiredRule", () => {
    it("givenEmptyString_thenReturnMessage", () => {
      expect(requiredRule("")).toBe("Pflichtfeld");
    });

    it("givenBlankString_thenReturnMessage", () => {
      expect(requiredRule("   ")).toBe("Pflichtfeld");
    });

    it("givenNull_thenReturnMessage", () => {
      expect(requiredRule(null)).toBe("Pflichtfeld");
    });

    it("givenZero_thenReturnTrue", () => {
      expect(requiredRule(0)).toBe(true);
    });
  });

  describe("tageUnerlaubteNutzung", () => {
    it("givenSameDay_thenReturnOne", () => {
      const adresse = adresseWith({
        unerlaubteNutzungVon: "2026-01-01",
        unerlaubteNutzungBis: "2026-01-01",
      });

      expect(tageUnerlaubteNutzung(adresse)).toBe(1);
    });

    it("givenPeriodAcrossDstChange_thenCountBothBoundariesInclusive", () => {
      // 29.03.2026 is the European DST switch, a naive local-time subtraction would lose an hour
      const adresse = adresseWith({
        unerlaubteNutzungVon: "2026-03-28",
        unerlaubteNutzungBis: "2026-03-31",
      });

      expect(tageUnerlaubteNutzung(adresse)).toBe(4);
    });

    it("givenIncompleteZeitraum_thenReturnUndefined", () => {
      const adresse = adresseWith({ unerlaubteNutzungVon: "2026-01-01" });

      expect(tageUnerlaubteNutzung(adresse)).toBeUndefined();
    });

    it("givenOnlyTage_thenReturnThem", () => {
      const adresse = adresseWith({ tageUnerlaubteNutzung: 12 });

      expect(tageUnerlaubteNutzung(adresse)).toBe(12);
    });

    it("givenZeitraumAndTage_thenPreferTheZeitraum", () => {
      const adresse = adresseWith({
        unerlaubteNutzungVon: "2026-01-01",
        unerlaubteNutzungBis: "2026-01-03",
        tageUnerlaubteNutzung: 99,
      });

      expect(tageUnerlaubteNutzung(adresse)).toBe(3);
    });

    it("givenInvertedZeitraum_thenReturnUndefined", () => {
      const adresse = adresseWith({
        unerlaubteNutzungVon: "2026-03-31",
        unerlaubteNutzungBis: "2026-01-01",
      });

      expect(tageUnerlaubteNutzung(adresse)).toBeUndefined();
    });
  });

  describe("abrechnungEndeRule", () => {
    it("givenEndeAfterBeginn_thenReturnTrue", () => {
      const { abrechnungBeginn, abrechnungEndeRule } = useProjektForm();
      abrechnungBeginn.value = "2026-01-01";

      expect(abrechnungEndeRule("2026-03-31")).toBe(true);
    });

    it("givenEndeBeforeBeginn_thenReturnMessage", () => {
      const { abrechnungBeginn, abrechnungEndeRule } = useProjektForm();
      abrechnungBeginn.value = "2026-03-31";

      expect(abrechnungEndeRule("2026-01-01")).toBe(
        "Das Ende darf nicht vor dem Beginn liegen."
      );
    });

    it("givenNoBeginn_thenReturnTrue", () => {
      const { abrechnungEndeRule } = useProjektForm();

      expect(abrechnungEndeRule("2026-01-01")).toBe(true);
    });
  });

  describe("unerlaubteNutzungBisRule", () => {
    it("givenBeginnWithoutEnde_thenReturnMessage", () => {
      const { unerlaubteNutzungBisRule } = useProjektForm();
      const adresse = adresseWith({ unerlaubteNutzungVon: "2026-01-01" });

      expect(unerlaubteNutzungBisRule(adresse)("")).toBe(
        "Bitte das Ende des Zeitraums angeben."
      );
    });

    it("givenEndeBeforeBeginn_thenReturnMessage", () => {
      const { unerlaubteNutzungBisRule } = useProjektForm();
      const adresse = adresseWith({ unerlaubteNutzungVon: "2026-03-31" });

      expect(unerlaubteNutzungBisRule(adresse)("2026-01-01")).toBe(
        "Das Ende darf nicht vor dem Beginn liegen."
      );
    });

    it("givenEmptyZeitraum_thenReturnTrue", () => {
      const { unerlaubteNutzungBisRule } = useProjektForm();

      expect(unerlaubteNutzungBisRule(adresseWith({}))("")).toBe(true);
    });
  });

  describe("unerlaubteNutzungVonRule", () => {
    it("givenEndeWithoutBeginn_thenReturnMessage", () => {
      const { unerlaubteNutzungVonRule } = useProjektForm();
      const adresse = adresseWith({ unerlaubteNutzungBis: "2026-03-31" });

      expect(unerlaubteNutzungVonRule(adresse)("")).toBe(
        "Bitte den Beginn des Zeitraums angeben."
      );
    });
  });

  describe("isDirty", () => {
    it("givenUntouchedForm_thenReturnFalse", () => {
      const { isDirty } = useProjektForm();

      expect(isDirty()).toBe(false);
    });

    it("givenFilledProjektnummer_thenReturnTrue", () => {
      const { isDirty, projektnummer } = useProjektForm();
      projektnummer.value = "2026-0001";

      expect(isDirty()).toBe(true);
    });

    it("givenAddedAdresse_thenReturnTrue", () => {
      const { addAdresse, isDirty } = useProjektForm();
      addAdresse();

      expect(isDirty()).toBe(true);
    });

    it("givenFilledAdresse_thenReturnTrue", () => {
      const { adressen, isDirty } = useProjektForm();
      itemAt(adressen.value).bezeichnung = "Marienplatz 8";

      expect(isDirty()).toBe(true);
    });
  });

  describe("removeAdresse", () => {
    it("givenIndex_thenRemoveOnlyThatAdresse", () => {
      const { addAdresse, adressen, removeAdresse } = useProjektForm();
      itemAt(adressen.value).bezeichnung = "erste";
      addAdresse();
      itemAt(adressen.value, 1).bezeichnung = "zweite";

      removeAdresse(0);

      expect(adressen.value).toHaveLength(1);
      expect(itemAt(adressen.value).bezeichnung).toBe("zweite");
    });
  });

  describe("toRequestDTO", () => {
    it("givenFilledForm_thenMapDatesAsUtcCalendarDays", () => {
      const form = useProjektForm();
      form.projektnummer.value = " 2026-0001 ";
      form.abrechnungBeginn.value = "2026-01-01";
      form.abrechnungEnde.value = "2026-03-31";
      form.adressen.value[0] = adresseWith({
        bezeichnung: " Marienplatz 8 ",
        baunutzung: "Gastronomie",
        unerlaubteNutzungVon: "2026-01-05",
        unerlaubteNutzungBis: "2026-01-10",
        anzahlMahnungen: 2,
        sondernutzungErlaubt: true,
      });

      const dto = form.toRequestDTO();

      expect(dto.projektnummer).toBe("2026-0001");
      // the serializer cuts the ISO string at 10 characters, so the UTC day must not shift
      expect(dto.abrechnungBeginn.toISOString()).toBe(
        "2026-01-01T00:00:00.000Z"
      );
      expect(dto.abrechnungEnde.toISOString()).toBe("2026-03-31T00:00:00.000Z");
      expect(dto.adressen).toHaveLength(1);
      expect(itemAt(dto.adressen).bezeichnung).toBe("Marienplatz 8");
      expect(itemAt(dto.adressen).baunutzung).toBe("Gastronomie");
      expect(itemAt(dto.adressen).unerlaubteNutzungVon?.toISOString()).toBe(
        "2026-01-05T00:00:00.000Z"
      );
      expect(itemAt(dto.adressen).anzahlMahnungen).toBe(2);
      expect(itemAt(dto.adressen).sondernutzungErlaubt).toBe(true);
    });

    it("givenTageWithoutZeitraum_thenSendTheTage", () => {
      const form = useProjektForm();
      form.projektnummer.value = "2026-0001";
      form.abrechnungBeginn.value = "2026-01-01";
      form.abrechnungEnde.value = "2026-03-31";
      form.adressen.value[0] = adresseWith({
        bezeichnung: "Marienplatz 8",
        tageUnerlaubteNutzung: 12,
      });

      const dto = form.toRequestDTO();

      expect(itemAt(dto.adressen).tageUnerlaubteNutzung).toBe(12);
      expect(itemAt(dto.adressen).unerlaubteNutzungVon).toBeUndefined();
    });

    it("givenZeitraum_thenOmitTheTageSoTheBackendDerivesThem", () => {
      const form = useProjektForm();
      form.projektnummer.value = "2026-0001";
      form.abrechnungBeginn.value = "2026-01-01";
      form.abrechnungEnde.value = "2026-03-31";
      form.adressen.value[0] = adresseWith({
        bezeichnung: "Marienplatz 8",
        unerlaubteNutzungVon: "2026-01-05",
        unerlaubteNutzungBis: "2026-01-10",
        tageUnerlaubteNutzung: 99,
      });

      const dto = form.toRequestDTO();

      expect(itemAt(dto.adressen).tageUnerlaubteNutzung).toBeUndefined();
      expect(itemAt(dto.adressen).unerlaubteNutzungVon).toBeDefined();
    });

    it("givenEmptyOptionalFields_thenOmitThem", () => {
      const form = useProjektForm();
      form.projektnummer.value = "2026-0001";
      form.abrechnungBeginn.value = "2026-01-01";
      form.abrechnungEnde.value = "2026-03-31";
      form.adressen.value[0] = adresseWith({ bezeichnung: "Marienplatz 8" });

      const dto = form.toRequestDTO();

      expect(itemAt(dto.adressen).baunutzung).toBeUndefined();
      expect(itemAt(dto.adressen).unerlaubteNutzungVon).toBeUndefined();
      expect(itemAt(dto.adressen).unerlaubteNutzungBis).toBeUndefined();
    });
  });
});
