import { ProjektAdresseRequestDTONutzungenEnum } from "@/api/generated/sonar-backend";

/**
 * The order follows the codes the source system holds, not the alphabet.
 */
export const NUTZUNG_OPTIONS = [
  { title: "Aufzüge", value: ProjektAdresseRequestDTONutzungenEnum.AUFZUEGE },
  {
    title: "Autokräne",
    value: ProjektAdresseRequestDTONutzungenEnum.AUTOKRAENE,
  },
  { title: "Bagger", value: ProjektAdresseRequestDTONutzungenEnum.BAGGER },
  { title: "Bauwagen", value: ProjektAdresseRequestDTONutzungenEnum.BAUWAGEN },
  { title: "Bauzaun", value: ProjektAdresseRequestDTONutzungenEnum.BAUZAUN },
  {
    title: "Container",
    value: ProjektAdresseRequestDTONutzungenEnum.CONTAINER,
  },
  {
    title: "Baugerüste",
    value: ProjektAdresseRequestDTONutzungenEnum.BAUGERUESTE,
  },
  {
    title: "Hauskanalanschluss",
    value: ProjektAdresseRequestDTONutzungenEnum.HAUSKANALANSCHLUSS,
  },
  { title: "Kräne", value: ProjektAdresseRequestDTONutzungenEnum.KRAENE },
  {
    title: "Materiallagerung",
    value: ProjektAdresseRequestDTONutzungenEnum.MATERIALLAGERUNG,
  },
  {
    title: "Überspannung",
    value: ProjektAdresseRequestDTONutzungenEnum.UEBERSPANNUNG,
  },
  {
    title: "Hebebühnen",
    value: ProjektAdresseRequestDTONutzungenEnum.HEBEBUEHNEN,
  },
  {
    title: "Sonstiges",
    value: ProjektAdresseRequestDTONutzungenEnum.SONSTIGES,
  },
];
