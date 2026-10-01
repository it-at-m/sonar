import type { AbrechnungVersion } from "@/types/abrechnung/AbrechnungVersion";

import { ApiFactory } from "@/api/ApiFactory";
import { AbrechnungControllerApi } from "@/api/generated/sonar-backend";
import { toAbrechnungVersionen } from "@/util/abrechnung/abrechnungVersionMapper";

export async function fetchAbrechnungVersionen(
  projektId: string,
  abrechnungId: string
): Promise<AbrechnungVersion[]> {
  const versionen = await ApiFactory.getInstance(
    AbrechnungControllerApi
  ).getAbrechnungVersionen(projektId, abrechnungId);
  return toAbrechnungVersionen(versionen);
}

export function abrechnungVersionLabel(version: AbrechnungVersion): string {
  return version.aktuell
    ? `Version ${version.versionsnummer} (aktuell)`
    : `Version ${version.versionsnummer}`;
}

export function abrechnungVersionOptions(
  versionen: AbrechnungVersion[]
): { title: string; value: string }[] {
  return versionen.map((version) => ({
    title: abrechnungVersionLabel(version),
    value: version.id,
  }));
}
