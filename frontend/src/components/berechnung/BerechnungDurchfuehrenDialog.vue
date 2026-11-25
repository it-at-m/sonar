<template>
  <slot
    name="activator"
    :open="openDialog"
  >
    <v-btn
      :aria-label="`Berechnung der Abrechnung ${geschaeftspartnerId} erstellen`"
      density="comfortable"
      :icon="mdiCalculator"
      variant="text"
      @click="openDialog"
    />
  </slot>

  <v-dialog
    v-model="berechnungDialog"
    :persistent="calculating"
    width="500"
  >
    <v-card>
      <v-card-title>Berechnung erstellen</v-card-title>
      <v-form
        ref="form"
        @submit.prevent="createBerechnung"
      >
        <v-card-text>
          <sonar-date-field
            id="berechnung-abrechnungsdatum"
            v-model="abrechnungsdatum"
            label="Abrechnungsdatum"
            :rules="[requiredRule]"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            :disabled="calculating"
            variant="text"
            @click="berechnungDialog = false"
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

  <berechnung-ergebnis-dialog
    v-if="ergebnis && kontext"
    v-model="ergebnisDialog"
    :ergebnis="ergebnis"
    :kontext="kontext"
  />
</template>

<script setup lang="ts">
import type { BerechnungResponseDTO } from "@/api/generated/sonar-backend";
import type { AbrechnungForm } from "@/types/abrechnung/AbrechnungForm";
import type { BerechnungsKontext } from "@/util/berechnung/berechnungsKontext";

import { mdiCalculator } from "@mdi/js";
import { ref, useTemplateRef } from "vue";

import { ApiFactory } from "@/api/ApiFactory";
import {
  BerechnungControllerApi,
  ResponseError,
} from "@/api/generated/sonar-backend";
import BerechnungErgebnisDialog from "@/components/berechnung/BerechnungErgebnisDialog.vue";
import SonarDateField from "@/components/common/SonarDateField.vue";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import { toAbrechnungRequestDTO } from "@/util/abrechnung/abrechnungMapper";
import {
  fetchBerechnungsKontext,
  fetchBerechnungsKontextOfForm,
} from "@/util/berechnung/berechnungsKontext";
import { toIsoDate } from "@/util/common/isoDate";
import { requiredRule } from "@/util/validationRules";

const {
  abrechnungId = undefined,
  geschaeftspartnerId = undefined,
  projektId,
  unsavedAbrechnung = undefined,
} = defineProps<{
  projektId: string;
  abrechnungId?: string;
  geschaeftspartnerId?: string;
  unsavedAbrechnung?: AbrechnungForm;
}>();

const snackbarStore = useSnackbarStore();

const form = useTemplateRef("form");
const berechnungDialog = ref(false);
const ergebnisDialog = ref(false);
const calculating = ref(false);
const abrechnungsdatum = ref(toIsoDate(new Date()));
const ergebnis = ref<BerechnungResponseDTO>();
const kontext = ref<BerechnungsKontext>();

function openDialog(): void {
  abrechnungsdatum.value = toIsoDate(new Date());
  berechnungDialog.value = true;
}

function errorText(error: unknown): string {
  const isUnsaved = unsavedAbrechnung !== undefined;
  if (error instanceof ResponseError) {
    if (error.response.status === 400) {
      return isUnsaved
        ? "Die Berechnung konnte nicht durchgeführt werden. Bitte prüfen Sie Ihre Eingaben."
        : "Das Abrechnungsdatum ist ungültig.";
    }
    if (error.response.status === 403) {
      return "Sie sind für den Stadtbezirk dieses Projekts nicht zuständig.";
    }
    if (error.response.status === 404) {
      return isUnsaved
        ? "Das Projekt oder ein übernommenes Nutzungsobjekt wurde nicht gefunden."
        : "Die Abrechnung wurde nicht gefunden.";
    }
  }
  return "Die Berechnung konnte nicht durchgeführt werden.";
}

async function createBerechnung(): Promise<void> {
  const validation = await form.value?.validate();
  if (!validation?.valid || calculating.value) {
    return;
  }

  calculating.value = true;
  try {
    const berechnungApi = ApiFactory.getInstance(BerechnungControllerApi);
    const berechnungsdatum = new Date(abrechnungsdatum.value);
    if (unsavedAbrechnung !== undefined) {
      const preview = await berechnungApi.previewBerechnungOfUnsavedAbrechnung(
        projektId,
        berechnungsdatum,
        toAbrechnungRequestDTO(unsavedAbrechnung)
      );
      kontext.value = await fetchBerechnungsKontextOfForm(
        projektId,
        unsavedAbrechnung
      );
      ergebnis.value = preview;
    } else if (abrechnungId !== undefined) {
      const preview = await berechnungApi.previewBerechnungOfSavedAbrechnung(
        projektId,
        abrechnungId,
        berechnungsdatum
      );
      kontext.value = await fetchBerechnungsKontext(projektId, abrechnungId);
      ergebnis.value = preview;
    } else {
      throw new Error("Es fehlt die Abrechnung, die berechnet werden soll.");
    }
    berechnungDialog.value = false;
    ergebnisDialog.value = true;
  } catch (error) {
    snackbarStore.push({
      text: errorText(error),
      color: STATUS_INDICATORS.ERROR,
    });
  } finally {
    calculating.value = false;
  }
}
</script>
