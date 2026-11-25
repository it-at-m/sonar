/*
 * This whole file is temporary. So the Berechnung can be compared against the prototype.
 * It goes away with the Berechnung dialog.
 */

import type {
  GeschaeftspartnerResponseDTO,
  ProjektResponseDTO,
} from "@/api/generated/sonar-backend";
import type { AbrechnungForm } from "@/types/abrechnung/AbrechnungForm";

import { toRaw } from "vue";

import { ApiFactory } from "@/api/ApiFactory";
import {
  AbrechnungControllerApi,
  ProjektControllerApi,
} from "@/api/generated/sonar-backend";
import { toAbrechnungForm } from "@/util/abrechnung/abrechnungMapper";
import { lookupGeschaeftspartner } from "@/util/geschaeftspartner/geschaeftspartnerLookup";

export interface BerechnungsKontext {
  abrechnung: AbrechnungForm;
  projekt: ProjektResponseDTO | undefined;
  geschaeftspartner: GeschaeftspartnerResponseDTO | null;
}

export async function fetchBerechnungsKontext(
  projektId: string,
  abrechnungId: string
): Promise<BerechnungsKontext> {
  const abrechnungRequest = ApiFactory.getInstance(
    AbrechnungControllerApi
  ).getAbrechnung(projektId, abrechnungId);
  const projektRequest = ApiFactory.getInstance(ProjektControllerApi)
    .getProjekt(projektId)
    .catch(() => undefined);

  const abrechnung = await abrechnungRequest;
  const geschaeftspartnerRequest = fetchGeschaeftspartner(
    abrechnung.geschaeftspartnerId
  );

  return {
    abrechnung: toAbrechnungForm(abrechnung),
    projekt: await projektRequest,
    geschaeftspartner: await geschaeftspartnerRequest,
  };
}

export async function fetchBerechnungsKontextOfForm(
  projektId: string,
  abrechnung: AbrechnungForm
): Promise<BerechnungsKontext> {
  // The Kontrolldaten show the form as it was calculated, while the user goes on editing it.
  const calculatedAbrechnung = structuredClone(toRaw(abrechnung));
  const projektRequest = ApiFactory.getInstance(ProjektControllerApi)
    .getProjekt(projektId)
    .catch(() => undefined);
  const geschaeftspartnerRequest = fetchGeschaeftspartner(
    calculatedAbrechnung.geschaeftspartnerId.trim()
  );

  return {
    abrechnung: calculatedAbrechnung,
    projekt: await projektRequest,
    geschaeftspartner: await geschaeftspartnerRequest,
  };
}

async function fetchGeschaeftspartner(
  geschaeftspartnerId: string | undefined
): Promise<GeschaeftspartnerResponseDTO | null> {
  if (!geschaeftspartnerId) {
    return null;
  }
  return (await lookupGeschaeftspartner(geschaeftspartnerId)).data;
}
