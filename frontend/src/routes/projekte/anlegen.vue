<template>
  <v-container>
    <h1 class="text-display-medium font-weight-bold mb-6">Projekt anlegen</h1>

    <v-form
      ref="form"
      @submit.prevent="save"
    >
      <v-row>
        <v-col
          cols="12"
          md="4"
        >
          <v-text-field
            v-model="projektnummer"
            label="Projektnummer"
            maxlength="20"
            counter
            :rules="[requiredRule]"
          />
        </v-col>
        <v-col
          cols="12"
          md="4"
        >
          <v-text-field
            v-model="abrechnungBeginn"
            label="Abrechnung Beginn"
            type="date"
            :rules="[requiredRule]"
          />
        </v-col>
        <v-col
          cols="12"
          md="4"
        >
          <v-text-field
            v-model="abrechnungEnde"
            label="Abrechnung Ende"
            type="date"
            :rules="[requiredRule, abrechnungEndeRule]"
          />
        </v-col>
      </v-row>

      <h2 class="text-headline-small mt-4 mb-2">Adressen/Flurnummern</h2>

      <v-card
        v-for="(adresse, index) in adressen"
        :key="adresse.id"
        class="mb-4"
        variant="outlined"
      >
        <v-card-title class="d-flex align-center">
          <span class="text-title-medium">Adresse {{ index + 1 }}</span>
          <v-spacer />
          <v-btn
            :aria-label="`Adresse ${index + 1} entfernen`"
            :disabled="adressen.length === 1"
            :icon="mdiDelete"
            variant="text"
            @click="removeAdresse(index)"
          />
        </v-card-title>
        <v-card-text>
          <v-row>
            <v-col
              cols="12"
              md="6"
            >
              <v-text-field
                v-model="adresse.bezeichnung"
                label="Adresse, Hausnummer oder Flurstück"
                maxlength="255"
                :rules="[requiredRule]"
              />
            </v-col>
            <v-col
              cols="12"
              md="6"
            >
              <v-text-field
                v-model="adresse.baunutzung"
                label="Baunutzung"
                maxlength="255"
              />
            </v-col>
            <v-col
              cols="12"
              md="3"
            >
              <v-text-field
                v-model="adresse.unerlaubteNutzungVon"
                label="Unerlaubte Nutzung von"
                type="date"
                :rules="[unerlaubteNutzungVonRule(adresse)]"
              />
            </v-col>
            <v-col
              cols="12"
              md="3"
            >
              <v-text-field
                v-model="adresse.unerlaubteNutzungBis"
                label="Unerlaubte Nutzung bis"
                type="date"
                :rules="[unerlaubteNutzungBisRule(adresse)]"
              />
            </v-col>
            <v-col
              cols="12"
              md="6"
            >
              <v-number-input
                :model-value="tageUnerlaubteNutzung(adresse) ?? null"
                label="Tage unerlaubte Nutzung"
                :min="1"
                :precision="0"
                :disabled="hasZeitraum(adresse)"
                :hint="
                  hasZeitraum(adresse)
                    ? 'Aus dem Zeitraum berechnet'
                    : 'Alternativ zum Zeitraum eintragbar'
                "
                persistent-hint
                @update:model-value="setTageUnerlaubteNutzung(adresse, $event)"
              />
            </v-col>
            <v-col
              cols="12"
              md="3"
            >
              <v-number-input
                v-model="adresse.anzahlMahnungen"
                label="Anzahl Mahnungen"
                :min="0"
                :precision="0"
                :rules="[requiredRule]"
              />
            </v-col>
            <v-col
              cols="12"
              md="9"
            >
              <v-checkbox
                v-model="adresse.sondernutzungErlaubt"
                label="Sondernutzung erlaubt"
                hide-details
              />
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <v-btn
        :prepend-icon="mdiPlus"
        variant="text"
        @click="addAdresse"
      >
        Adresse hinzufügen
      </v-btn>

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
import type { AdresseForm } from "@/composables/projektForm";
import type { VForm } from "vuetify/components";

import { mdiDelete, mdiPlus } from "@mdi/js";
import { ref } from "vue";
import { useRouter } from "vue-router";

import { ApiFactory } from "@/api/ApiFactory";
import {
  ProjektControllerApi,
  ResponseError,
} from "@/api/generated/sonar-backend";
import YesNoDialog from "@/components/common/YesNoDialog.vue";
import {
  hasZeitraum,
  requiredRule,
  tageUnerlaubteNutzung,
  useProjektForm,
} from "@/composables/projektForm";
import { useSaveLeave } from "@/composables/saveLeave";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";

const router = useRouter();
const snackbarStore = useSnackbarStore();

const form = ref<VForm>();
const saving = ref(false);

const {
  abrechnungBeginn,
  abrechnungEnde,
  abrechnungEndeRule,
  addAdresse,
  adressen,
  isDirty,
  projektnummer,
  removeAdresse,
  toRequestDTO,
  unerlaubteNutzungBisRule,
  unerlaubteNutzungVonRule,
} = useProjektForm();

const {
  cancel,
  isSave,
  leave,
  saveLeaveDialog,
  saveLeaveDialogText,
  saveLeaveDialogTitle,
} = useSaveLeave(isDirty);

/**
 * Keeps a directly entered day count. Ignored while a period is given, because then the value is
 * derived from it and the input is disabled.
 */
function setTageUnerlaubteNutzung(
  adresse: AdresseForm,
  value: number | null
): void {
  if (!hasZeitraum(adresse)) {
    adresse.tageUnerlaubteNutzung = value;
  }
}

function errorText(error: unknown): string {
  if (error instanceof ResponseError) {
    if (error.response.status === 400) {
      return "Das Projekt konnte nicht gespeichert werden. Bitte prüfen Sie Ihre Eingaben.";
    }
  }
  return "Das Projekt konnte nicht gespeichert werden.";
}

async function save(): Promise<void> {
  const validation = await form.value?.validate();
  if (!validation?.valid) {
    return;
  }

  saving.value = true;
  try {
    const projekt =
      await ApiFactory.getInstance(ProjektControllerApi).saveProjekt(
        toRequestDTO()
      );
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
</script>
