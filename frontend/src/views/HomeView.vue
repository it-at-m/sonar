<template>
  <v-container class="fill-height">
    <v-row class="text-center">
      <v-col cols="12">
        <v-img
          src="@/assets/logo.png"
          class="my-3"
          height="200"
        />
        <p v-if="isWriter">writer-Rolle vorhanden</p>
        <p v-else>writer-Rolle fehlt</p>
      </v-col>

      <v-col class="mb-4">
        <h1 class="text-display-medium font-weight-bold mb-3">
          Willkommen bei Sonar
        </h1>
        <p>
          Das API-Gateway ist:
          <span :class="apiGwStatus">{{ apiGwStatus }}</span>
        </p>
        <p>
          Das Backend ist:
          <span :class="backendStatus">{{ backendStatus }}</span>
        </p>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import type { HealthState } from "@/types/HealthState.ts";

import { onMounted, ref } from "vue";

import { ApiFactory } from "@/api/ApiFactory.ts";
import { ActuatorApi } from "@/api/generated/sonar-backend";
import { checkHealth } from "@/api/healthstate-client.ts";
import useHasAnyRole from "@/composables/useHasAnyRole.ts";
import { STATUS_INDICATORS } from "@/constants.ts";
import { useSnackbarStore } from "@/stores/snackbar.ts";
import { Role } from "@/types/Role.ts";

const isWriter = useHasAnyRole(Role.WRITER);

const snackbarStore = useSnackbarStore();
const apiGwStatus = ref("DOWN");
const backendStatus = ref("DOWN");

onMounted(async () => {
  try {
    const content = await checkHealth();
    apiGwStatus.value = content.status;
  } catch (error) {
    const err = error as Error;
    snackbarStore.push({
      text: err.message,
      color: STATUS_INDICATORS.ERROR,
    });
  }

  try {
    const content = await ApiFactory.getInstance(ActuatorApi).health();
    backendStatus.value = (content as HealthState).status;
  } catch (error) {
    const err = error as Error;
    snackbarStore.push({
      text: err.message,
      color: STATUS_INDICATORS.ERROR,
    });
  }
});
</script>

<style scoped>
.UP {
  color: limegreen;
}

.DOWN {
  color: lightcoral;
}
</style>
