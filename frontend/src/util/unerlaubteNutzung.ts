import type { UnerlaubteNutzung } from "@/types/UnerlaubteNutzung";

const MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

export function hasZeitraum(unerlaubteNutzung: UnerlaubteNutzung): boolean {
  return Boolean(
    unerlaubteNutzung.unerlaubteNutzungVon &&
    unerlaubteNutzung.unerlaubteNutzungBis
  );
}

export function tageUnerlaubteNutzung(
  unerlaubteNutzung: UnerlaubteNutzung
): number | undefined {
  if (!hasZeitraum(unerlaubteNutzung)) {
    return unerlaubteNutzung.tageUnerlaubteNutzung ?? undefined;
  }
  const von = new Date(unerlaubteNutzung.unerlaubteNutzungVon).getTime();
  const bis = new Date(unerlaubteNutzung.unerlaubteNutzungBis).getTime();
  if (Number.isNaN(von) || Number.isNaN(bis) || bis < von) {
    return undefined;
  }
  return Math.round((bis - von) / MILLIS_PER_DAY) + 1;
}

export function hasUnerlaubteNutzung(
  unerlaubteNutzung: UnerlaubteNutzung
): boolean {
  return (
    unerlaubteNutzung.unerlaubteNutzungVon !== "" ||
    unerlaubteNutzung.unerlaubteNutzungBis !== "" ||
    unerlaubteNutzung.tageUnerlaubteNutzung !== null
  );
}
