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
import type { AbrechnungTableRow } from "@/types/AbrechnungTableRow";
import type { DataTableSortItem } from "@/types/DataTableSortItem";

import { mdiArrowLeft, mdiPlus } from "@mdi/js";
import { computed, onMounted, ref, watch } from "vue";

import AbrechnungTable from "@/components/AbrechnungTable.vue";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import { fetchAbrechnungenPage } from "@/util/abrechnungenListe";
import { toAbrechnungSort } from "@/util/abrechnungSortMapper";

const DEFAULT_ITEMS_PER_PAGE = 10;

const DEFAULT_SORT: DataTableSortItem[] = [
  { key: "zeitraumVon", order: "desc" },
];

const { projektId } = defineProps<{ projektId: string }>();

const snackbarStore = useSnackbarStore();

const page = ref(1);
const itemsPerPage = ref(DEFAULT_ITEMS_PER_PAGE);
const sortBy = ref<DataTableSortItem[]>([...DEFAULT_SORT]);
const rows = ref<AbrechnungTableRow[]>([]);
const totalAbrechnungen = ref(0);
const loading = ref(false);

const sort = computed(() => toAbrechnungSort(sortBy.value));

async function loadPage(): Promise<void> {
  loading.value = true;
  try {
    const abrechnungenPage = await fetchAbrechnungenPage(
      projektId,
      page.value,
      itemsPerPage.value,
      sort.value
    );
    rows.value = abrechnungenPage.rows;
    totalAbrechnungen.value = abrechnungenPage.totalAbrechnungen;
  } catch {
    snackbarStore.push({
      text: "Die Abrechnungen konnten nicht geladen werden.",
      color: STATUS_INDICATORS.ERROR,
    });
  } finally {
    loading.value = false;
  }
}

watch(sort, () => {
  page.value = 1;
});

watch([page, itemsPerPage, sort], () => void loadPage());

onMounted(() => void loadPage());
</script>
