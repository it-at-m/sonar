import type { AbrechnungSort } from "@/types/AbrechnungSort";
import type { AbrechnungTableRow } from "@/types/AbrechnungTableRow";

import { ApiFactory } from "@/api/ApiFactory";
import { AbrechnungControllerApi } from "@/api/generated/sonar-backend";
import { toAbrechnungTableRow } from "@/util/abrechnungTableRowMapper";

export interface AbrechnungenPage {
  rows: AbrechnungTableRow[];
  totalAbrechnungen: number;
}

export async function fetchAbrechnungenPage(
  projektId: string,
  oneBasedPage: number,
  itemsPerPage: number,
  sort: AbrechnungSort = {}
): Promise<AbrechnungenPage> {
  const result = await ApiFactory.getInstance(
    AbrechnungControllerApi
  ).getAbrechnungenByPageAndSize(
    projektId,
    oneBasedPage - 1,
    itemsPerPage,
    sort.sortBy,
    sort.sortDirection
  );
  return {
    rows: (result.content ?? []).map(toAbrechnungTableRow),
    totalAbrechnungen: result.page?.totalElements ?? 0,
  };
}
