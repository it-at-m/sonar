import { ProjektAdresseRequestDTONutzungEnum } from "@/api/generated/sonar-backend";

/**
 * Offered for an Adresse and a Flurstück alike. The titles are placeholders, like the constants
 * behind them.
 */
export const NUTZUNG_OPTIONS = [
  { title: "Nutzung A", value: ProjektAdresseRequestDTONutzungEnum.NUTZUNG_A },
  { title: "Nutzung B", value: ProjektAdresseRequestDTONutzungEnum.NUTZUNG_B },
  { title: "Nutzung C", value: ProjektAdresseRequestDTONutzungEnum.NUTZUNG_C },
];
