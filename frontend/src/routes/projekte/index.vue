<template>
  <v-container>
    <div class="d-flex align-center flex-wrap mb-6">
      <h1 class="text-display-medium font-weight-bold">Projekte</h1>
      <v-spacer />
      <v-btn
        color="primary"
        :prepend-icon="mdiPlus"
        to="/projekte/anlegen"
      >
        Projekt anlegen
      </v-btn>
    </div>

    <projekt-filter-bar v-model="filter" />

    <projekt-table
      v-model:page="page"
      v-model:items-per-page="itemsPerPage"
      v-model:sort-by="sortBy"
      :rows="rows"
      :total-projekte="totalProjekte"
      :loading="loading"
      :filtered="filtered"
    />
  </v-container>
</template>

<script setup lang="ts">
import type { DataTableSortItem } from "@/types/DataTableSortItem";
import type { ProjektFilter } from "@/types/ProjektFilter";

import { mdiPlus } from "@mdi/js";
import { watchDebounced } from "@vueuse/core";
import { computed, onMounted, ref, watch } from "vue";

import ProjektFilterBar from "@/components/projekt/ProjektFilterBar.vue";
import ProjektTable from "@/components/projekt/ProjektTable.vue";
import { useProjekteListe } from "@/composables/projekteListe";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import { toProjektSort } from "@/util/projektSortMapper";

const DEFAULT_ITEMS_PER_PAGE = 10;
const SEARCH_DEBOUNCE_MS = 300;

const DEFAULT_SORT: DataTableSortItem[] = [
  { key: "projektnummer", order: "desc" },
];

const snackbarStore = useSnackbarStore();

const { load, loading, rows, totalProjekte } = useProjekteListe();

const page = ref(1);
const itemsPerPage = ref(DEFAULT_ITEMS_PER_PAGE);
const sortBy = ref<DataTableSortItem[]>([...DEFAULT_SORT]);
const filter = ref<ProjektFilter>({});

const sort = computed(() => toProjektSort(sortBy.value));

const filtered = computed(() =>
  Boolean(
    filter.value.projektnummer ||
    filter.value.abrechnungBeginn ||
    filter.value.abrechnungEnde
  )
);

async function loadPage(): Promise<void> {
  try {
    await load(page.value, itemsPerPage.value, filter.value, sort.value);
  } catch {
    snackbarStore.push({
      text: "Die Projekte konnten nicht geladen werden.",
      color: STATUS_INDICATORS.ERROR,
    });
  }
}

watch([filter, sort], () => {
  page.value = 1;
});

watchDebounced([page, itemsPerPage, filter, sort], () => void loadPage(), {
  debounce: SEARCH_DEBOUNCE_MS,
});

onMounted(() => void loadPage());
</script>
