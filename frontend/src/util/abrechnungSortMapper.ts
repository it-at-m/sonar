import type {
  GetAbrechnungenByPageAndSizeSortByEnum,
  GetAbrechnungenByPageAndSizeSortDirectionEnum,
} from "@/api/generated/sonar-backend";
import type { AbrechnungSort } from "@/types/AbrechnungSort";
import type { DataTableSortItem } from "@/types/DataTableSortItem";

const SORT_BY_OF_COLUMN: Record<
  string,
  GetAbrechnungenByPageAndSizeSortByEnum
> = {
  geschaeftspartnerId: "GESCHAEFTSPARTNER_ID",
  zeitraumVon: "ZEITRAUM_VON",
  zeitraumBis: "ZEITRAUM_BIS",
  abrechnungsArt: "ABRECHNUNGS_ART",
  versionsnummer: "VERSIONSNUMMER",
};

export function toAbrechnungSort(
  sortItems: DataTableSortItem[]
): AbrechnungSort {
  const sortBy: GetAbrechnungenByPageAndSizeSortByEnum[] = [];
  const sortDirection: GetAbrechnungenByPageAndSizeSortDirectionEnum[] = [];

  for (const sortItem of sortItems) {
    const column = SORT_BY_OF_COLUMN[sortItem.key];
    if (!column) {
      continue;
    }
    sortBy.push(column);
    sortDirection.push(sortItem.order === "asc" ? "ASC" : "DESC");
  }

  if (sortBy.length === 0) {
    return {};
  }
  return { sortBy, sortDirection };
}
