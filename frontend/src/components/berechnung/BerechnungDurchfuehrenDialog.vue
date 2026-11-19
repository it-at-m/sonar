<template>
  <v-btn
    :aria-label="`Berechnung der Abrechnung ${geschaeftspartnerId} erstellen`"
    density="comfortable"
    :icon="mdiCalculator"
    variant="text"
    @click="berechnungDialogOpen = true"
  />

  <berechnung-dialog
    v-model="berechnungDialogOpen"
    :abrechnung-id="abrechnungId"
    :projekt-id="projektId"
  />
</template>

<script setup lang="ts">
import { mdiCalculator } from "@mdi/js";
import { ref } from "vue";

import BerechnungDialog from "@/components/berechnung/BerechnungDialog.vue";

const { abrechnungId, projektId } = defineProps<{
  projektId: string;
  abrechnungId: string;
  geschaeftspartnerId: string;
}>();

const berechnungDialogOpen = ref(false);
</script>
