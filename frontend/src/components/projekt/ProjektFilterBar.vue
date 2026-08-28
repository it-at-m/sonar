<template>
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
        :disabled="!filtered"
        :prepend-icon="mdiFilterRemove"
        variant="text"
        @click="filterLoeschen"
      >
        Filter löschen
      </v-btn>
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import type { ProjektFilter } from "@/types/ProjektFilter";

import { mdiFilterRemove, mdiMagnify } from "@mdi/js";
import { computed } from "vue";

const filter = defineModel<ProjektFilter>({ required: true });

const filtered = computed(() =>
  Boolean(
    filter.value.projektnummer ||
    filter.value.abrechnungBeginn ||
    filter.value.abrechnungEnde
  )
);

function filterModel(entityAttribute: keyof ProjektFilter) {
  return computed({
    get: () => filter.value[entityAttribute] ?? null,
    set: (value: string | null) => {
      filter.value = { ...filter.value, [entityAttribute]: value ?? undefined };
    },
  });
}

const projektnummer = filterModel("projektnummer");
const abrechnungBeginn = filterModel("abrechnungBeginn");
const abrechnungEnde = filterModel("abrechnungEnde");

function filterLoeschen(): void {
  filter.value = {};
}
</script>
