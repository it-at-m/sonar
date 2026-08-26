import type { ProjektAdresseRequestDTO } from "@/api/generated/sonar-backend";
import type { ProjektAdresseForm } from "@/types/ProjektAdresseForm";
import type { ValidationRule } from "@/util/validationRules";

import { endeNotBeforeBeginn } from "@/util/validationRules";

const MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

export function createProjektAdresse(): ProjektAdresseForm {
  return {
    id: crypto.randomUUID(),
    bezeichnung: "",
    baunutzung: "",
    unerlaubteNutzungVon: "",
    unerlaubteNutzungBis: "",
    tageUnerlaubteNutzung: null,
    anzahlMahnungen: 0,
    sondernutzungErlaubt: false,
  };
}

export function hasZeitraum(adresse: ProjektAdresseForm): boolean {
  return Boolean(adresse.unerlaubteNutzungVon && adresse.unerlaubteNutzungBis);
}

export function tageUnerlaubteNutzung(
  adresse: ProjektAdresseForm
): number | undefined {
  if (!hasZeitraum(adresse)) {
    return adresse.tageUnerlaubteNutzung ?? undefined;
  }
  const von = new Date(adresse.unerlaubteNutzungVon).getTime();
  const bis = new Date(adresse.unerlaubteNutzungBis).getTime();
  if (Number.isNaN(von) || Number.isNaN(bis) || bis < von) {
    return undefined;
  }
  return Math.round((bis - von) / MILLIS_PER_DAY) + 1;
}

export function unerlaubteNutzungVonRule(
  adresse: ProjektAdresseForm
): ValidationRule {
  return (value: string) =>
    !!value ||
    !adresse.unerlaubteNutzungBis ||
    "Bitte den Beginn des Zeitraums angeben.";
}

export function unerlaubteNutzungBisRule(
  adresse: ProjektAdresseForm
): ValidationRule {
  return (value: string) => {
    if (!value) {
      return (
        !adresse.unerlaubteNutzungVon || "Bitte das Ende des Zeitraums angeben."
      );
    }
    return endeNotBeforeBeginn(adresse.unerlaubteNutzungVon, value);
  };
}

export function isProjektAdresseDirty(adresse: ProjektAdresseForm): boolean {
  return (
    adresse.bezeichnung !== "" ||
    adresse.baunutzung !== "" ||
    adresse.unerlaubteNutzungVon !== "" ||
    adresse.unerlaubteNutzungBis !== "" ||
    adresse.tageUnerlaubteNutzung !== null ||
    adresse.anzahlMahnungen !== 0 ||
    adresse.sondernutzungErlaubt
  );
}

export function toProjektAdresseRequestDTO(
  adresse: ProjektAdresseForm
): ProjektAdresseRequestDTO {
  return {
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
  };
}
