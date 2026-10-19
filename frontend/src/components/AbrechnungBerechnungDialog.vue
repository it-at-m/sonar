<template>
  <v-btn
    :aria-label="`Berechnung der Abrechnung ${geschaeftspartnerId} erstellen`"
    density="comfortable"
    :icon="mdiCalculator"
    variant="text"
    @click="openDialog"
  />

  <v-dialog
    v-model="berechnungDialog"
    width="500"
  >
    <v-card>
      <v-card-title>Berechnung erstellen</v-card-title>
      <v-form
        ref="form"
        @submit.prevent="createBerechnung"
      >
        <v-card-text>
          <date-field
            id="berechnung-abrechnungsdatum"
            v-model="abrechnungsdatum"
            label="Abrechnungsdatum"
            :rules="[requiredRule]"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            variant="text"
            @click="berechnungDialog = false"
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

import DateField from "@/components/common/DateField.vue";
import { toIsoDate } from "@/util/common/isoDate";
import { requiredRule } from "@/util/validationRules";

defineProps<{
  projektId: string;
  abrechnungId: string;
  geschaeftspartnerId: string;
}>();

const form = useTemplateRef("form");
const berechnungDialog = ref(false);
const abrechnungsdatum = ref(toIsoDate(new Date()));

function openDialog(): void {
  abrechnungsdatum.value = toIsoDate(new Date());
  berechnungDialog.value = true;
}

async function createBerechnung(): Promise<void> {
  const validation = await form.value?.validate();
  if (!validation?.valid) {
    return;
  }
  berechnungDialog.value = false;
  // The Berechnung itself is not requested yet.
}
</script>
