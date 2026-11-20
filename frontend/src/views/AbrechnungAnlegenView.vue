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

    <v-progress-circular
      v-if="loading"
      color="primary"
      indeterminate
    />

    <v-form
      v-else
      ref="form"
      @submit.prevent="save"
    >
      <div :inert="saving || undefined">
        <abrechnung-tabs
          ref="tabs"
          v-model="abrechnung"
          :invalid-nutzungsobjekte="invalidNutzungsobjekte"
          :nutzungsobjekt-suggestions="nutzungsobjektSuggestions"
          :suggestions="suggestions"
        />
      </div>

      <div class="d-flex justify-end mt-6">
        <v-btn
          class="mr-2"
          variant="text"
          @click="abbrechen"
        >
          Abbrechen
        </v-btn>
        <v-btn
          color="primary"
          :loading="saving"
          type="submit"
        >
          Entwurf speichern
        </v-btn>
      </div>
    </v-form>

    <berechnung-dialog
      v-if="entwurf?.id"
      v-model="berechnungDialogOpen"
      :abrechnung-id="entwurf.id"
      :projekt-id="projektId"
    >
      <template #ergebnis-actions="{ close }">
        <v-btn
          variant="text"
          @click="close"
        >
          Weiter bearbeiten
        </v-btn>
        <v-btn
          variant="text"
          @click="keepEntwurf"
        >
          Als Entwurf behalten
        </v-btn>
        <v-btn
          color="primary"
          :loading="finalizing"
          @click="finalize"
        >
          Abrechnung abschließen
        </v-btn>
      </template>
    </berechnung-dialog>

    <yes-no-dialog
      v-model="saveLeaveDialog"
      :dialogtitle="saveLeaveDialogTitle"
      :dialogtext="saveLeaveDialogText"
      @no="cancel"
      @yes="leave"
    />
  </v-container>
</template>

<script setup lang="ts">
import type {
  AbrechnungRequestDTO,
  AbrechnungResponseDTO,
} from "@/api/generated/sonar-backend";
import type { NutzungsobjektSuggestion } from "@/types/abrechnung/NutzungsobjektSuggestion";
import type { ProjektAdresseSuggestion } from "@/types/projekt/ProjektAdresseSuggestion";

import { mdiArrowLeft } from "@mdi/js";
import { computed, nextTick, onMounted, ref, useTemplateRef } from "vue";
import { useRouter } from "vue-router";

import { ApiFactory } from "@/api/ApiFactory";
import {
  AbrechnungControllerApi,
  ResponseError,
} from "@/api/generated/sonar-backend";
import AbrechnungTabs from "@/components/AbrechnungTabs.vue";
import BerechnungDialog from "@/components/berechnung/BerechnungDialog.vue";
import YesNoDialog from "@/components/common/YesNoDialog.vue";
import { useAbrechnungForm } from "@/composables/abrechnungForm";
import { useSaveLeave } from "@/composables/saveLeave";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import {
  rememberSavedIds,
  toAbrechnungForm,
  toAbrechnungRequestDTO,
} from "@/util/abrechnung/abrechnungMapper";
import { nutzungsobjektOfError } from "@/util/abrechnung/abrechnungTabs";
import { fetchNutzungsobjektSuggestions } from "@/util/abrechnung/nutzungsobjektSuggestion";
import { fetchProjektAdresseSuggestions } from "@/util/projekt/projektAdresseSuggestion";

const {
  projektId,
  vorgaengerAbrechnungId = undefined,
  entwurfId = undefined,
} = defineProps<{
  projektId: string;
  vorgaengerAbrechnungId?: string;
  entwurfId?: string;
}>();

const router = useRouter();
const snackbarStore = useSnackbarStore();

const form = useTemplateRef("form");
const tabs = useTemplateRef("tabs");
const saving = ref(false);
const finalizing = ref(false);
const berechnungDialogOpen = ref(false);
const entwurf = ref<AbrechnungResponseDTO>();
const suggestions = ref<ProjektAdresseSuggestion[]>([]);
const nutzungsobjektSuggestions = ref<NutzungsobjektSuggestion[]>([]);
const loading = ref(
  vorgaengerAbrechnungId !== undefined || entwurfId !== undefined
);

