import type { ProjektResponseDTOStadtbezirkEnum } from "@/api/generated/sonar-backend";

import { ProjektRequestDTOStadtbezirkEnum } from "@/api/generated/sonar-backend";

export const STADTBEZIRK_OPTIONS = [
  {
    title: "01 Altstadt-Lehel",
    value: ProjektRequestDTOStadtbezirkEnum.ALTSTADT_LEHEL,
  },
  {
    title: "02 Ludwigsvorstadt-Isarvorstadt",
    value: ProjektRequestDTOStadtbezirkEnum.LUDWIGSVORSTADT_ISARVORSTADT,
  },
  {
    title: "03 Maxvorstadt",
    value: ProjektRequestDTOStadtbezirkEnum.MAXVORSTADT,
  },
  {
    title: "04 Schwabing-West",
    value: ProjektRequestDTOStadtbezirkEnum.SCHWABING_WEST,
  },
  {
    title: "05 Au-Haidhausen",
    value: ProjektRequestDTOStadtbezirkEnum.AU_HAIDHAUSEN,
  },
  { title: "06 Sendling", value: ProjektRequestDTOStadtbezirkEnum.SENDLING },
  {
    title: "07 Sendling-Westpark",
    value: ProjektRequestDTOStadtbezirkEnum.SENDLING_WESTPARK,
  },
  {
    title: "08 Schwanthalerhöhe",
    value: ProjektRequestDTOStadtbezirkEnum.SCHWANTHALERHOEHE,
  },
  {
    title: "09 Neuhausen-Nymphenburg",
    value: ProjektRequestDTOStadtbezirkEnum.NEUHAUSEN_NYMPHENBURG,
  },
  { title: "10 Moosach", value: ProjektRequestDTOStadtbezirkEnum.MOOSACH },
  {
    title: "11 Milbertshofen-Am Hart",
    value: ProjektRequestDTOStadtbezirkEnum.MILBERTSHOFEN_AM_HART,
  },
  {
    title: "12 Schwabing-Freimann",
    value: ProjektRequestDTOStadtbezirkEnum.SCHWABING_FREIMANN,
  },
  {
    title: "13 Bogenhausen",
    value: ProjektRequestDTOStadtbezirkEnum.BOGENHAUSEN,
  },
  {
    title: "14 Berg am Laim",
    value: ProjektRequestDTOStadtbezirkEnum.BERG_AM_LAIM,
  },
  {
    title: "15 Trudering-Riem",
    value: ProjektRequestDTOStadtbezirkEnum.TRUDERING_RIEM,
  },
  {
    title: "16 Ramersdorf-Perlach",
    value: ProjektRequestDTOStadtbezirkEnum.RAMERSDORF_PERLACH,
  },
  {
    title: "17 Obergiesing-Fasangarten",
    value: ProjektRequestDTOStadtbezirkEnum.OBERGIESING_FASANGARTEN,
  },
  {
    title: "18 Untergiesing-Harlaching",
    value: ProjektRequestDTOStadtbezirkEnum.UNTERGIESING_HARLACHING,
  },
  {
    title: "19 Thalkirchen-Obersendling-Forstenried-Fürstenried-Solln",
    value:
      ProjektRequestDTOStadtbezirkEnum.THALKIRCHEN_OBERSENDLING_FORSTENRIED_FUERSTENRIED_SOLLN,
  },
  { title: "20 Hadern", value: ProjektRequestDTOStadtbezirkEnum.HADERN },
  {
    title: "21 Pasing-Obermenzing",
    value: ProjektRequestDTOStadtbezirkEnum.PASING_OBERMENZING,
  },
  {
    title: "22 Aubing-Lochhausen-Langwied",
    value: ProjektRequestDTOStadtbezirkEnum.AUBING_LOCHHAUSEN_LANGWIED,
  },
  {
    title: "23 Allach-Untermenzing",
    value: ProjektRequestDTOStadtbezirkEnum.ALLACH_UNTERMENZING,
  },
  {
    title: "24 Feldmoching-Hasenbergl",
    value: ProjektRequestDTOStadtbezirkEnum.FELDMOCHING_HASENBERGL,
  },
  { title: "25 Laim", value: ProjektRequestDTOStadtbezirkEnum.LAIM },
];

export function stadtbezirkLabel(
  stadtbezirk: ProjektResponseDTOStadtbezirkEnum | undefined
): string {
  return (
    STADTBEZIRK_OPTIONS.find((option) => option.value === stadtbezirk)?.title ??
    ""
  );
}
