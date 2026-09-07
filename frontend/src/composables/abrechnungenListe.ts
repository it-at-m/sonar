import type { AbrechnungSort } from "@/types/AbrechnungSort";
import type { AbrechnungTableRow } from "@/types/AbrechnungTableRow";

import { ref } from "vue";

import { ApiFactory } from "@/api/ApiFactory";
import { AbrechnungControllerApi } from "@/api/generated/sonar-backend";
import { toAbrechnungTableRow } from "@/util/abrechnungTableRowMapper";

export function useAbrechnungenListe() {
  const rows = ref<AbrechnungTableRow[]>([]);
  const totalAbrechnungen = ref(0);
  const loading = ref(false);

  async function load(
    projektId: string,
    oneBasedPage: number,
    itemsPerPage: number,
    sort: AbrechnungSort = {}
  ): Promise<void> {
    loading.value = true;
    try {
      const result = await ApiFactory.getInstance(
        AbrechnungControllerApi
      ).getAbrechnungenByPageAndSize(
        projektId,
        oneBasedPage - 1,
        itemsPerPage,
        sort.sortBy,
        sort.sortDirection
      );
      rows.value = (result.content ?? []).map(toAbrechnungTableRow);
      totalAbrechnungen.value = result.page?.totalElements ?? 0;
    } finally {
      loading.value = false;
    }
  }

  return { load, loading, rows, totalAbrechnungen };
}