const { abrechnung, isDirty, markSaved, uebernehmen } = useAbrechnungForm();

const {
  cancel,
  isSave,
  leave,
  saveLeaveDialog,
  saveLeaveDialogText,
  saveLeaveDialogTitle,
} = useSaveLeave(isDirty);

const title = computed(() => {
  if (entwurfId !== undefined) {
    return "Entwurf der Abrechnung bearbeiten";
  }
  return vorgaengerAbrechnungId === undefined
    ? "Abrechnung anlegen"
    : "Neue Version der Abrechnung anlegen";
});

const isNewVersion = computed(
  () =>
    vorgaengerAbrechnungId !== undefined ||
    (entwurf.value?.versionsnummer ?? 1) > 1
);

const invalidNutzungsobjekte = computed(() =>
  (form.value?.errors ?? [])
    .map((error) => nutzungsobjektOfError(String(error.id)))
    .filter((index): index is number => index !== undefined)
);

function errorText(error: unknown): string {
  if (error instanceof ResponseError) {
    if (error.response.status === 400) {
      return isNewVersion.value
        ? "Die neue Version konnte nicht gespeichert werden. Bitte prüfen Sie Ihre Eingaben."
        : "Die Abrechnung konnte nicht gespeichert werden. Bitte prüfen Sie Ihre Eingaben.";
    }
    if (error.response.status === 404) {
      if (entwurf.value !== undefined) {
        return "Der Entwurf der Abrechnung wurde nicht gefunden.";
      }
      return isNewVersion.value
        ? "Die Abrechnung, zu der die neue Version gehört, wurde nicht gefunden."
        : "Das Projekt wurde nicht gefunden.";
    }
    if (error.response.status === 409) {
      return entwurf.value === undefined
        ? "Zu dieser Abrechnung besteht bereits eine neuere Version."
        : "Die Abrechnung ist bereits abgeschlossen und lässt sich nicht mehr ändern.";
    }
  }
  return isNewVersion.value
    ? "Die neue Version konnte nicht gespeichert werden."
    : "Die Abrechnung konnte nicht gespeichert werden.";
}

function finalizeErrorText(error: unknown): string {
  if (error instanceof ResponseError && error.response.status === 409) {
    return "Die Abrechnung ist bereits abgeschlossen.";
  }
  return "Die Abrechnung konnte nicht abgeschlossen werden. Sie bleibt als Entwurf erhalten.";
}

function finalizedText(
  abgeschlosseneAbrechnung: AbrechnungResponseDTO
): string {
  return isNewVersion.value
    ? `Die Version ${abgeschlosseneAbrechnung.versionsnummer} der Abrechnung wurde abgeschlossen.`
    : `Die Abrechnung ${abgeschlosseneAbrechnung.id} wurde abgeschlossen.`;
}

async function showFirstError(
  errors: { id: string | number }[]
): Promise<void> {
  const first = errors[0];
  if (first === undefined) {
    return;
  }
  const id = String(first.id);
  tabs.value?.showError(id);
  await nextTick();
  document.getElementById(id)?.focus();
}

function saveEntwurf(
  requestDTO: AbrechnungRequestDTO
): Promise<AbrechnungResponseDTO> {
  const abrechnungApi = ApiFactory.getInstance(AbrechnungControllerApi);
  if (entwurf.value?.id !== undefined) {
    return abrechnungApi.updateAbrechnung(
      projektId,
      entwurf.value.id,
      requestDTO
    );
  }
  return vorgaengerAbrechnungId === undefined
    ? abrechnungApi.saveAbrechnung(projektId, requestDTO)
    : abrechnungApi.saveAbrechnungVersion(
        projektId,
        vorgaengerAbrechnungId,
        requestDTO
      );
}

async function save(): Promise<void> {
  const validation = await form.value?.validate();
  if (!validation?.valid) {
    await showFirstError(validation?.errors ?? []);
    return;
  }

  saving.value = true;
  try {
    entwurf.value = await saveEntwurf(toAbrechnungRequestDTO(abrechnung.value));
    rememberSavedIds(abrechnung.value, entwurf.value);
    markSaved();
    berechnungDialogOpen.value = true;
  } catch (error) {
    snackbarStore.push({
      text: errorText(error),
      color: STATUS_INDICATORS.ERROR,
    });
  } finally {
    saving.value = false;
  }
}

