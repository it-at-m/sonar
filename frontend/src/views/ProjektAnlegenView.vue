<template>
  <v-container>
    <h1 class="text-display-medium font-weight-bold mb-6">Projekt anlegen</h1>

    <projekt-form
      ref="projektForm"
      :saving="saving"
      :stadtbezirk-options="stadtbezirkOptions"
      @cancel="abbrechen"
      @save="save"
    />

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
import type { ProjektRequestDTO } from "@/api/generated/sonar-backend";
import type { StadtbezirkOption } from "@/types/projekt/StadtbezirkOption";

import { onMounted, ref, useTemplateRef } from "vue";
import { useRouter } from "vue-router";

import { ApiFactory } from "@/api/ApiFactory.ts";
import {
  ProjektControllerApi,
  ResponseError,
} from "@/api/generated/sonar-backend";
import YesNoDialog from "@/components/common/YesNoDialog.vue";
import ProjektForm from "@/components/projekt/ProjektForm.vue";
import { useSaveLeave } from "@/composables/saveLeave.ts";
import { STATUS_INDICATORS } from "@/constants.ts";
import { useSnackbarStore } from "@/stores/snackbar.ts";
import { fetchStadtbezirkOptionsOfCurrentUser } from "@/util/projekt/stadtbezirk";

const router = useRouter();
const snackbarStore = useSnackbarStore();

const projektForm =
  useTemplateRef<InstanceType<typeof ProjektForm>>("projektForm");
const saving = ref(false);
const stadtbezirkOptions = ref<StadtbezirkOption[]>([]);

const {
  cancel,
  isSave,
  leave,
  saveLeaveDialog,
  saveLeaveDialogText,
  saveLeaveDialogTitle,
} = useSaveLeave(() => projektForm.value?.isDirty() ?? false);

function errorText(error: unknown): string {
  if (error instanceof ResponseError) {
    if (error.response.status === 400) {
      return "Das Projekt konnte nicht gespeichert werden. Bitte prüfen Sie Ihre Eingaben.";
    }
    if (error.response.status === 403) {
      return "Sie sind für den gewählten Stadtbezirk nicht zuständig.";
    }
  }
  return "Das Projekt konnte nicht gespeichert werden.";
}

async function loadStadtbezirkOptions(): Promise<void> {
  try {
    stadtbezirkOptions.value = await fetchStadtbezirkOptionsOfCurrentUser();
  } catch {
    snackbarStore.push({
      text: "Die Stadtbezirke konnten nicht geladen werden.",
      color: STATUS_INDICATORS.ERROR,
    });
  }
}

async function save(request: ProjektRequestDTO): Promise<void> {
  saving.value = true;
  try {
    const projekt =
      await ApiFactory.getInstance(ProjektControllerApi).saveProjekt(request);
    snackbarStore.push({
      text: `Das Projekt ${projekt.projektnummer} wurde angelegt.`,
      color: STATUS_INDICATORS.SUCCESS,
    });
    isSave.value = true;
    await router.push("/projekte");
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
  void router.push("/projekte");
}

onMounted(() => void loadStadtbezirkOptions());
</script>
