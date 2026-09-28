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
  >
    <template #[`item.widerspruch`]="{ item }">
      <span v-if="item.id">
        <v-btn
          :aria-label="widerspruchLabel(item)"
          density="comfortable"
          :disabled="item.widerspruchVorhanden"
          :icon="item.widerspruchVorhanden ? mdiChatAlert : mdiChatPlus"
          :to="widerspruchAnlegenLink(item)"
          variant="text"
        />
        <v-tooltip
          v-if="item.widerspruchVorhanden"
          activator="parent"
          text="Es besteht bereits ein Widerspruch."
        />
      </span>
    </template>
  </v-data-table-server>
</template>

<script setup lang="ts">
import type { AbrechnungTableRow } from "@/types/abrechnung/AbrechnungTableRow";
import type { DataTableSortItem } from "@/types/DataTableSortItem";

import { mdiChatAlert, mdiChatPlus } from "@mdi/js";

const HEADERS = [
  { title: "Geschäftspartner:in", key: "geschaeftspartnerId" },
  { title: "Zeitraum von", key: "zeitraumVon" },
  { title: "Zeitraum bis", key: "zeitraumBis" },
  { title: "Art", key: "abrechnungsArt" },
  { title: "Nutzungsobjekte", key: "anzahlNutzungsobjekte", sortable: false },
  { title: "Widerspruch", key: "widerspruch", sortable: false },
];

const ITEMS_PER_PAGE_OPTIONS = [10, 25, 50, 100];

const page = defineModel<number>("page", { required: true });
const itemsPerPage = defineModel<number>("itemsPerPage", { required: true });
const sortBy = defineModel<DataTableSortItem[]>("sortBy", { required: true });

const props = defineProps<{
  projektId: string;
  rows: AbrechnungTableRow[];
  totalAbrechnungen: number;
  loading: boolean;
}>();

function widerspruchLabel(row: AbrechnungTableRow): string {
  const zustand = row.widerspruchVorhanden ? "vorhanden" : "anlegen";
  return `Widerspruch zu Abrechnung ${row.geschaeftspartnerId} ${zustand}`;
}

function widerspruchAnlegenLink(row: AbrechnungTableRow): string | undefined {
  return row.widerspruchVorhanden
    ? undefined
    : `/projekte/${props.projektId}/abrechnungen/${row.id}/widerspruch/anlegen`;
}
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
