<template>
  <v-container>
    <v-btn
      class="mb-2"
      :prepend-icon="mdiArrowLeft"
      variant="text"
      to="/projekte"
    >
      Zurück zu den Projekten
    </v-btn>

    <div class="d-flex align-center flex-wrap mb-6">
      <h1 class="text-display-medium font-weight-bold">Abrechnungen</h1>
      <v-spacer />
      <v-btn
        color="primary"
        :prepend-icon="mdiPlus"
        :to="`/projekte/${projektId}/abrechnungen/anlegen`"
      >
        Abrechnung anlegen
      </v-btn>
    </div>

    <abrechnung-table
      v-model:page="page"
      v-model:items-per-page="itemsPerPage"
      v-model:sort-by="sortBy"
      :rows="rows"
      :total-abrechnungen="totalAbrechnungen"
      :loading="loading"
    />
  </v-container>
</template>

<script setup lang="ts">
import type { DataTableSortItem } from "@/types/DataTableSortItem";

import { mdiArrowLeft, mdiPlus } from "@mdi/js";
import { computed, onMounted, ref, watch } from "vue";

import AbrechnungTable from "@/components/AbrechnungTable.vue";
import { useAbrechnungenListe } from "@/composables/abrechnungenListe";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import { toAbrechnungSort } from "@/util/abrechnungSortMapper";

const DEFAULT_ITEMS_PER_PAGE = 10;

const DEFAULT_SORT: DataTableSortItem[] = [
  { key: "zeitraumVon", order: "desc" },
];

const { projektId } = defineProps<{ projektId: string }>();

const snackbarStore = useSnackbarStore();

const { load, loading, rows, totalAbrechnungen } = useAbrechnungenListe();

const page = ref(1);
const itemsPerPage = ref(DEFAULT_ITEMS_PER_PAGE);
const sortBy = ref<DataTableSortItem[]>([...DEFAULT_SORT]);

const sort = computed(() => toAbrechnungSort(sortBy.value));

async function loadPage(): Promise<void> {
  try {
    await load(projektId, page.value, itemsPerPage.value, sort.value);
  } catch {
    snackbarStore.push({
      text: "Die Abrechnungen konnten nicht geladen werden.",
      color: STATUS_INDICATORS.ERROR,
    });
  }
}

watch(sort, () => {
  page.value = 1;
});

watch([page, itemsPerPage, sort], () => void loadPage());

onMounted(() => void loadPage());
</script>
