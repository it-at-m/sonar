import type {
  AbrechnungNutzungsobjektRequestDTO,
  AbrechnungNutzungsobjektResponseDTO,
  AbrechnungPositionRequestDTO,
  AbrechnungPositionResponseDTO,
  AbrechnungRequestDTO,
  AbrechnungResponseDTO,
} from "@/api/generated/sonar-backend";
import type { AbrechnungForm } from "@/composables/abrechnungForm";
import type { AbrechnungNutzungsobjektForm } from "@/types/AbrechnungNutzungsobjektForm";
import type { AbrechnungPositionForm } from "@/types/AbrechnungPositionForm";

import { ProjektAdresseRequestDTOArtEnum } from "@/api/generated/sonar-backend";
import { createAbrechnungNutzungsobjekt } from "@/util/abrechnungNutzungsobjektForm";
import { createAbrechnungPosition } from "@/util/abrechnungPositionForm";
import { toAdresseRequestFields } from "@/util/adresseMapper";
import { toIsoDateString } from "@/util/formatter";
import { toUnerlaubteNutzungRequestFields } from "@/util/unerlaubteNutzungMapper";

export function toAbrechnungRequestDTO(
  abrechnung: AbrechnungForm
): AbrechnungRequestDTO {
  const abrechnungsArt = abrechnung.abrechnungsArt;
  if (abrechnungsArt === null) {
    throw new Error("Die Art der Abrechnung fehlt.");
  }

  const genutzt = abrechnung.zustellungsbevollmaechtigterGenutzt;
  return {
    geschaeftspartnerId: abrechnung.geschaeftspartnerId.trim(),
    zustellungsbevollmaechtigterGenutzt: genutzt,
    // Leftovers of a toggle switched back off are rejected, so they are dropped here.
    zustellungsbevollmaechtigterId: genutzt
      ? abrechnung.zustellungsbevollmaechtigterId.trim()
      : undefined,
    zustellungsbevollmaechtigterTyp: genutzt
      ? (abrechnung.zustellungsbevollmaechtigterTyp ?? undefined)
      : undefined,
    zeitraumVon: new Date(abrechnung.zeitraumVon),
    zeitraumBis: new Date(abrechnung.zeitraumBis),
    abrechnungsArt,
    nutzungsobjekte: abrechnung.nutzungsobjekte.map(toNutzungsobjektRequestDTO),
  };
}

function toNutzungsobjektRequestDTO(
  nutzungsobjekt: AbrechnungNutzungsobjektForm
): AbrechnungNutzungsobjektRequestDTO {
  return {
    ...toAdresseRequestFields(nutzungsobjekt),
    ...toUnerlaubteNutzungRequestFields(nutzungsobjekt),
    bemerkung: nutzungsobjekt.bemerkung.trim() || undefined,
    positionen: nutzungsobjekt.positionen.map(toPositionRequestDTO),
  };
}

export function toAbrechnungForm(
  abrechnung: AbrechnungResponseDTO
): AbrechnungForm {
  const nutzungsobjekte = (abrechnung.nutzungsobjekte ?? []).map(
    toNutzungsobjektForm
  );
  return {
    geschaeftspartnerId: abrechnung.geschaeftspartnerId ?? "",
    zustellungsbevollmaechtigterGenutzt:
      abrechnung.zustellungsbevollmaechtigterGenutzt ?? false,
    zustellungsbevollmaechtigterId:
      abrechnung.zustellungsbevollmaechtigterId ?? "",
    zustellungsbevollmaechtigterTyp:
      abrechnung.zustellungsbevollmaechtigterTyp ?? null,
    zeitraumVon: toIsoDateString(abrechnung.zeitraumVon),
    zeitraumBis: toIsoDateString(abrechnung.zeitraumBis),
    abrechnungsArt: abrechnung.abrechnungsArt ?? null,
    nutzungsobjekte:
      nutzungsobjekte.length > 0
        ? nutzungsobjekte
        : [createAbrechnungNutzungsobjekt()],
  };
}

function toNutzungsobjektForm(
  nutzungsobjekt: AbrechnungNutzungsobjektResponseDTO
): AbrechnungNutzungsobjektForm {
  const positionen = (nutzungsobjekt.positionen ?? []).map(toPositionForm);
  return {
    id: crypto.randomUUID(),
    art: nutzungsobjekt.art ?? ProjektAdresseRequestDTOArtEnum.ADRESSE,
    adresse: nutzungsobjekt.adresse ?? "",
    hausnummerVon: nutzungsobjekt.hausnummerVon ?? "",
    hausnummerBis: nutzungsobjekt.hausnummerBis ?? "",
    flurstueck: nutzungsobjekt.flurstueck ?? "",
    gemarkung: nutzungsobjekt.gemarkung ?? "",
    nutzung: nutzungsobjekt.nutzung ?? null,
    unerlaubteNutzungVon: toIsoDateString(nutzungsobjekt.unerlaubteNutzungVon),
    unerlaubteNutzungBis: toIsoDateString(nutzungsobjekt.unerlaubteNutzungBis),
    tageUnerlaubteNutzung: nutzungsobjekt.tageUnerlaubteNutzung ?? null,
    bemerkung: nutzungsobjekt.bemerkung ?? "",
    positionen:
      positionen.length > 0 ? positionen : [createAbrechnungPosition()],
  };
}

function toPositionForm(
  position: AbrechnungPositionResponseDTO
): AbrechnungPositionForm {
  return {
    id: crypto.randomUUID(),
    beginn: toIsoDateString(position.beginn),
    ende: toIsoDateString(position.ende),
    laenge: position.laenge ?? null,
    breite: position.breite ?? null,
    flaeche: position.flaeche ?? null,
    haelfte: position.haelfte ?? false,
    anteilAnFlaeche: position.anteilAnFlaeche ?? null,
  };
}

function toPositionRequestDTO(
  position: AbrechnungPositionForm
): AbrechnungPositionRequestDTO {
  return {
    beginn: new Date(position.beginn),
    ende: new Date(position.ende),
    // The 0 fallbacks are used to ensure the type, but the backend will reject them.
    // The values are validated so these fallbacks should never actually be used.
    laenge: position.laenge ?? 0,
    breite: position.breite ?? 0,
    flaeche: position.flaeche ?? 0,
    haelfte: position.haelfte,
    anteilAnFlaeche: position.anteilAnFlaeche ?? 0,
  };
}
