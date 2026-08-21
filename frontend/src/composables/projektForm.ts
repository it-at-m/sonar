/**
 * State, validation rules and request mapping of the "Projekt anlegen" form.
 *
 * Kept out of the view so the rules and the mapping can be tested without rendering anything. Dates
 * are held as ISO strings (yyyy-MM-dd) exactly as the date inputs provide them, which also makes
 * them comparable lexicographically.
 */
import type { ProjektRequestDTO } from "@/api/generated/sonar-backend";

import { ref } from "vue";

export interface AdresseForm {
  /** Only exists so the list can be keyed by identity instead of by index. */
  id: number;
  bezeichnung: string;
  baunutzung: string;
  unerlaubteNutzungVon: string;
  unerlaubteNutzungBis: string;
  tageUnerlaubteNutzung: number | null;
  anzahlMahnungen: number;
  sondernutzungErlaubt: boolean;
}

type ValidationRule = (value: string) => boolean | string;

const MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

let nextAdresseId = 0;

export function createAdresse(): AdresseForm {
  return {
    id: nextAdresseId++,
    bezeichnung: "",
    baunutzung: "",
    unerlaubteNutzungVon: "",
    unerlaubteNutzungBis: "",
    tageUnerlaubteNutzung: null,
    anzahlMahnungen: 0,
    sondernutzungErlaubt: false,
  };
}

export function hasZeitraum(adresse: AdresseForm): boolean {
  return Boolean(adresse.unerlaubteNutzungVon && adresse.unerlaubteNutzungBis);
}

export function requiredRule(value: unknown): boolean | string {
  const filled =
    typeof value === "string"
      ? value.trim() !== ""
      : value !== null && value !== undefined;
  return filled || "Pflichtfeld";
}

export function tageUnerlaubteNutzung(
  adresse: AdresseForm
): number | undefined {
  if (!adresse.unerlaubteNutzungVon || !adresse.unerlaubteNutzungBis) {
    return adresse.tageUnerlaubteNutzung ?? undefined;
  }
  const von = new Date(adresse.unerlaubteNutzungVon).getTime();
  const bis = new Date(adresse.unerlaubteNutzungBis).getTime();
  if (Number.isNaN(von) || Number.isNaN(bis) || bis < von) {
    return undefined;
  }
  return Math.round((bis - von) / MILLIS_PER_DAY) + 1;
}

export function useProjektForm() {
  const projektnummer = ref("");
  const abrechnungBeginn = ref("");
  const abrechnungEnde = ref("");
  const adressen = ref<AdresseForm[]>([createAdresse()]);

  function addAdresse(): void {
    adressen.value.push(createAdresse());
  }

  function removeAdresse(index: number): void {
    adressen.value.splice(index, 1);
  }

  function abrechnungEndeRule(value: string): boolean | string {
    if (!value || !abrechnungBeginn.value) {
      return true;
    }
    return (
      value >= abrechnungBeginn.value ||
      "Das Ende darf nicht vor dem Beginn liegen."
    );
  }

  function unerlaubteNutzungVonRule(adresse: AdresseForm): ValidationRule {
    return (value: string) =>
      !!value ||
      !adresse.unerlaubteNutzungBis ||
      "Bitte den Beginn des Zeitraums angeben.";
  }

  function unerlaubteNutzungBisRule(adresse: AdresseForm): ValidationRule {
    return (value: string) => {
      if (!value) {
        return (
          !adresse.unerlaubteNutzungVon ||
          "Bitte das Ende des Zeitraums angeben."
        );
      }
      if (
        adresse.unerlaubteNutzungVon &&
        value < adresse.unerlaubteNutzungVon
      ) {
        return "Das Ende darf nicht vor dem Beginn liegen.";
      }
      return true;
    };
  }

  function isDirty(): boolean {
    if (projektnummer.value || abrechnungBeginn.value || abrechnungEnde.value) {
      return true;
    }
    if (adressen.value.length > 1) {
      return true;
    }
    return adressen.value.some(
      (adresse) =>
        adresse.bezeichnung !== "" ||
        adresse.baunutzung !== "" ||
        adresse.unerlaubteNutzungVon !== "" ||
        adresse.unerlaubteNutzungBis !== "" ||
        adresse.tageUnerlaubteNutzung !== null ||
        adresse.anzahlMahnungen !== 0 ||
        adresse.sondernutzungErlaubt
    );
  }

  function toRequestDTO(): ProjektRequestDTO {
    return {
      projektnummer: projektnummer.value.trim(),
      abrechnungBeginn: new Date(abrechnungBeginn.value),
      abrechnungEnde: new Date(abrechnungEnde.value),
      adressen: adressen.value.map((adresse) => ({
        bezeichnung: adresse.bezeichnung.trim(),
        baunutzung: adresse.baunutzung.trim() || undefined,
        unerlaubteNutzungVon: adresse.unerlaubteNutzungVon
          ? new Date(adresse.unerlaubteNutzungVon)
          : undefined,
        unerlaubteNutzungBis: adresse.unerlaubteNutzungBis
          ? new Date(adresse.unerlaubteNutzungBis)
          : undefined,
        // Sending both is rejected: with a period given the backend derives the days from it.
        tageUnerlaubteNutzung: hasZeitraum(adresse)
          ? undefined
          : (adresse.tageUnerlaubteNutzung ?? undefined),
        anzahlMahnungen: adresse.anzahlMahnungen,
        sondernutzungErlaubt: adresse.sondernutzungErlaubt,
      })),
    };
  }

  return {
    abrechnungBeginn,
    abrechnungEnde,
    abrechnungEndeRule,
    addAdresse,
    adressen,
    isDirty,
    projektnummer,
    removeAdresse,
    toRequestDTO,
    unerlaubteNutzungBisRule,
    unerlaubteNutzungVonRule,
  };
}
