<template>
  <v-card
    class="mb-4"
    variant="outlined"
  >
    <v-card-title class="d-flex align-center">
      <span class="text-title-medium">Adresse {{ position }}</span>
      <v-spacer />
      <v-btn
        :aria-label="`Adresse ${position} entfernen`"
        :disabled="!removable"
        :icon="mdiDelete"
        variant="text"
        @click="emit('remove')"
      />
    </v-card-title>
    <v-card-text>
      <v-row>
        <v-col
          cols="12"
          md="6"
        >
          <v-text-field
            v-model="adresse.bezeichnung"
            label="Adresse, Hausnummer oder Flurstück"
            maxlength="255"
            :rules="[requiredRule]"
          />
        </v-col>
        <v-col
          cols="12"
          md="6"
        >
          <v-text-field
            v-model="adresse.baunutzung"
            label="Baunutzung"
            maxlength="255"
          />
        </v-col>
        <v-col
          cols="12"
          md="3"
        >
          <v-text-field
            v-model="adresse.unerlaubteNutzungVon"
            label="Unerlaubte Nutzung von"
            type="date"
            :rules="[unerlaubteNutzungVonRule(adresse)]"
          />
        </v-col>
        <v-col
          cols="12"
          md="3"
        >
          <v-text-field
            v-model="adresse.unerlaubteNutzungBis"
            label="Unerlaubte Nutzung bis"
            type="date"
            :rules="[unerlaubteNutzungBisRule(adresse)]"
          />
        </v-col>
        <v-col
          cols="12"
          md="6"
        >
          <v-number-input
            v-model="tage"
            label="Tage unerlaubte Nutzung"
            :min="1"
            :precision="0"
            :disabled="tageAusZeitraum"
            :hint="tageHint"
            persistent-hint
          />
        </v-col>
        <v-col
          cols="12"
          md="3"
        >
          <v-number-input
            v-model="adresse.anzahlMahnungen"
            label="Anzahl Mahnungen"
            :min="0"
            :precision="0"
            :rules="[requiredRule]"
          />
        </v-col>
        <v-col
          cols="12"
          md="9"
        >
          <v-checkbox
            v-model="adresse.sondernutzungErlaubt"
            label="Sondernutzung erlaubt"
            hide-details
          />
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import type { ProjektAdresseForm } from "@/types/ProjektAdresseForm";

import { mdiDelete } from "@mdi/js";
import { computed } from "vue";

import { hasZeitraum, tageUnerlaubteNutzung } from "@/util/unerlaubteNutzung";
import {
  unerlaubteNutzungBisRule,
  unerlaubteNutzungVonRule,
} from "@/util/unerlaubteNutzungRules";
import { requiredRule } from "@/util/validationRules";

const adresse = defineModel<ProjektAdresseForm>({ required: true });

defineProps<{
  position: number;
  removable: boolean;
}>();

const emit = defineEmits<{ remove: [] }>();

const tageAusZeitraum = computed(() => hasZeitraum(adresse.value));

const tage = computed({
  get: () => tageUnerlaubteNutzung(adresse.value) ?? null,
  set: (value: number | null) => {
    // A period wins, so a directly entered count is kept but not overwritten while one is given.
    if (!tageAusZeitraum.value) {
      adresse.value.tageUnerlaubteNutzung = value;
    }
  },
});

const tageHint = computed(() =>
  tageAusZeitraum.value
    ? "Aus dem Zeitraum berechnet"
    : "Alternativ zum Zeitraum eintragbar"
);
</script>
