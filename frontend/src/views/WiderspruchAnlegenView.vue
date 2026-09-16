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
      Widerspruch anlegen
    </h1>

    <v-form
      ref="form"
      @submit.prevent="save"
    >
      <v-row>
        <v-col
          cols="12"
          md="3"
        >
          <v-text-field
            id="widerspruch-datum-eingang"
            v-model="widerspruch.datumEingang"
            label="Datum Eingang"
            :rules="[requiredRule]"
            type="date"
          />
        </v-col>
        <v-col
          cols="12"
          md="3"
        >
          <v-text-field
            id="widerspruch-datum-ruecknahme"
            v-model="widerspruch.datumRuecknahme"
            label="Datum Rücknahme"
            type="date"
          />
        </v-col>
        <v-col
          cols="12"
          md="3"
        >
          <v-text-field
            id="widerspruch-datum-vorlage-regierung"
            v-model="widerspruch.datumVorlageRegierung"
            label="Datum Vorlage Regierung"
            type="date"
          />
        </v-col>
        <v-col
          cols="12"
          md="3"
        >
          <v-text-field
            id="widerspruch-datum-ablehnung-regierung"
            v-model="widerspruch.datumAblehnungRegierung"
            label="Datum Ablehnung Regierung"
            type="date"
          />
        </v-col>
      </v-row>

      <v-row>
        <v-col
          cols="12"
          md="6"
        >
          <v-text-field
            id="widerspruch-entscheidung-durchfuehrung"
            v-model="widerspruch.entscheidungDurchfuehrung"
            label="Entscheidung Durchführung"
            :maxlength="ENTSCHEIDUNG_MAX_LENGTH"
          />
        </v-col>
        <v-col
          cols="12"
          md="3"
        >
          <v-radio-group
            id="widerspruch-soll-abgesetzt"
            v-model="widerspruch.sollAbgesetzt"
            inline
            label="SOLL abgesetzt"
          >
            <v-radio
              label="Ja"
              :value="true"
            />
            <v-radio
              label="Nein"
              :value="false"
            />
          </v-radio-group>
        </v-col>
        <v-col
          cols="12"
          md="3"
        >
          <v-radio-group
            id="widerspruch-neue-teilabrechnung-anlegen"
            v-model="widerspruch.neueTeilabrechnungAnlegen"
            inline
            label="Neue Teilabrechnung anlegen"
          >
            <v-radio
              label="Ja"
              :value="true"
            />
            <v-radio
              label="Nein"
              :value="false"
            />
          </v-radio-group>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12">
          <v-textarea
            id="widerspruch-bemerkung"
            v-model="widerspruch.bemerkung"
            auto-grow
            label="Bemerkung"
            :maxlength="BEMERKUNG_MAX_LENGTH"
            rows="3"
          />
        </v-col>
      </v-row>

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
import { nextTick, ref, useTemplateRef } from "vue";
import { useRouter } from "vue-router";

import { ApiFactory } from "@/api/ApiFactory";
import {
  ResponseError,
  WiderspruchControllerApi,
} from "@/api/generated/sonar-backend";
import YesNoDialog from "@/components/common/YesNoDialog.vue";
import { useSaveLeave } from "@/composables/saveLeave";
import { useWiderspruchForm } from "@/composables/widerspruchForm";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import { requiredRule } from "@/util/validationRules";
import { toWiderspruchRequestDTO } from "@/util/widerspruchMapper";

const ENTSCHEIDUNG_MAX_LENGTH = 255;
const BEMERKUNG_MAX_LENGTH = 10000;

const { abrechnungId, projektId } = defineProps<{
  projektId: string;
  abrechnungId: string;
}>();

const router = useRouter();
const snackbarStore = useSnackbarStore();

const form = useTemplateRef("form");
const saving = ref(false);

const { isDirty, widerspruch } = useWiderspruchForm();

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
      return "Der Widerspruch konnte nicht gespeichert werden. Bitte prüfen Sie Ihre Eingaben.";
    }
    if (error.response.status === 404) {
      return "Die Abrechnung wurde nicht gefunden.";
    }
    if (error.response.status === 409) {
      return "Für diese Abrechnung besteht bereits ein Widerspruch.";
    }
  }
  return "Der Widerspruch konnte nicht gespeichert werden.";
}

async function showFirstError(
  errors: { id: string | number }[]
): Promise<void> {
  const first = errors[0];
  if (first === undefined) {
    return;
  }
  await nextTick();
  document.getElementById(String(first.id))?.focus();
}

async function save(): Promise<void> {
  const validation = await form.value?.validate();
  if (!validation?.valid) {
    await showFirstError(validation?.errors ?? []);
    return;
  }

  saving.value = true;
  try {
    const created = await ApiFactory.getInstance(
      WiderspruchControllerApi
    ).saveWiderspruch(abrechnungId, toWiderspruchRequestDTO(widerspruch.value));
    snackbarStore.push({
      text: `Der Widerspruch ${created.id} wurde angelegt.`,
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
