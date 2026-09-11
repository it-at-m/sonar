<template>
  <h3 class="text-title-small mt-4 mb-2">Unerlaubte Nutzung</h3>

  <v-row>
    <v-col
      cols="12"
      md="3"
    >
      <v-text-field
        :id="`${idPrefix}-unerlaubt-von`"
        v-model="unerlaubteNutzung.unerlaubteNutzungVon"
        label="Unerlaubte Nutzung von"
        :rules="[unerlaubteNutzungVonRule(unerlaubteNutzung)]"
        type="date"
      />
    </v-col>
    <v-col
      cols="12"
      md="3"
    >
      <v-text-field
        :id="`${idPrefix}-unerlaubt-bis`"
        v-model="unerlaubteNutzung.unerlaubteNutzungBis"
        label="Unerlaubte Nutzung bis"
        :rules="[unerlaubteNutzungBisRule(unerlaubteNutzung)]"
        type="date"
      />
    </v-col>
    <v-col
      cols="12"
      md="3"
    >
      <v-number-input
        :id="`${idPrefix}-tage`"
        v-model="tage"
        :disabled="tageAusZeitraum"
        :hint="tageHint"
        label="Tage unerlaubte Nutzung"
        :min="1"
        persistent-hint
        :precision="0"
      />
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import type { UnerlaubteNutzung } from "@/types/UnerlaubteNutzung";

import { computed } from "vue";

import { hasZeitraum, tageUnerlaubteNutzung } from "@/util/unerlaubteNutzung";
import {
  unerlaubteNutzungBisRule,
  unerlaubteNutzungVonRule,
} from "@/util/unerlaubteNutzungRules";

const unerlaubteNutzung = defineModel<UnerlaubteNutzung>({ required: true });

defineProps<{
  idPrefix: string;
}>();

const tageAusZeitraum = computed(() => hasZeitraum(unerlaubteNutzung.value));

const tage = computed({
  get: () => tageUnerlaubteNutzung(unerlaubteNutzung.value) ?? null,
  set: (value: number | null) => {
    // A period wins, so a directly entered count is kept but not overwritten while one is given.
    if (!tageAusZeitraum.value) {
      unerlaubteNutzung.value.tageUnerlaubteNutzung = value;
    }
  },
});

const tageHint = computed(() =>
  tageAusZeitraum.value
    ? "Aus dem Zeitraum berechnet"
    : "Alternativ zum Zeitraum eintragbar"
);
</script>
