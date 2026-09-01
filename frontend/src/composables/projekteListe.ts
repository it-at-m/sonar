import type { ProjektFilter } from "@/types/ProjektFilter";
import type { ProjektSort } from "@/types/ProjektSort";
import type { ProjektTableRow } from "@/types/ProjektTableRow";

import { ref } from "vue";

import { ApiFactory } from "@/api/ApiFactory";
import { ProjektControllerApi } from "@/api/generated/sonar-backend";
import { toProjektTableRow } from "@/util/projektTableRowMapper";

export function useProjekteListe() {
  const rows = ref<ProjektTableRow[]>([]);
  const totalProjekte = ref(0);
  const loading = ref(false);

  async function load(
    oneBasedPage: number,
    itemsPerPage: number,
    filter: ProjektFilter = {},
    sort: ProjektSort = {}
  ): Promise<void> {
    loading.value = true;
    try {
      const result = await ApiFactory.getInstance(
        ProjektControllerApi
      ).getProjekteByPageAndSize(
        oneBasedPage - 1,
        itemsPerPage,
        filter.projektnummer || undefined,
        filter.abrechnungBeginn ? new Date(filter.abrechnungBeginn) : undefined,
        filter.abrechnungEnde ? new Date(filter.abrechnungEnde) : undefined,
        sort.sortBy,
        sort.sortDirection
      );
      rows.value = (result.content ?? []).map(toProjektTableRow);
      totalProjekte.value = result.page?.totalElements ?? 0;
    } finally {
      loading.value = false;
    }
  }

  return { load, loading, rows, totalProjekte };
}
