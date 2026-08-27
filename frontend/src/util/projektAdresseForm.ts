import type { ProjektAdresseForm } from "@/types/ProjektAdresseForm";

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
