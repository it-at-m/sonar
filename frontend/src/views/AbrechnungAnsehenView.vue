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

    <template v-else>
      <!-- readonly on the form reaches every input, on the tabs it hides the buttons of the Berechnung. -->
      <v-form readonly>
        <abrechnung-tabs
          v-model="abrechnung"
          readonly
        />
      </v-form>

      <div class="d-flex justify-end mt-6">
        <v-btn
          id="berechnung-durchfuehren"
          color="primary"
          :loading="berechnungLaeuft"
          @click="berechnungDurchfuehren"
        >
          Berechnung durchführen
        </v-btn>
      </div>
    </template>
  </v-container>
</template>

<script setup lang="ts">
import type { AbrechnungResponseDTO } from "@/api/generated/sonar-backend";

import { mdiArrowLeft, mdiHistory } from "@mdi/js";
import { computed, ref, watch } from "vue";
import { useRouter } from "vue-router";

import { ApiFactory } from "@/api/ApiFactory";
import {
  AbrechnungControllerApi,
  BerechnungControllerApi,
  ProjektControllerApi,
} from "@/api/generated/sonar-backend";
import AbrechnungTabs from "@/components/AbrechnungTabs.vue";
import { useAbrechnungForm } from "@/composables/abrechnungForm";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import { toAbrechnungForm } from "@/util/abrechnungMapper";
import { toBerechnungRequestDTO } from "@/util/berechnungMapper";

const { abrechnungId, projektId } = defineProps<{
  projektId: string;
  abrechnungId: string;
}>();

const router = useRouter();
const snackbarStore = useSnackbarStore();

const angezeigteAbrechnung = ref<AbrechnungResponseDTO>();
const berechnungLaeuft = ref(false);

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

/**
 * The Projektnummer belongs to the Projekt, not to the Abrechnung. It is fetched on the click, so
 * that a view which only shows the Abrechnung does not load the Projekt on every visit.
 */
async function berechnungDurchfuehren(): Promise<void> {
  const geladeneAbrechnung = angezeigteAbrechnung.value;
  if (geladeneAbrechnung === undefined) {
    return;
  }

  berechnungLaeuft.value = true;
  try {
    const projekt =
      await ApiFactory.getInstance(ProjektControllerApi).getProjekt(projektId);
    await ApiFactory.getInstance(
      BerechnungControllerApi
    ).berechnungDurchfuehren(
      projektId,
      abrechnungId,
      toBerechnungRequestDTO(geladeneAbrechnung, projekt)
    );
    snackbarStore.push({
      text: "Die Berechnung wurde durchgeführt.",
      color: STATUS_INDICATORS.SUCCESS,
    });
  } catch {
    snackbarStore.push({
      text: "Die Berechnung konnte nicht durchgeführt werden.",
      color: STATUS_INDICATORS.ERROR,
    });
  } finally {
    berechnungLaeuft.value = false;
  }
}

watch(
  () => abrechnungId,
  (id) => void loadAbrechnung(id),
  { immediate: true }
);
</script>
