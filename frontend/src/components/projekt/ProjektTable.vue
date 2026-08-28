<template>
  <v-data-table-server
    v-model:page="page"
    v-model:items-per-page="itemsPerPage"
    v-model:sort-by="sortBy"
    :headers="HEADERS"
    :items="rows"
    :items-length="totalProjekte"
    :items-per-page-options="ITEMS_PER_PAGE_OPTIONS"
    :loading="loading"
    item-value="id"
    :no-data-text="noDataText"
  />
</template>

<script setup lang="ts">
import type { DataTableSortItem } from "@/types/DataTableSortItem";
import type { ProjektTableRow } from "@/types/ProjektTableRow";

import { computed } from "vue";

const HEADERS = [
  { title: "Projektnummer", key: "projektnummer" },
  { title: "Abrechnung Beginn", key: "abrechnungBeginn" },
  { title: "Abrechnung Ende", key: "abrechnungEnde" },
  { title: "Adressen/Flurnummern", key: "anzahlAdressen", sortable: false },
];

const ITEMS_PER_PAGE_OPTIONS = [10, 25, 50, 100];

const page = defineModel<number>("page", { required: true });
const itemsPerPage = defineModel<number>("itemsPerPage", { required: true });
const sortBy = defineModel<DataTableSortItem[]>("sortBy", { required: true });

const props = defineProps<{
  rows: ProjektTableRow[];
  totalProjekte: number;
  loading: boolean;
  filtered: boolean;
}>();

const noDataText = computed(() =>
  props.filtered
    ? "Keine Projekte gefunden."
    : "Es sind noch keine Projekte angelegt."
);
</script>
