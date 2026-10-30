import type { AbrechnungPositionForm } from "@/types/abrechnung/AbrechnungPositionForm";

import {
  countWholePeriods,
  dayAfter,
  dayBefore,
  lastDayOfPeriods,
} from "@/util/common/zeitraum";

const HUNDREDTHS_PER_UNIT = 100;

export function createAbrechnungPosition(): AbrechnungPositionForm {
  return {
    id: crypto.randomUUID(),
    beginn: "",
    ende: "",
    laenge: null,
    breite: null,
    flaeche: null,
  };
}

export function isAbrechnungPositionDirty(
  position: AbrechnungPositionForm
): boolean {
  return (
    position.beginn !== "" ||
    position.ende !== "" ||
    position.laenge !== null ||
    position.breite !== null ||
    position.flaeche !== null
  );
}

export function alignPositionenToZeitraum(
  positionen: AbrechnungPositionForm[],
  zeitraumVon: string,
  zeitraumBis: string
): void {
  positionen.forEach((position, index) => {
    if (index === 0) {
      position.beginn = zeitraumVon;
    }
    const nachfolger = positionen[index + 1];
    position.ende =
      nachfolger === undefined ? zeitraumBis : dayBefore(nachfolger.beginn);
  });
}

export function moveBeginneWithZeitraumVon(
  positionen: AbrechnungPositionForm[],
  previousZeitraumVon: string,
  zeitraumVon: string
): void {
  for (const position of positionen.slice(1)) {
    const weeks = countWholePeriods(
      previousZeitraumVon,
      dayBefore(position.beginn),
      "week"
    );
    if (weeks !== undefined && zeitraumVon) {
      position.beginn = dayAfter(lastDayOfPeriods(zeitraumVon, weeks, "week"));
    }
  }
}

export function isPossibleBeginn(
  vorgaenger: AbrechnungPositionForm,
  position: AbrechnungPositionForm,
  beginn: string
): boolean {
  const vorgaengerInWholeWeeks =
    !vorgaenger.beginn ||
    countWholePeriods(vorgaenger.beginn, dayBefore(beginn), "week") !==
      undefined;
  const positionInWholeWeeks =
    !position.ende ||
    countWholePeriods(beginn, position.ende, "week") !== undefined;
  return vorgaengerInWholeWeeks && positionInWholeWeeks;
}

export function hasLaengeUndBreite(position: AbrechnungPositionForm): boolean {
  return position.laenge !== null && position.breite !== null;
}

export function setLaenge(
  position: AbrechnungPositionForm,
  laenge: number | null
): void {
  position.laenge = laenge;
  updateFlaeche(position);
}

export function setBreite(
  position: AbrechnungPositionForm,
  breite: number | null
): void {
  position.breite = breite;
  updateFlaeche(position);
}

function updateFlaeche(position: AbrechnungPositionForm): void {
  const { breite, laenge } = position;
  if (laenge === null || breite === null) {
    return;
  }
  const laengeHundertstel = Math.round(laenge * HUNDREDTHS_PER_UNIT);
  const breiteHundertstel = Math.round(breite * HUNDREDTHS_PER_UNIT);
  position.flaeche =
    Math.round((laengeHundertstel * breiteHundertstel) / HUNDREDTHS_PER_UNIT) /
    HUNDREDTHS_PER_UNIT;
}
