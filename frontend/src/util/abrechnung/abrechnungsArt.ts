import type { AbrechnungResponseDTOAbrechnungsArtEnum } from "@/api/generated/sonar-backend";

import { AbrechnungRequestDTOAbrechnungsArtEnum } from "@/api/generated/sonar-backend";

export const ABRECHNUNGS_ART_OPTIONS = [
  {
    title: "Endabrechnung",
    value: AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG,
  },
  {
    title: "Zwischenabrechnung",
    value: AbrechnungRequestDTOAbrechnungsArtEnum.ZWISCHENABRECHNUNG,
  },
];

export function abrechnungsArtLabel(
  abrechnungsArt: AbrechnungResponseDTOAbrechnungsArtEnum | undefined
): string {
  return (
    ABRECHNUNGS_ART_OPTIONS.find((option) => option.value === abrechnungsArt)
      ?.title ?? ""
  );
}
