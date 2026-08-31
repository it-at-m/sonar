import type { GetProjekteByPageAndSizeSortByEnum } from "@/api/generated/sonar-backend";
import type { DataTableSortItem } from "@/types/DataTableSortItem";
import type { ProjektSort } from "@/types/ProjektSort";

const SORT_BY_OF_COLUMN: Record<string, GetProjekteByPageAndSizeSortByEnum> = {
  projektnummer: "PROJEKTNUMMER",
  abrechnungBeginn: "ABRECHNUNG_BEGINN",
  abrechnungEnde: "ABRECHNUNG_ENDE",
};

export function toProjektSort(sortBy: DataTableSortItem[]): ProjektSort {
  const [first] = sortBy;
  if (!first) {
    return {};
  }
  const mapped = SORT_BY_OF_COLUMN[first.key];
  if (!mapped) {
    return {};
  }
  return {
    sortBy: mapped,
    sortDirection: first.order === "asc" ? "ASC" : "DESC",
  };
}
