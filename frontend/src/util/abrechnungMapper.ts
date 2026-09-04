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
    // The 0 fallbacks are used to ensure the type, but the backend will reject them.
    // The values are validated so these fallbacks should never actually be used.
    laenge: position.laenge ?? 0,
    breite: position.breite ?? 0,
    flaeche: position.flaeche ?? 0,
    haelfte: position.haelfte,
    anteilAnFlaeche: position.anteilAnFlaeche ?? 0,
  };
}
