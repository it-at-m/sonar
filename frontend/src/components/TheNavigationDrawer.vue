<template>
  <v-navigation-drawer
    v-model="showDrawer"
    temporary
  >
    <v-list>
      <v-list-item to="/projekte">
        <v-list-item-title> Projekte </v-list-item-title>
      </v-list-item>
      <v-list-item to="/">
        <v-list-item-title> Get Started </v-list-item-title>
      </v-list-item>
      <v-list-item
        v-if="showGetStarted"
        to="/get-started"
      >
        <v-list-item-title> Get Started </v-list-item-title>
      </v-list-item>
    </v-list>
    <template #append>
      <v-divider />
      <v-list>
        <v-list-item
          :prepend-icon="mdiPaletteOutline"
          title="Darstellung"
          @click="openAppearance"
        />
      </v-list>
    </template>
  </v-navigation-drawer>
  <appearance-dialog v-model="showAppearance" />
</template>

<script setup lang="ts">
import { mdiPaletteOutline } from "@mdi/js";
import { ref } from "vue";

import AppearanceDialog from "@/components/AppearanceDialog.vue";
import useHasAnyRole from "@/composables/useHasAnyRole";
import { Role } from "@/types/Role";

const showDrawer = defineModel<boolean>({ required: true });
const showGetStarted = useHasAnyRole([Role.READER, Role.WRITER]);
const showAppearance = ref(false);

function openAppearance(): void {
  showDrawer.value = false;
  showAppearance.value = true;
}
</script>
