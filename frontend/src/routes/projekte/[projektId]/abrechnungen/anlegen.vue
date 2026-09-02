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
      Abrechnung anlegen
    </h1>

    <v-form
      ref="form"
      @submit.prevent="save"
    >
      <v-tabs
        v-model="tab"
        class="mb-4"
      >
        <v-tab :value="TABS.BASIS">Basisinformationen</v-tab>
        <v-tab :value="TABS.BERECHNUNG">Berechnung</v-tab>
      </v-tabs>

      <v-tabs-window v-model="tab">
        <!-- eager, because the rules of an unmounted input never run -->
        <v-tabs-window-item
          eager
          :value="TABS.BASIS"
        >
          <abrechnung-basisinformationen v-model="abrechnung" />
        </v-tabs-window-item>

        <v-tabs-window-item
          eager
          :value="TABS.BERECHNUNG"
        >
          <abrechnung-berechnung v-model="abrechnung" />
        </v-tabs-window-item>
      </v-tabs-window>

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
import { mdiArrowLeft } from "@mdi/js";
import { ref, useTemplateRef } from "vue";
import { useRoute, useRouter } from "vue-router";

import { ApiFactory } from "@/api/ApiFactory";
import {
  AbrechnungControllerApi,
  ResponseError,
} from "@/api/generated/sonar-backend";
import AbrechnungBasisinformationen from "@/components/AbrechnungBasisinformationen.vue";
import AbrechnungBerechnung from "@/components/AbrechnungBerechnung.vue";
import YesNoDialog from "@/components/common/YesNoDialog.vue";
import { useAbrechnungForm } from "@/composables/abrechnungForm";
import { useSaveLeave } from "@/composables/saveLeave";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import { toAbrechnungRequestDTO } from "@/util/abrechnungMapper";
import { tabOfFirstError, TABS } from "@/util/abrechnungTabs";

const route = useRoute("/projekte/[projektId]/abrechnungen/anlegen");
const projektId = route.params.projektId;
const router = useRouter();
const snackbarStore = useSnackbarStore();

const form = useTemplateRef("form");
const tab = ref<string>(TABS.BASIS);
const saving = ref(false);

const { abrechnung, isDirty } = useAbrechnungForm();

const {
  cancel,
  isSave,
  leave,
  saveLeaveDialog,
  saveLeaveDialogText,
  saveLeaveDialogTitle,
} = useSaveLeave(isDirty);

function errorText(error: unknown): string {
  if (error instanceof ResponseError) {
    if (error.response.status === 400) {
      return "Die Abrechnung konnte nicht gespeichert werden. Bitte prüfen Sie Ihre Eingaben.";
    }
    if (error.response.status === 404) {
      return "Das Projekt wurde nicht gefunden.";
    }
  }
  return "Die Abrechnung konnte nicht gespeichert werden.";
}

async function save(): Promise<void> {
  const validation = await form.value?.validate();
  if (!validation?.valid) {
    const offendingTab = tabOfFirstError(validation?.errors ?? []);
    if (offendingTab) {
      tab.value = offendingTab;
    }
    return;
  }

  saving.value = true;
  try {
    const created = await ApiFactory.getInstance(
      AbrechnungControllerApi
    ).saveAbrechnung(projektId, toAbrechnungRequestDTO(abrechnung.value));
    snackbarStore.push({
      text: `Die Abrechnung ${created.id} wurde angelegt.`,
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
</script>
