<template>
  <v-dialog
    v-model="open"
    scrollable
    width="1200"
  >
    <v-card>
      <v-card-title>Ergebnis der Berechnung</v-card-title>
      <v-card-subtitle>{{ ueberschrift }}</v-card-subtitle>
      <v-card-text>
        <abrechnung-berechnung-ergebnis
          :abrechnung="kontext.abrechnung"
          :ergebnis="ergebnis"
          :geschaeftspartner="kontext.geschaeftspartner"
          :projekt="kontext.projekt"
        />
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          variant="text"
          @click="open = false"
        >
          Schließen
        </v-btn>
        <v-btn
          v-if="speicherbar"
          id="berechnung-speichern"
          color="primary"
          :loading="speichert"
          @click="emit('speichern')"
        >
          Speichern
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import type { BerechnungResponseDTO } from "@/api/generated/sonar-backend";
import type { BerechnungsKontext } from "@/util/berechnungsKontext";

import { computed } from "vue";

import AbrechnungBerechnungErgebnis from "@/components/AbrechnungBerechnungErgebnis.vue";
import { toDateString } from "@/util/formatter";

const open = defineModel<boolean>({ required: true });

const { ergebnis } = defineProps<{
  ergebnis: BerechnungResponseDTO;
  kontext: BerechnungsKontext;
  speicherbar: boolean;
  speichert: boolean;
}>();

const emit = defineEmits<{ speichern: [] }>();

const ueberschrift = computed(
  () =>
    `Abrechnung ${ergebnis.lfdNr}, ` +
    `${toDateString(ergebnis.abrechnungszeitraumVon)} bis ` +
    `${toDateString(ergebnis.abrechnungszeitraumBis)}`
);
</script>
