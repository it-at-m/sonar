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
      v-if="loadingVorgaenger"
      color="primary"
      indeterminate
    />

    <v-form
      v-else
      ref="form"
      @submit.prevent="save"
    >
      <abrechnung-tabs
        ref="tabs"
        v-model="abrechnung"
        :invalid-nutzungsobjekte="invalidNutzungsobjekte"
        :suggestions="suggestions"
      />

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
          Speichern
        </v-btn>
      </div>
    </v-form>

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
import type { AbrechnungResponseDTO } from "@/api/generated/sonar-backend";
import type { ProjektAdresseSuggestion } from "@/types/ProjektAdresseSuggestion";

import { mdiArrowLeft } from "@mdi/js";
import { computed, nextTick, onMounted, ref, useTemplateRef } from "vue";
import { useRouter } from "vue-router";

import { ApiFactory } from "@/api/ApiFactory";
import {
  AbrechnungControllerApi,
  ResponseError,
} from "@/api/generated/sonar-backend";
import AbrechnungTabs from "@/components/AbrechnungTabs.vue";
import YesNoDialog from "@/components/common/YesNoDialog.vue";
import { useAbrechnungForm } from "@/composables/abrechnungForm";
import { useSaveLeave } from "@/composables/saveLeave";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import {
  toAbrechnungForm,
  toAbrechnungRequestDTO,
} from "@/util/abrechnungMapper";
import { nutzungsobjektOfError } from "@/util/abrechnungTabs";
import { fetchProjektAdresseSuggestions } from "@/util/projektAdresseSuggestion";

const { projektId, vorgaengerAbrechnungId = undefined } = defineProps<{
  projektId: string;
  vorgaengerAbrechnungId?: string;
}>();

const router = useRouter();
const snackbarStore = useSnackbarStore();

const form = useTemplateRef("form");
const tabs = useTemplateRef("tabs");
const saving = ref(false);
const suggestions = ref<ProjektAdresseSuggestion[]>([]);
const loadingVorgaenger = ref(vorgaengerAbrechnungId !== undefined);

const { abrechnung, isDirty, uebernehmen } = useAbrechnungForm();

const {
  cancel,
  isSave,
  leave,
  saveLeaveDialog,
  saveLeaveDialogText,
  saveLeaveDialogTitle,
} = useSaveLeave(isDirty);

const title = computed(() =>
  vorgaengerAbrechnungId === undefined
    ? "Abrechnung anlegen"
    : "Neue Version der Abrechnung anlegen"
);

const invalidNutzungsobjekte = computed(() =>
  (form.value?.errors ?? [])
    .map((error) => nutzungsobjektOfError(String(error.id)))
    .filter((index): index is number => index !== undefined)
);

function errorText(error: unknown): string {
  const isNewVersion = vorgaengerAbrechnungId !== undefined;
  if (error instanceof ResponseError) {
    if (error.response.status === 400) {
      return isNewVersion
        ? "Die neue Version konnte nicht gespeichert werden. Bitte prüfen Sie Ihre Eingaben."
        : "Die Abrechnung konnte nicht gespeichert werden. Bitte prüfen Sie Ihre Eingaben.";
    }
    if (error.response.status === 404) {
      return isNewVersion
        ? "Die Abrechnung, zu der die neue Version gehört, wurde nicht gefunden."
        : "Das Projekt wurde nicht gefunden.";
    }
    if (error.response.status === 409) {
      return "Zu dieser Abrechnung besteht bereits eine neuere Version.";
    }
  }
  return isNewVersion
    ? "Die neue Version konnte nicht gespeichert werden."
    : "Die Abrechnung konnte nicht gespeichert werden.";
}

function savedText(gespeicherteAbrechnung: AbrechnungResponseDTO): string {
  return vorgaengerAbrechnungId === undefined
    ? `Die Abrechnung ${gespeicherteAbrechnung.id} wurde angelegt.`
    : `Die Version ${gespeicherteAbrechnung.versionsnummer} der Abrechnung wurde angelegt.`;
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

async function save(): Promise<void> {
  const validation = await form.value?.validate();
  if (!validation?.valid) {
    await showFirstError(validation?.errors ?? []);
    return;
  }

  saving.value = true;
  try {
    const abrechnungApi = ApiFactory.getInstance(AbrechnungControllerApi);
    const requestDTO = toAbrechnungRequestDTO(abrechnung.value);
    const saved =
      vorgaengerAbrechnungId === undefined
        ? await abrechnungApi.saveAbrechnung(projektId, requestDTO)
        : await abrechnungApi.saveAbrechnungVersion(
            projektId,
            vorgaengerAbrechnungId,
            requestDTO
          );
    snackbarStore.push({
      text: savedText(saved),
      color: STATUS_INDICATORS.SUCCESS,
    });
    isSave.value = true;
    await router.push(`/projekte/${projektId}/abrechnungen`);
  } catch (error) {
    snackbarStore.push({
      text: errorText(error),
      color: STATUS_INDICATORS.ERROR,
    });
  } finally {
    saving.value = false;
  }
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

async function loadVorgaenger(abrechnungId: string): Promise<void> {
  try {
    const vorgaenger = await ApiFactory.getInstance(
      AbrechnungControllerApi
    ).getAbrechnung(projektId, abrechnungId);
    uebernehmen(toAbrechnungForm(vorgaenger));
    loadingVorgaenger.value = false;
  } catch {
    snackbarStore.push({
      text: "Die Abrechnung konnte nicht geladen werden. Es lässt sich daher keine neue Version anlegen.",
      color: STATUS_INDICATORS.ERROR,
    });
    await router.push(`/projekte/${projektId}/abrechnungen`);
  }
}

onMounted(() => {
  void loadProjektAdressen();
  if (vorgaengerAbrechnungId !== undefined) {
    void loadVorgaenger(vorgaengerAbrechnungId);
  }
});
</script>
