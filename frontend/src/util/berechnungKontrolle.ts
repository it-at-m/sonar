import type {
  GeschaeftspartnerResponseDTO,
  ProjektResponseDTO,
} from "@/api/generated/sonar-backend";
import type { AbrechnungForm } from "@/composables/abrechnungForm";
import type { AbrechnungNutzungsobjektForm } from "@/types/AbrechnungNutzungsobjektForm";

import { adresseLabel } from "@/util/adresseLabel";
import { toDecimalString, toEuroString } from "@/util/formatter";
import { projektAdresseSuggestionTitle } from "@/util/projektAdresseSuggestion";
import {
  hasUnerlaubteNutzung,
  tageUnerlaubteNutzung,
} from "@/util/unerlaubteNutzung";

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

/**
 * The data the Berechnung ran on, written the way the Berechnungslog writes its own sections, so
 * the two can be read next to each other.
 */
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

/**
 * A Projekt without a Projektname is named by its Projektnummer, the way the Berechnung names it.
 * The number then stands alone instead of being repeated in the brackets.
 */
function projektzeile(projekt: ProjektResponseDTO | undefined): string {
  if (projekt === undefined) {
    return "Projekt: konnte nicht geladen werden";
  }
  const nummer = isFilled(projekt.projektnummer) ? projekt.projektnummer : "";
  if (!isFilled(projekt.projektname)) {
    return nummer ? `Projekt: ${nummer}` : "Projekt: ohne Name und Nummer";
  }
  return nummer
    ? `Projekt: ${projekt.projektname} (${nummer})`
    : `Projekt: ${projekt.projektname}`;
}

function geschaeftspartnerzeile(kontrolldaten: Kontrolldaten): string {
  const id = kontrolldaten.abrechnung.geschaeftspartnerId;
  if (!id) {
    return "Geschäftspartner: keiner hinterlegt";
  }
  const name = geschaeftspartnerName(kontrolldaten.geschaeftspartner);
  return name ? `Geschäftspartner: ${name} (${id})` : `Geschäftspartner: ${id}`;
}

/** The first of the name lines, the same one the Stammdaten of a Geschäftspartner lead with. */
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
  // The Aufschlag belongs to the Nutzungsobjekt and holds for all of its Positionen, but the form
  // keeps it on the Position. The Berechnung reads it off the first one, so this line does too.
  const aufschlag = nutzungsobjekt.positionen[0]?.haelfte ?? false;
  const titel =
    projektAdresseSuggestionTitle(nutzungsobjekt) ||
    adresseLabel(nutzungsobjekt, index + 1);

  const zeilen = [`${titel} (Aufschlag: ${aufschlag})`];
  if (hasUnerlaubteNutzung(nutzungsobjekt)) {
    zeilen.push(`  Unerlaubte Nutzung: ${unerlaubteNutzung(nutzungsobjekt)}`);
  }
  // A Position without a Bezeichnung is numbered in the order it is read, the way the Berechnung
  // names it in the Bescheiddaten. The form carries no Bezeichnung, so every Position is numbered.
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