async function finalize(): Promise<void> {
  if (entwurf.value?.id === undefined) {
    return;
  }

  finalizing.value = true;
  try {
    const abgeschlosseneAbrechnung = await ApiFactory.getInstance(
      AbrechnungControllerApi
    ).finalizeAbrechnung(projektId, entwurf.value.id);
    snackbarStore.push({
      text: finalizedText(abgeschlosseneAbrechnung),
      color: STATUS_INDICATORS.SUCCESS,
    });
    await returnToAbrechnungen();
  } catch (error) {
    snackbarStore.push({
      text: finalizeErrorText(error),
      color: STATUS_INDICATORS.ERROR,
    });
  } finally {
    finalizing.value = false;
  }
}

async function keepEntwurf(): Promise<void> {
  snackbarStore.push({
    text: "Die Abrechnung wurde als Entwurf gespeichert.",
    color: STATUS_INDICATORS.SUCCESS,
  });
  await returnToAbrechnungen();
}

async function returnToAbrechnungen(): Promise<void> {
  isSave.value = true;
  await router.push(`/projekte/${projektId}/abrechnungen`);
}

function abbrechen(): void {
  void router.push(`/projekte/${projektId}/abrechnungen`);
}

async function loadProjektAdressen(): Promise<void> {
  try {
    suggestions.value = await fetchProjektAdresseSuggestions(projektId);
  } catch {
    snackbarStore.push({
      text: "Die Adressen des Projekts konnten nicht geladen werden. Sie lassen sich daher nicht übernehmen.",
      color: STATUS_INDICATORS.WARNING,
    });
  }
}

async function loadNutzungsobjekte(): Promise<void> {
  try {
    nutzungsobjektSuggestions.value =
      await fetchNutzungsobjektSuggestions(projektId);
  } catch {
    snackbarStore.push({
      text: "Die Nutzungsobjekte des Projekts konnten nicht geladen werden. Sie lassen sich daher nicht übernehmen.",
      color: STATUS_INDICATORS.WARNING,
    });
  }
}

async function loadVorgaenger(abrechnungId: string): Promise<void> {
  try {
    const vorgaenger = await ApiFactory.getInstance(
      AbrechnungControllerApi
    ).getAbrechnung(projektId, abrechnungId);
    uebernehmen(toAbrechnungForm(vorgaenger));
    loading.value = false;
  } catch {
    snackbarStore.push({
      text: "Die Abrechnung konnte nicht geladen werden. Es lässt sich daher keine neue Version anlegen.",
      color: STATUS_INDICATORS.ERROR,
    });
    await router.push(`/projekte/${projektId}/abrechnungen`);
  }
}

async function loadEntwurf(abrechnungId: string): Promise<void> {
  try {
    const geladenerEntwurf = await ApiFactory.getInstance(
      AbrechnungControllerApi
    ).getAbrechnung(projektId, abrechnungId);
    if (!geladenerEntwurf.draft) {
      snackbarStore.push({
        text: "Die Abrechnung ist bereits abgeschlossen und lässt sich nicht mehr ändern.",
        color: STATUS_INDICATORS.ERROR,
      });
      await router.push(`/projekte/${projektId}/abrechnungen`);
      return;
    }
    const entwurfForm = toAbrechnungForm(geladenerEntwurf);
    rememberSavedIds(entwurfForm, geladenerEntwurf);
    uebernehmen(entwurfForm);
    entwurf.value = geladenerEntwurf;
    loading.value = false;
  } catch {
    snackbarStore.push({
      text: "Der Entwurf der Abrechnung konnte nicht geladen werden.",
      color: STATUS_INDICATORS.ERROR,
    });
    await router.push(`/projekte/${projektId}/abrechnungen`);
  }
}

onMounted(() => {
  void loadProjektAdressen();
  void loadNutzungsobjekte();
  if (vorgaengerAbrechnungId !== undefined) {
    void loadVorgaenger(vorgaengerAbrechnungId);
  }
  if (entwurfId !== undefined) {
    void loadEntwurf(entwurfId);
  }
});
</script>
