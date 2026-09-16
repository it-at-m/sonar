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
    <template #[`item.ansehen`]="{ item }">
      <v-btn
        v-if="item.id"
        :aria-label="`Abrechnung ${item.geschaeftspartnerId} in Version ${item.versionsnummer} ansehen`"
        density="comfortable"
        :icon="mdiEye"
        :to="`/projekte/${projektId}/abrechnungen/${item.id}/ansehen`"
        variant="text"
      />
    </template>

    <template #[`item.widerspruch`]="{ item }">
      <span v-if="item.id">
        <v-btn
          :aria-label="`Widerspruch zu Abrechnung ${item.geschaeftspartnerId} anlegen`"
          density="comfortable"
          :disabled="item.widerspruchVorhanden"
          :icon="item.widerspruchVorhanden ? mdiChatAlert : mdiChatPlus"
          :to="`/projekte/${projektId}/abrechnungen/${item.id}/widerspruch/anlegen`"
          variant="text"
        />
        <v-tooltip
          v-if="item.widerspruchVorhanden"
          activator="parent"
          text="Es besteht bereits ein Widerspruch."
        />
      </span>
    </template>

    <template #[`item.neueVersion`]="{ item }">
      <v-btn
        v-if="item.id"
        :aria-label="`Neue Version der Abrechnung ${item.geschaeftspartnerId} anlegen`"
        density="comfortable"
        :icon="mdiFileDocumentPlus"
        :to="`/projekte/${projektId}/abrechnungen/${item.id}/version/anlegen`"
        variant="text"
      />
    </template>
  </v-data-table-server>
</template>

<script setup lang="ts">
import type { AbrechnungTableRow } from "@/types/AbrechnungTableRow";
import type { DataTableSortItem } from "@/types/DataTableSortItem";

import {
  mdiChatAlert,
  mdiChatPlus,
  mdiEye,
  mdiFileDocumentPlus,
} from "@mdi/js";

const HEADERS = [
  { title: "Geschäftspartner:in", key: "geschaeftspartnerId" },
  { title: "Zeitraum von", key: "zeitraumVon" },
  { title: "Zeitraum bis", key: "zeitraumBis" },
  { title: "Art", key: "abrechnungsArt" },
  { title: "Version", key: "versionsnummer" },
  { title: "Nutzungsobjekte", key: "anzahlNutzungsobjekte", sortable: false },
  { title: "Ansehen", key: "ansehen", sortable: false },
  { title: "Widerspruch", key: "widerspruch", sortable: false },
  { title: "Neue Version", key: "neueVersion", sortable: false },
];

const ITEMS_PER_PAGE_OPTIONS = [10, 25, 50, 100];

const page = defineModel<number>("page", { required: true });
const itemsPerPage = defineModel<number>("itemsPerPage", { required: true });
const sortBy = defineModel<DataTableSortItem[]>("sortBy", { required: true });

defineProps<{
  projektId: string;
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
