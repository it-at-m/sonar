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
  >
    <template #[`item.verknuepfung`]="{ item }">
      <v-tooltip
        v-if="item.id && item.verknuepfungsstatus !== 'NICHT_MOEGLICH'"
      >
        <template #activator="{ props: activatorProps }">
          <v-btn
            v-bind="activatorProps"
            :aria-label="`Verknüpfung von Projekt ${item.projektnummer} bearbeiten`"
            density="comfortable"
            icon
            variant="text"
            @click="emit('verknuepfen', item)"
          >
            <v-icon :icon="mdiLinkVariant" />
            <span
              v-if="item.verknuepfungsstatus === 'OFFEN'"
              class="verknuepfung-offen bg-warning"
              >!</span
            >
          </v-btn>
        </template>
        <template v-if="item.verknuepfungsstatus === 'VERKNUEPFT'">
          Verknüpfungen: <projektkette-text :kette="item.projektkette" />
        </template>
        <template v-else>{{
          VERKNUEPFUNG_TEXT[item.verknuepfungsstatus]
        }}</template>
      </v-tooltip>
    </template>

    <template #[`item.abrechnungen`]="{ item }">
      <v-btn
        v-if="item.id"
        :aria-label="`Abrechnungen zu Projekt ${item.projektnummer} anzeigen`"
        density="comfortable"
        :icon="mdiReceiptTextOutline"
        :to="`/projekte/${item.id}/abrechnungen`"
        variant="text"
      />
    </template>
  </v-data-table-server>
</template>

<script setup lang="ts">
import type { DataTableSortItem } from "@/types/DataTableSortItem";
import type { ProjektTableRow } from "@/types/projekt/ProjektTableRow";
import type { Verknuepfungsstatus } from "@/types/projekt/Verknuepfungsstatus";

import { mdiLinkVariant, mdiReceiptTextOutline } from "@mdi/js";
import { computed } from "vue";

import ProjektketteText from "@/components/projekt/ProjektketteText.vue";

const VERKNUEPFUNG_TEXT: Record<
  Exclude<Verknuepfungsstatus, "VERKNUEPFT" | "NICHT_MOEGLICH">,
  string
> = {
  ABGELEHNT: "Verknüpfung abgelehnt",
  OFFEN: "Verknüpfung möglich, noch nicht entschieden",
};

const HEADERS = [
  { title: "Projektnummer", key: "projektnummer" },
  { title: "Abrechnung Beginn", key: "abrechnungBeginn" },
  { title: "Abrechnung Ende", key: "abrechnungEnde" },
  { title: "Adressen/Flurnummern", key: "anzahlAdressen", sortable: false },
  { title: "Verknüpfung", key: "verknuepfung", sortable: false },
  { title: "Abrechnungen", key: "abrechnungen", sortable: false },
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

const emit = defineEmits<{ verknuepfen: [projekt: ProjektTableRow] }>();

const noDataText = computed(() =>
  props.filtered
    ? "Keine Projekte gefunden."
    : "Es sind noch keine Projekte angelegt."
);
</script>

<style scoped>
/* The round button clips everything outside its circle, so the mark sits inside it instead of in a v-badge. */
.verknuepfung-offen {
  position: absolute;
  top: 3px;
  right: 3px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgb(var(--v-theme-surface));
  font-size: 12px;
  font-weight: 700;
  line-height: 16px;
  text-align: center;
}
</style>
