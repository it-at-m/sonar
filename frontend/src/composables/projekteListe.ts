/**
 * Loads the Projekte for the overview table, one page at a time.
 */
import type {
  GetProjekteByPageAndSizeSortByEnum,
  GetProjekteByPageAndSizeSortDirectionEnum,
  ProjektResponseDTO,
} from "@/api/generated/sonar-backend";

import { ref } from "vue";

import { ApiFactory } from "@/api/ApiFactory";
import { ProjektControllerApi } from "@/api/generated/sonar-backend";
import { toDateString } from "@/util/formatter";

/**
 * Search criteria, all optional and combined with AND. The Projektnummer matches anywhere in the
 * value, the dates match exactly. Dates are ISO strings (yyyy-MM-dd).
 */
export interface ProjektFilter {
  projektnummer?: string;
  abrechnungBeginn?: string;
  abrechnungEnde?: string;
}

export interface ProjektSort {
  sortBy?: GetProjekteByPageAndSizeSortByEnum;
  sortDirection?: GetProjekteByPageAndSizeSortDirectionEnum;
}

export interface ProjektZeile {
  id?: string;
  projektnummer?: string;
  abrechnungBeginn: string;
  abrechnungEnde: string;
  anzahlAdressen: number;
}

export function toZeile(projekt: ProjektResponseDTO): ProjektZeile {
  return {
    id: projekt.id,
    projektnummer: projekt.projektnummer,
    abrechnungBeginn: projekt.abrechnungBeginn
      ? toDateString(projekt.abrechnungBeginn)
      : "",
    abrechnungEnde: projekt.abrechnungEnde
      ? toDateString(projekt.abrechnungEnde)
      : "",
    anzahlAdressen: projekt.adressen?.length ?? 0,
  };
}

export function useProjekteListe() {
  const zeilen = ref<ProjektZeile[]>([]);
  const gesamtAnzahl = ref(0);
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
      zeilen.value = (result.content ?? []).map(toZeile);
      gesamtAnzahl.value = result.page?.totalElements ?? 0;
    } finally {
      loading.value = false;
    }
  }

  return { gesamtAnzahl, load, loading, zeilen };
}
