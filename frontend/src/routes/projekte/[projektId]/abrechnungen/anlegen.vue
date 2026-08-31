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
      <v-tabs-window-item :value="TABS.BASIS">
        <abrechnung-basisinformationen v-model="abrechnung" />
      </v-tabs-window-item>

      <v-tabs-window-item :value="TABS.BERECHNUNG" />
    </v-tabs-window>
  </v-container>
</template>

<script setup lang="ts">
import { mdiArrowLeft } from "@mdi/js";
import { ref } from "vue";
import { useRoute } from "vue-router";

import AbrechnungBasisinformationen from "@/components/AbrechnungBasisinformationen.vue";
import { useAbrechnungForm } from "@/composables/abrechnungForm";

const TABS = {
  BASIS: "basis",
  BERECHNUNG: "berechnung",
} as const;

const route = useRoute("/projekte/[projektId]/abrechnungen/anlegen");
const projektId = route.params.projektId;

const tab = ref<string>(TABS.BASIS);

const { abrechnung } = useAbrechnungForm();
</script>
