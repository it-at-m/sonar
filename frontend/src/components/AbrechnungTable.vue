<template>
  <v-data-table-server
    v-model:page="page"
    v-model:items-per-page="itemsPerPage"
    v-model:sort-by="sortBy"
    class="abrechnung-table"
    :headers="HEADERS"
    :items="rows"
    :items-length="totalAbrechnungen"
    :items-per-page-options="ITEMS_PER_PAGE_OPTIONS"
    :loading="loading"
    item-value="id"
    multi-sort
    no-data-text="Es sind noch keine Abrechnungen angelegt."
  />
</template>

<script setup lang="ts">
import type { AbrechnungTableRow } from "@/types/AbrechnungTableRow";
import type { DataTableSortItem } from "@/types/DataTableSortItem";

const HEADERS = [
  { title: "Geschäftspartner:in", key: "geschaeftspartnerId" },
  { title: "Zeitraum von", key: "zeitraumVon" },
  { title: "Zeitraum bis", key: "zeitraumBis" },
  { title: "Art", key: "abrechnungsArt" },
  { title: "Nutzungsobjekte", key: "anzahlNutzungsobjekte", sortable: false },
];

const ITEMS_PER_PAGE_OPTIONS = [10, 25, 50, 100];

const page = defineModel<number>("page", { required: true });
const itemsPerPage = defineModel<number>("itemsPerPage", { required: true });
const sortBy = defineModel<DataTableSortItem[]>("sortBy", { required: true });

defineProps<{
  rows: AbrechnungTableRow[];
  totalAbrechnungen: number;
  loading: boolean;
}>();
</script>

<style scoped>
/*
 * Vuetify puts the badge with the sort position into a column only while that column is sorted,
 * and the table takes its column widths from the content. Every click would resize the columns.
 * The placeholder holds the space of the badge open as long as a column carries none.
 */
.abrechnung-table
  :deep(.v-data-table__th--sortable)
  .v-data-table-header__content:not(
    :has(.v-data-table-header__sort-badge)
  )::after {
  content: "";
  /* Same as .v-data-table-header__sort-badge, which has no margin of its own. */
  width: 20px;
}
</style>
