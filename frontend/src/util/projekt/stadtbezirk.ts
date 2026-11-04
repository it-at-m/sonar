import type { ProjektResponseDTOStadtbezirkEnum } from "@/api/generated/sonar-backend";

import { ProjektRequestDTOStadtbezirkEnum } from "@/api/generated/sonar-backend";

export const STADTBEZIRK_OPTIONS = [
  {
    title: "01 Altstadt-Lehel",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_01_ALTSTADT_LEHEL,
  },
  {
    title: "02 Ludwigsvorstadt-Isarvorstadt",
    value:
      ProjektRequestDTOStadtbezirkEnum.BEZIRK_02_LUDWIGSVORSTADT_ISARVORSTADT,
  },
  {
    title: "03 Maxvorstadt",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_03_MAXVORSTADT,
  },
  {
    title: "04 Schwabing-West",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_04_SCHWABING_WEST,
  },
  {
    title: "05 Au-Haidhausen",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_05_AU_HAIDHAUSEN,
  },
  {
    title: "06 Sendling",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_06_SENDLING,
  },
  {
    title: "07 Sendling-Westpark",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_07_SENDLING_WESTPARK,
  },
  {
    title: "08 Schwanthalerhöhe",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_08_SCHWANTHALERHOEHE,
  },
  {
    title: "09 Neuhausen-Nymphenburg",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_09_NEUHAUSEN_NYMPHENBURG,
  },
  {
    title: "10 Moosach",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_10_MOOSACH,
  },
  {
    title: "11 Milbertshofen-Am Hart",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_11_MILBERTSHOFEN_AM_HART,
  },
  {
    title: "12 Schwabing-Freimann",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_12_SCHWABING_FREIMANN,
  },
  {
    title: "13 Bogenhausen",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_13_BOGENHAUSEN,
  },
  {
    title: "14 Berg am Laim",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_14_BERG_AM_LAIM,
  },
  {
    title: "15 Trudering-Riem",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_15_TRUDERING_RIEM,
  },
  {
    title: "16 Ramersdorf-Perlach",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_16_RAMERSDORF_PERLACH,
  },
  {
    title: "17 Obergiesing-Fasangarten",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_17_OBERGIESING_FASANGARTEN,
  },
  {
    title: "18 Untergiesing-Harlaching",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_18_UNTERGIESING_HARLACHING,
  },
  {
    title: "19 Thalkirchen-Obersendling-Forstenried-Fürstenried-Solln",
    value:
      ProjektRequestDTOStadtbezirkEnum.BEZIRK_19_THALKIRCHEN_OBERSENDLING_FORSTENRIED_FUERSTENRIED_SOLLN,
  },
  {
    title: "20 Hadern",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_20_HADERN,
  },
  {
    title: "21 Pasing-Obermenzing",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_21_PASING_OBERMENZING,
  },
  {
    title: "22 Aubing-Lochhausen-Langwied",
    value:
      ProjektRequestDTOStadtbezirkEnum.BEZIRK_22_AUBING_LOCHHAUSEN_LANGWIED,
  },
  {
    title: "23 Allach-Untermenzing",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_23_ALLACH_UNTERMENZING,
  },
  {
    title: "24 Feldmoching-Hasenbergl",
    value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_24_FELDMOCHING_HASENBERGL,
  },
  { title: "25 Laim", value: ProjektRequestDTOStadtbezirkEnum.BEZIRK_25_LAIM },
];

export function stadtbezirkLabel(
  stadtbezirk: ProjektResponseDTOStadtbezirkEnum | undefined
): string {
  return (
    STADTBEZIRK_OPTIONS.find((option) => option.value === stadtbezirk)?.title ??
    ""
  );
}
