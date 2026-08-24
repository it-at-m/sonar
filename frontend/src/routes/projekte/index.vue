<template>
  <v-container>
    <div class="d-flex align-center flex-wrap mb-6">
      <h1 class="text-display-medium font-weight-bold">Projekte</h1>
      <v-spacer />
      <v-btn
        color="primary"
        :prepend-icon="mdiPlus"
        to="/projekte/anlegen"
      >
        Projekt anlegen
      </v-btn>
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { mdiPlus } from "@mdi/js";
</script>
