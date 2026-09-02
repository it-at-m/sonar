<template>
  <v-container>
    <v-btn
      class="mb-2"
      :prepend-icon="mdiArrowLeft"
      variant="text"
      :to="`/projekte/${projektId}/abrechnungen`"
    >
      Zurück zu den Abrechnungen
    </v-btn>

    <h1 class="text-display-medium font-weight-bold mb-6">
      Abrechnung anlegen
    </h1>

    <v-tabs
      v-model="tab"
      class="mb-4"
    >
      <v-tab :value="TABS.BASIS">Basisinformationen</v-tab>
      <v-tab :value="TABS.BERECHNUNG">Berechnung</v-tab>
    </v-tabs>

    <v-tabs-window v-model="tab">
      <v-tabs-window-item :value="TABS.BASIS" />
      <v-tabs-window-item :value="TABS.BERECHNUNG" />
    </v-tabs-window>
  </v-container>
</template>

<script setup lang="ts">
import { mdiArrowLeft } from "@mdi/js";
import { ref } from "vue";

const TABS = {
  BASIS: "basis",
  BERECHNUNG: "berechnung",
} as const;

const { projektId } = defineProps<{ projektId: string }>();

const tab = ref<string>(TABS.BASIS);
</script>
