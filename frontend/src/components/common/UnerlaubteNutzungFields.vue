<template>
  <h3 class="text-title-small mt-4 mb-2">Unerlaubte Nutzung</h3>

  <v-row>
    <v-col
      cols="12"
      md="3"
    >
      <date-field
        :id="`${idPrefix}-unerlaubt-von`"
        v-model="unerlaubteNutzung.unerlaubteNutzungVon"
        label="Unerlaubte Nutzung von"
        :readonly="readonly || undefined"
        :rules="[unerlaubteNutzungVonRule(unerlaubteNutzung)]"
      />
    </v-col>
    <v-col
      cols="12"
      md="3"
    >
      <date-field
        :id="`${idPrefix}-unerlaubt-bis`"
        v-model="unerlaubteNutzung.unerlaubteNutzungBis"
        label="Unerlaubte Nutzung bis"
        :readonly="readonly || undefined"
        :rules="[unerlaubteNutzungBisRule(unerlaubteNutzung)]"
        :zeitraum-beginn="unerlaubteNutzung.unerlaubteNutzungVon"
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
        :readonly="readonly || undefined"
      />
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import type { UnerlaubteNutzung } from "@/types/common/UnerlaubteNutzung";

import { computed } from "vue";

import DateField from "@/components/common/DateField.vue";
import {
  hasZeitraum,
  tageUnerlaubteNutzung,
} from "@/util/common/unerlaubteNutzungForm";
import {
  unerlaubteNutzungBisRule,
  unerlaubteNutzungVonRule,
} from "@/util/common/unerlaubteNutzungRules";

const unerlaubteNutzung = defineModel<UnerlaubteNutzung>({ required: true });

const { readonly = false } = defineProps<{
  idPrefix: string;
  readonly?: boolean;
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
