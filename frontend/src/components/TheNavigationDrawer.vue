<template>
  <v-navigation-drawer
    v-model="showDrawer"
    temporary
  >
    <v-list>
      <v-list-item to="/">
        <v-list-item-title>
          Get Started
        </v-list-item-title>
      </v-list-item>
      <v-list-item
        v-if="showGetStarted"
        to="/get-started"
      >
        <v-list-item-title>
          Get Started
        </v-list-item-title>
      </v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>

<script setup lang="ts">

import useHasAnyRole from "@/composables/useHasAnyRole";
import { Role } from "@/types/Role";

const showDrawer = defineModel<boolean>({ required: true });
const showGetStarted = useHasAnyRole([Role.READER, Role.WRITER]);

</script>
