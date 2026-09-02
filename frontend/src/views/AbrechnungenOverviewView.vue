<template>
  <v-container>
    <v-btn
      class="mb-2"
      :prepend-icon="mdiArrowLeft"
      variant="text"
      to="/projekte"
    >
      Zurück zu den Projekten
    </v-btn>

    <div class="d-flex align-center flex-wrap mb-6">
      <h1 class="text-display-medium font-weight-bold">Abrechnungen</h1>
      <v-spacer />
      <v-btn
        color="primary"
        :prepend-icon="mdiPlus"
        :to="`/projekte/${projektId}/abrechnungen/anlegen`"
      >
        Abrechnung anlegen
      </v-btn>
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { mdiArrowLeft, mdiPlus } from "@mdi/js";

const { projektId } = defineProps<{ projektId: string }>();
</script>
