import type { WiderspruchRequestDTO } from "@/api/generated/sonar-backend";
import type { WiderspruchForm } from "@/composables/widerspruchForm";

export function toWiderspruchRequestDTO(
  widerspruch: WiderspruchForm
): WiderspruchRequestDTO {
  if (!widerspruch.datumEingang) {
    throw new Error("Das Datum des Eingangs fehlt.");
  }

  return {
    datumEingang: new Date(widerspruch.datumEingang),
    datumRuecknahme: toOptionalDate(widerspruch.datumRuecknahme),
    datumVorlageRegierung: toOptionalDate(widerspruch.datumVorlageRegierung),
    datumAblehnungRegierung: toOptionalDate(
      widerspruch.datumAblehnungRegierung
    ),
    // An empty string would fail the minimum length of the column, so it is dropped here.
    entscheidungDurchfuehrung:
      widerspruch.entscheidungDurchfuehrung.trim() || undefined,
    sollAbgesetzt: widerspruch.sollAbgesetzt,
    neueTeilabrechnungAnlegen: widerspruch.neueTeilabrechnungAnlegen,
    bemerkung: widerspruch.bemerkung.trim() || undefined,
  };
}

function toOptionalDate(date: string): Date | undefined {
  return date ? new Date(date) : undefined;
}
