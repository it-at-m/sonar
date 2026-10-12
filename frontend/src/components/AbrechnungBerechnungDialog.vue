<template>
  <v-btn
    :aria-label="`Berechnung der Abrechnung ${geschaeftspartnerId} ansehen`"
    density="comfortable"
    :icon="mdiCalculator"
    :loading="loadingBerechnung"
    variant="text"
    @click="open"
  />

  <v-dialog
    v-model="inputDialog"
    width="500"
  >
    <v-card>
      <v-card-title>Berechnung erstellen</v-card-title>
      <v-form
        ref="form"
        @submit.prevent="calculate"
      >
        <v-card-text>
          <v-text-field
            id="berechnung-abrechnungsdatum"
            v-model="abrechnungsdatum"
            label="Abrechnungsdatum"
            :rules="[requiredRule]"
            type="date"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            variant="text"
            @click="inputDialog = false"
          >
            Abbrechen
          </v-btn>
          <v-btn
            color="primary"
            :loading="calculating"
            type="submit"
          >
            Berechnen
          </v-btn>
        </v-card-actions>
      </v-form>
    </v-card>
  </v-dialog>

  <abrechnung-berechnung-ergebnis-dialog
    v-if="ergebnis && kontext"
    v-model="ergebnisDialog"
    :ergebnis="ergebnis"
    :kontext="kontext"
    :speicherbar="!stored"
    :speichert="saving"
    @speichern="save"
  />
</template>

<script setup lang="ts">
import type { BerechnungResponseDTO } from "@/api/generated/sonar-backend";
import type { BerechnungsKontext } from "@/util/berechnungsKontext";

import { mdiCalculator } from "@mdi/js";
import { ref, useTemplateRef } from "vue";

import { ApiFactory } from "@/api/ApiFactory";
import {
  BerechnungControllerApi,
  ResponseError,
} from "@/api/generated/sonar-backend";
import AbrechnungBerechnungErgebnisDialog from "@/components/AbrechnungBerechnungErgebnisDialog.vue";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import { fetchBerechnungsKontext } from "@/util/berechnungsKontext";
import { requiredRule } from "@/util/validationRules";

const { abrechnungId, projektId } = defineProps<{
  projektId: string;
  abrechnungId: string;
  geschaeftspartnerId: string;
}>();

const snackbarStore = useSnackbarStore();

const form = useTemplateRef("form");
const inputDialog = ref(false);
const ergebnisDialog = ref(false);
const loadingBerechnung = ref(false);
const calculating = ref(false);
const saving = ref(false);
const abrechnungsdatum = ref(today());
const ergebnis = ref<BerechnungResponseDTO>();
const stored = ref(false);
const kontext = ref<BerechnungsKontext>();

/**
 * Built from the local parts, because toISOString reads the date in UTC and would name the day
 * before whenever it is still before 02:00 in Munich.
 */
function today(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${now.getFullYear()}-${month}-${day}`;
}

function errorText(error: unknown, fallback: string): string {
  if (error instanceof ResponseError) {
    if (error.response.status === 400) {
      return "Das Abrechnungsdatum ist ungültig.";
    }
    if (error.response.status === 404) {
      return "Die Abrechnung wurde nicht gefunden.";
    }
    if (error.response.status === 409) {
      return "Für diese Abrechnung besteht bereits eine Berechnung.";
    }
  }
  return fallback;
}

function showError(error: unknown, fallback: string): void {
  snackbarStore.push({
    text: errorText(error, fallback),
    color: STATUS_INDICATORS.ERROR,
  });
}

/**
 * An Abrechnung is calculated once. The stored Berechnung is therefore shown as it is, and only an
 * Abrechnung without one is calculated anew.
 */
async function open(): Promise<void> {
  loadingBerechnung.value = true;
  try {
    const storedBerechnung = await loadBerechnung();
    if (storedBerechnung) {
      await loadKontext();
      ergebnis.value = storedBerechnung;
      stored.value = true;
      ergebnisDialog.value = true;
    } else {
      openInputDialog();
    }
  } catch (error) {
    showError(error, "Die Berechnung konnte nicht geladen werden.");
  } finally {
    loadingBerechnung.value = false;
  }
}

/** The backend answers an Abrechnung that was never calculated with a 404. */
async function loadBerechnung(): Promise<BerechnungResponseDTO | undefined> {
  try {
    return await ApiFactory.getInstance(BerechnungControllerApi).getBerechnung(
      projektId,
      abrechnungId
    );
  } catch (error) {
    if (error instanceof ResponseError && error.response.status === 404) {
      return undefined;
    }
    throw error;
  }
}

async function loadKontext(): Promise<void> {
  kontext.value ??= await fetchBerechnungsKontext(projektId, abrechnungId);
}

function openInputDialog(): void {
  abrechnungsdatum.value = today();
  inputDialog.value = true;
}

async function calculate(): Promise<void> {
  const validation = await form.value?.validate();
  if (!validation?.valid) {
    return;
  }

  calculating.value = true;
  try {
    // A date only string is parsed as UTC midnight, and the generated client cuts the day out of
    // the UTC representation again, so the day arrives at the backend the way it was entered.
    const preview = await ApiFactory.getInstance(
      BerechnungControllerApi
    ).previewBerechnung(
      projektId,
      abrechnungId,
      new Date(abrechnungsdatum.value)
    );
    await loadKontext();
    ergebnis.value = preview;
    stored.value = false;
    inputDialog.value = false;
    ergebnisDialog.value = true;
  } catch (error) {
    showError(error, "Die Berechnung konnte nicht durchgeführt werden.");
  } finally {
    calculating.value = false;
  }
}

async function save(): Promise<void> {
  saving.value = true;
  try {
    ergebnis.value = await ApiFactory.getInstance(
      BerechnungControllerApi
    ).calculateAndStoreBerechnung(
      projektId,
      abrechnungId,
      new Date(abrechnungsdatum.value)
    );
    stored.value = true;
    snackbarStore.push({
      text: "Die Berechnung wurde gespeichert.",
      color: STATUS_INDICATORS.SUCCESS,
    });
    ergebnisDialog.value = false;
  } catch (error) {
    showError(error, "Die Berechnung konnte nicht gespeichert werden.");
  } finally {
    saving.value = false;
  }
}
</script>
