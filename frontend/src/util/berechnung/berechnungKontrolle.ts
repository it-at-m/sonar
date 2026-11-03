/*
 * This whole file is temporary. It writes the data the Berechnung ran on in the shape the
 * Berechnungslog uses, so the two can be read next to each other and held against the prototype.
 * It goes away with the Berechnung dialog, and the formatters below go with it.
 */

import type {
  GeschaeftspartnerResponseDTO,
  ProjektResponseDTO,
} from "@/api/generated/sonar-backend";
import type { AbrechnungForm } from "@/types/abrechnung/AbrechnungForm";
import type { AbrechnungNutzungsobjektForm } from "@/types/abrechnung/AbrechnungNutzungsobjektForm";

import { adresseLabel, adresseTitle } from "@/util/common/adresseLabel";
import {
  hasUnerlaubteNutzung,
  tageUnerlaubteNutzung,
} from "@/util/common/unerlaubteNutzungForm";

const DECIMAL_FORMAT = new Intl.NumberFormat("de-DE", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const EURO_FORMAT = new Intl.NumberFormat("de-DE", {
  style: "currency",
  currency: "EUR",
});

// The shared formatter widened to accept a missing date, which the Berechnung DTOs allow.
export function toDateString(date: Date | undefined): string {
  return date ? date.toLocaleDateString() : "";
}

export function toDecimalString(value: number | null | undefined): string {
  return value === null || value === undefined
    ? ""
    : DECIMAL_FORMAT.format(value);
}

export function toEuroString(value: number | null | undefined): string {
  return value === null || value === undefined ? "" : EURO_FORMAT.format(value);
}

export interface Kontrolldaten {
  abrechnung: AbrechnungForm;
  projekt: ProjektResponseDTO | undefined;
  geschaeftspartner: GeschaeftspartnerResponseDTO | null;
  verwaltungsgebuehr: number | undefined;
}

const PROJEKTDATEN_UEBERSCHRIFT = [
  "P R O J E K T D A T E N",
  "==========================",
];

const FLAECHEN_UEBERSCHRIFT = ["F L Ä C H E N", "======================="];

export function kontrolldatenText(kontrolldaten: Kontrolldaten): string {
  return [
    ...PROJEKTDATEN_UEBERSCHRIFT,
    projektzeile(kontrolldaten.projekt),
    `Verwaltungsgebühr: ${toEuroString(kontrolldaten.verwaltungsgebuehr)}`,
    geschaeftspartnerzeile(kontrolldaten),
    "",
    ...FLAECHEN_UEBERSCHRIFT,
    ...flaechenzeilen(kontrolldaten.abrechnung),
  ].join("\n");
}

function isFilled(wert: string | undefined): wert is string {
  return Boolean(wert?.trim());
}

function projektzeile(projekt: ProjektResponseDTO | undefined): string {
  if (projekt === undefined) {
    return "Projekt: konnte nicht geladen werden";
  }
  return isFilled(projekt.projektnummer)
    ? `Projekt: ${projekt.projektnummer}`
    : "Projekt: ohne Nummer";
}

function geschaeftspartnerzeile(kontrolldaten: Kontrolldaten): string {
  const id = kontrolldaten.abrechnung.geschaeftspartnerId;
  if (!id) {
    return "Geschäftspartner: keiner hinterlegt";
  }
  const name = geschaeftspartnerName(kontrolldaten.geschaeftspartner);
  return name ? `Geschäftspartner: ${name} (${id})` : `Geschäftspartner: ${id}`;
}

function geschaeftspartnerName(
  geschaeftspartner: GeschaeftspartnerResponseDTO | null
): string {
  if (geschaeftspartner === null) {
    return "";
  }
  return (
    [
      geschaeftspartner.name1,
      geschaeftspartner.name2,
      geschaeftspartner.name3,
      geschaeftspartner.name4,
      [geschaeftspartner.vorname, geschaeftspartner.nachname]
        .filter(isFilled)
        .join(" "),
    ].find(isFilled) ?? ""
  );
}

function flaechenzeilen(abrechnung: AbrechnungForm): string[] {
  return abrechnung.nutzungsobjekte.flatMap((nutzungsobjekt, index) => [
    ...nutzungsobjektzeilen(nutzungsobjekt, index),
    "",
  ]);
}

function nutzungsobjektzeilen(
  nutzungsobjekt: AbrechnungNutzungsobjektForm,
  index: number
): string[] {
  const titel =
    adresseTitle(nutzungsobjekt) || adresseLabel(nutzungsobjekt, index + 1);

  const zeilen = [`${titel} (Aufschlag: ${nutzungsobjekt.aufschlag50prozent})`];
  if (hasUnerlaubteNutzung(nutzungsobjekt)) {
    zeilen.push(`  Unerlaubte Nutzung: ${unerlaubteNutzung(nutzungsobjekt)}`);
  }
  nutzungsobjekt.positionen.forEach((position, positionIndex) => {
    zeilen.push(
      `  -- Position ${positionIndex + 1} (${toDecimalString(position.flaeche)}m²)` +
        ` : Gültigkeit: ${position.beginn} - ${position.ende}`
    );
  });
  return zeilen;
}

function unerlaubteNutzung(
  nutzungsobjekt: AbrechnungNutzungsobjektForm
): string {
  const tage = tageUnerlaubteNutzung(nutzungsobjekt);
  const zeitraum = [
    nutzungsobjekt.unerlaubteNutzungVon,
    nutzungsobjekt.unerlaubteNutzungBis,
  ]
    .filter(isFilled)
    .join(" - ");
  if (tage === undefined) {
    return zeitraum;
  }
  return zeitraum ? `${zeitraum} (${tage} Tage)` : `${tage} Tage`;
}
