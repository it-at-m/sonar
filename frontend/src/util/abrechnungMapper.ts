import type {
  AbrechnungNutzungsobjektRequestDTO,
  AbrechnungPositionRequestDTO,
  AbrechnungRequestDTO,
} from "@/api/generated/sonar-backend";
import type { AbrechnungForm } from "@/composables/abrechnungForm";
import type { AbrechnungNutzungsobjektForm } from "@/types/AbrechnungNutzungsobjektForm";
import type { AbrechnungPositionForm } from "@/types/AbrechnungPositionForm";

import { toAdresseRequestFields } from "@/util/adresseMapper";
import { toUnerlaubteNutzungRequestFields } from "@/util/unerlaubteNutzungMapper";

/**
 * Turns the form into the request the backend expects.
 *
 * Only a validated form is submitted, so the required entries are present. Where a neutral value
 * exists it is used anyway, because the backend rejects it and a rejected save is the better failure
 * than a silently wrong one.
 *
 * @throws Error if the Art der Abrechnung is missing, which has no neutral value to fall back to
 */
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

function toPositionRequestDTO(
  position: AbrechnungPositionForm
): AbrechnungPositionRequestDTO {
  return {
    beginn: new Date(position.beginn),
    ende: new Date(position.ende),
    laenge: position.laenge ?? 0,
    breite: position.breite ?? 0,
    flaeche: position.flaeche ?? 0,
    haelfte: position.haelfte,
    anteilAnFlaeche: position.anteilAnFlaeche ?? 0,
  };
}
