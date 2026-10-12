<template>
  <v-btn
    :aria-label="`Berechnung der Abrechnung ${geschaeftspartnerId} erstellen`"
    density="comfortable"
    :icon="mdiCalculator"
    variant="text"
    @click="oeffne"
  />

  <v-dialog
    v-model="eingabeDialog"
    width="500"
  >
    <v-card>
      <v-card-title>Berechnung erstellen</v-card-title>
      <v-form
        ref="form"
        @submit.prevent="berechnen"
      >
        <v-card-text>
          <v-text-field
            id="berechnung-abrechnungsdatum"
            v-model="abrechnungsdatum"
            label="Abrechnungsdatum"
            :rules="[requiredRule]"
            type="date"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            variant="text"
            @click="eingabeDialog = false"
          >
            Abbrechen
          </v-btn>
          <v-btn
            color="primary"
            type="submit"
          >
            Berechnen
          </v-btn>
        </v-card-actions>
      </v-form>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { mdiCalculator } from "@mdi/js";
import { ref, useTemplateRef } from "vue";

import { requiredRule } from "@/util/validationRules";

defineProps<{
  projektId: string;
  abrechnungId: string;
  geschaeftspartnerId: string;
}>();

const form = useTemplateRef("form");
const eingabeDialog = ref(false);
const abrechnungsdatum = ref(heute());

function heute(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${now.getFullYear()}-${month}-${day}`;
}

function oeffne(): void {
  abrechnungsdatum.value = heute();
  eingabeDialog.value = true;
}

async function berechnen(): Promise<void> {
  const validation = await form.value?.validate();
  if (!validation?.valid) {
    return;
  }
  eingabeDialog.value = false;
  // The Berechnung itself is not requested yet.
}
</script>
