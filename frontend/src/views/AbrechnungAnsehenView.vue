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
      {{ title }}
    </h1>

    <v-alert
      v-if="angezeigteAbrechnung?.neuereVersionVorhanden"
      class="mb-4"
      text="Zu dieser Abrechnung besteht eine neuere Version."
      type="info"
      variant="tonal"
    />

    <v-btn
      v-if="angezeigteAbrechnung?.vorgaengerAbrechnungId"
      class="mb-4"
      :prepend-icon="mdiHistory"
      variant="text"
      :to="`/projekte/${projektId}/abrechnungen/${angezeigteAbrechnung?.vorgaengerAbrechnungId}/ansehen`"
    >
      Vorherige Version ansehen
    </v-btn>

    <v-progress-circular
      v-if="angezeigteAbrechnung === undefined"
      color="primary"
      indeterminate
    />

    <!-- readonly on the form reaches every input, on the tabs it hides the buttons of the Berechnung. -->
    <v-form
      v-else
      readonly
    >
      <abrechnung-tabs
        v-model="abrechnung"
        readonly
      />
    </v-form>
  </v-container>
</template>

<script setup lang="ts">
import type { AbrechnungResponseDTO } from "@/api/generated/sonar-backend";

import { mdiArrowLeft, mdiHistory } from "@mdi/js";
import { computed, ref, watch } from "vue";
import { useRouter } from "vue-router";

import { ApiFactory } from "@/api/ApiFactory";
import { AbrechnungControllerApi } from "@/api/generated/sonar-backend";
import AbrechnungTabs from "@/components/AbrechnungTabs.vue";
import { useAbrechnungForm } from "@/composables/abrechnungForm";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import { toAbrechnungForm } from "@/util/abrechnungMapper";

const { abrechnungId, projektId } = defineProps<{
  projektId: string;
  abrechnungId: string;
}>();

const router = useRouter();
const snackbarStore = useSnackbarStore();

const angezeigteAbrechnung = ref<AbrechnungResponseDTO>();

const { abrechnung, uebernehmen } = useAbrechnungForm();

const title = computed(() =>
  angezeigteAbrechnung.value === undefined
    ? "Abrechnung"
    : `Abrechnung, Version ${angezeigteAbrechnung.value.versionsnummer ?? 1}`
);

async function loadAbrechnung(id: string): Promise<void> {
  angezeigteAbrechnung.value = undefined;
  try {
    const geladeneAbrechnung = await ApiFactory.getInstance(
      AbrechnungControllerApi
    ).getAbrechnung(projektId, id);
    uebernehmen(toAbrechnungForm(geladeneAbrechnung));
    angezeigteAbrechnung.value = geladeneAbrechnung;
  } catch {
    snackbarStore.push({
      text: "Die Abrechnung konnte nicht geladen werden.",
      color: STATUS_INDICATORS.ERROR,
    });
    await router.push(`/projekte/${projektId}/abrechnungen`);
  }
}

watch(
  () => abrechnungId,
  (id) => void loadAbrechnung(id),
  { immediate: true }
);
</script>
