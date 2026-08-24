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
        v-if="isWriter"
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
import { useRoute } from "vue-router";

import useHasAnyRole from "@/composables/useHasAnyRole";
import { Role } from "@/types/Role";

definePage({
  meta: {
    hasAnyRole: [Role.READER, Role.WRITER],
  },
});

const route = useRoute("/projekte/[projektId]/abrechnungen/");
const projektId = route.params.projektId;

const isWriter = useHasAnyRole(Role.WRITER);
</script>
