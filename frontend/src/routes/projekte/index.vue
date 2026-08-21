<template>
  <v-container>
    <div class="d-flex align-center flex-wrap mb-6">
      <h1 class="text-display-medium font-weight-bold">Projekte</h1>
      <v-spacer />
      <v-btn
        v-if="isWriter"
        color="primary"
        :prepend-icon="mdiPlus"
        to="/projekte/anlegen"
      >
        Projekt anlegen
      </v-btn>
    </div>

    <v-row class="mb-2">
      <v-col
        cols="12"
        md="4"
      >
        <v-text-field
          v-model="projektnummer"
          label="Projektnummer"
          maxlength="20"
          :prepend-inner-icon="mdiMagnify"
          clearable
          hide-details
        />
      </v-col>
      <v-col
        cols="12"
        md="4"
      >
        <v-text-field
          v-model="abrechnungBeginn"
          label="Abrechnung Beginn"
          type="date"
          clearable
          hide-details
        />
      </v-col>
      <v-col
        cols="12"
        md="4"
      >
        <v-text-field
          v-model="abrechnungEnde"
          label="Abrechnung Ende"
          type="date"
          clearable
          hide-details
        />
      </v-col>
      <v-col
        cols="12"
        class="d-flex justify-end pt-0"
      >
        <v-btn
          :disabled="!isFiltered"
          :prepend-icon="mdiFilterRemove"
          variant="text"
          @click="filterLoeschen"
        >
          Filter löschen
        </v-btn>
      </v-col>
    </v-row>

    <v-data-table-server
      v-model:page="page"
      v-model:items-per-page="itemsPerPage"
      v-model:sort-by="sortBy"
      :headers="headers"
      :items="zeilen"
      :items-length="gesamtAnzahl"
      :items-per-page-options="ITEMS_PER_PAGE_OPTIONS"
      :loading="loading"
      item-value="id"
      :no-data-text="noDataText"
    />
  </v-container>
</template>

<script setup lang="ts">
import type {
  GetProjekteByPageAndSizeSortByEnum,
  GetProjekteByPageAndSizeSortDirectionEnum,
} from "@/api/generated/sonar-backend";
import type { ProjektFilter, ProjektSort } from "@/composables/projekteListe";

import { mdiFilterRemove, mdiMagnify, mdiPlus } from "@mdi/js";
import { watchDebounced } from "@vueuse/core";
import { computed, onMounted, ref, watch } from "vue";

import { useProjekteListe } from "@/composables/projekteListe";
import useHasAnyRole from "@/composables/useHasAnyRole";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import { Role } from "@/types/Role";

definePage({
  meta: {
    hasAnyRole: [Role.READER, Role.WRITER],
  },
});

interface SortItem {
  key: string;
  order?: boolean | "asc" | "desc";
}

const DEFAULT_ITEMS_PER_PAGE = 10;
const ITEMS_PER_PAGE_OPTIONS = [10, 25, 50, 100];
const SEARCH_DEBOUNCE_MS = 300;

const headers = [
  { title: "Projektnummer", key: "projektnummer" },
  { title: "Abrechnung Beginn", key: "abrechnungBeginn" },
  { title: "Abrechnung Ende", key: "abrechnungEnde" },
  { title: "Adressen/Flurnummern", key: "anzahlAdressen", sortable: false },
];

const SORT_BY_OF_COLUMN: Record<string, GetProjekteByPageAndSizeSortByEnum> = {
  projektnummer: "PROJEKTNUMMER",
  abrechnungBeginn: "ABRECHNUNG_BEGINN",
  abrechnungEnde: "ABRECHNUNG_ENDE",
};

const DEFAULT_SORT: SortItem[] = [{ key: "projektnummer", order: "desc" }];

const isWriter = useHasAnyRole(Role.WRITER);
const snackbarStore = useSnackbarStore();

const { gesamtAnzahl, load, loading, zeilen } = useProjekteListe();

const page = ref(1);
const itemsPerPage = ref(DEFAULT_ITEMS_PER_PAGE);
const sortBy = ref<SortItem[]>([...DEFAULT_SORT]);
const projektnummer = ref<string | null>(null);
const abrechnungBeginn = ref<string | null>(null);
const abrechnungEnde = ref<string | null>(null);

const filter = computed<ProjektFilter>(() => ({
  projektnummer: projektnummer.value ?? undefined,
  abrechnungBeginn: abrechnungBeginn.value ?? undefined,
  abrechnungEnde: abrechnungEnde.value ?? undefined,
}));

const sort = computed<ProjektSort>(() => {
  const [first] = sortBy.value;
  const mapped = first ? SORT_BY_OF_COLUMN[first.key] : undefined;
  if (!mapped) {
    return {};
  }
  const sortDirection: GetProjekteByPageAndSizeSortDirectionEnum =
    first?.order === "asc" ? "ASC" : "DESC";
  return { sortBy: mapped, sortDirection };
});

const isFiltered = computed(
  () =>
    Boolean(projektnummer.value) ||
    Boolean(abrechnungBeginn.value) ||
    Boolean(abrechnungEnde.value)
);

const noDataText = computed(() =>
  isFiltered.value
    ? "Keine Projekte gefunden."
    : "Es sind noch keine Projekte angelegt."
);

function filterLoeschen(): void {
  projektnummer.value = null;
  abrechnungBeginn.value = null;
  abrechnungEnde.value = null;
}

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

// A narrowed search can leave fewer pages than the one being displayed, and a different order moves
// the rows across all of them, so both start over at the first page.
watch([filter, sort], () => {
  page.value = 1;
});

// Both changes of a filter- or sort-triggered page reset fall into the same debounce window, so this
// reloads once instead of twice.
watchDebounced([page, itemsPerPage, filter, sort], () => void loadPage(), {
  debounce: SEARCH_DEBOUNCE_MS,
});

onMounted(() => void loadPage());
</script>
