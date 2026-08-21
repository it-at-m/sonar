<template>
  <v-container>
    <div class="d-flex align-center flex-wrap mb-6">
      <h1 class="text-display-medium font-weight-bold">Projekte</h1>
      <v-spacer />
      <v-btn
        v-if="isWriter"
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

import useHasAnyRole from "@/composables/useHasAnyRole";
import { Role } from "@/types/Role";

definePage({
  meta: {
    hasAnyRole: [Role.READER, Role.WRITER],
  },
});

const isWriter = useHasAnyRole(Role.WRITER);
</script>
