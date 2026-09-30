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

    <div class="d-flex align-center flex-wrap ga-4 mb-6">
      <h1 class="text-display-medium font-weight-bold">
        {{ title }}
      </h1>
      <v-spacer />
      <v-select
        v-if="versionen.length > 1"
        id="abrechnung-version"
        density="comfortable"
        hide-details
        :items="versionOptions"
        label="Angezeigte Version"
        max-width="260"
        :model-value="abrechnungId"
        @update:model-value="switchToVersion"
      />
    </div>

    <v-alert
      v-if="angezeigteAbrechnung?.neuereVersionVorhanden"
      class="mb-4"
      text="Zu dieser Abrechnung besteht eine neuere Version."
      type="info"
      variant="tonal"
    />

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
import type { AbrechnungVersion } from "@/types/abrechnung/AbrechnungVersion";

import { mdiArrowLeft } from "@mdi/js";
import { computed, ref, watch } from "vue";
import { useRouter } from "vue-router";

import { ApiFactory } from "@/api/ApiFactory";
import { AbrechnungControllerApi } from "@/api/generated/sonar-backend";
import AbrechnungTabs from "@/components/AbrechnungTabs.vue";
import { useAbrechnungForm } from "@/composables/abrechnungForm";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import { toAbrechnungForm } from "@/util/abrechnung/abrechnungMapper";
import {
  abrechnungVersionOptions,
  fetchAbrechnungVersionen,
} from "@/util/abrechnung/abrechnungVersionen";

const { abrechnungId, projektId } = defineProps<{
  projektId: string;
  abrechnungId: string;
}>();

const router = useRouter();
const snackbarStore = useSnackbarStore();

const angezeigteAbrechnung = ref<AbrechnungResponseDTO>();
const versionen = ref<AbrechnungVersion[]>([]);

const { abrechnung, uebernehmen } = useAbrechnungForm();

const title = computed(() =>
  angezeigteAbrechnung.value === undefined
    ? "Abrechnung"
    : `Abrechnung, Version ${angezeigteAbrechnung.value.versionsnummer ?? 1}`
);

const versionOptions = computed(() =>
  abrechnungVersionOptions(versionen.value)
);

function switchToVersion(id: string): void {
  void router.push(`/projekte/${projektId}/abrechnungen/${id}/ansehen`);
}

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

async function loadVersionen(id: string): Promise<void> {
  // Every member of a chain answers with the same list, so a switch inside it needs no new request.
  if (versionen.value.some((version) => version.id === id)) {
    return;
  }
  versionen.value = [];
  try {
    versionen.value = await fetchAbrechnungVersionen(projektId, id);
  } catch {
    snackbarStore.push({
      text: "Die Versionen der Abrechnung konnten nicht geladen werden. Ein Wechsel zwischen den Versionen ist daher nicht möglich.",
      color: STATUS_INDICATORS.WARNING,
    });
  }
}

watch(
  () => abrechnungId,
  (id) => {
    void loadAbrechnung(id);
    void loadVersionen(id);
  },
  { immediate: true }
);
</script>
