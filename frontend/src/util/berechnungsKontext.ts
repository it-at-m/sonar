import type {
  GeschaeftspartnerResponseDTO,
  ProjektResponseDTO,
} from "@/api/generated/sonar-backend";
import type { AbrechnungForm } from "@/composables/abrechnungForm";

import { ApiFactory } from "@/api/ApiFactory";
import {
  AbrechnungControllerApi,
  ProjektControllerApi,
} from "@/api/generated/sonar-backend";
import { toAbrechnungForm } from "@/util/abrechnungMapper";
import { lookupGeschaeftspartner } from "@/util/geschaeftspartnerLookup";

export interface BerechnungsKontext {
  abrechnung: AbrechnungForm;
  projekt: ProjektResponseDTO | undefined;
  geschaeftspartner: GeschaeftspartnerResponseDTO | null;
}

export async function fetchBerechnungsKontext(
  projektId: string,
  abrechnungId: string
): Promise<BerechnungsKontext> {
  const abrechnung = await ApiFactory.getInstance(
    AbrechnungControllerApi
  ).getAbrechnung(projektId, abrechnungId);
  const [projekt, geschaeftspartner] = await Promise.all([
    fetchProjekt(projektId),
    fetchGeschaeftspartner(abrechnung.geschaeftspartnerId),
  ]);
  return {
    abrechnung: toAbrechnungForm(abrechnung),
    projekt,
    geschaeftspartner,
  };
}

async function fetchProjekt(
  projektId: string
): Promise<ProjektResponseDTO | undefined> {
  try {
    return await ApiFactory.getInstance(ProjektControllerApi).getProjekt(
      projektId
    );
  } catch {
    return undefined;
  }
}

async function fetchGeschaeftspartner(
  geschaeftspartnerId: string | undefined
): Promise<GeschaeftspartnerResponseDTO | null> {
  if (!geschaeftspartnerId) {
    return null;
  }
  return (await lookupGeschaeftspartner(geschaeftspartnerId)).data;
}
