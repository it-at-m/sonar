<template>
  <v-dialog
    v-model="open"
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
            variant="text"
            @click="open = false"
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
  >
    <template
      v-if="$slots['ergebnis-actions']"
      #actions="{ close }"
    >
      <slot
        :close="close"
        name="ergebnis-actions"
      />
    </template>
  </berechnung-ergebnis-dialog>
</template>

<script setup lang="ts">
import type { BerechnungResponseDTO } from "@/api/generated/sonar-backend";
import type { BerechnungsKontext } from "@/util/berechnung/berechnungsKontext";

import { ref, useTemplateRef, watch } from "vue";

import { ApiFactory } from "@/api/ApiFactory";
import {
  BerechnungControllerApi,
  ResponseError,
} from "@/api/generated/sonar-backend";
import BerechnungErgebnisDialog from "@/components/berechnung/BerechnungErgebnisDialog.vue";
import SonarDateField from "@/components/common/SonarDateField.vue";
import { STATUS_INDICATORS } from "@/constants";
import { useSnackbarStore } from "@/stores/snackbar";
import { fetchBerechnungsKontext } from "@/util/berechnung/berechnungsKontext";
import { toIsoDate } from "@/util/common/isoDate";
import { requiredRule } from "@/util/validationRules";

const open = defineModel<boolean>({ required: true });

const { abrechnungId, projektId } = defineProps<{
  projektId: string;
  abrechnungId: string;
}>();

defineSlots<{
  "ergebnis-actions"?: (props: { close: () => void }) => unknown;
}>();

const snackbarStore = useSnackbarStore();

const form = useTemplateRef("form");
const ergebnisDialog = ref(false);
const calculating = ref(false);
const abrechnungsdatum = ref(toIsoDate(new Date()));
const ergebnis = ref<BerechnungResponseDTO>();
const kontext = ref<BerechnungsKontext>();

watch(open, (opened) => {
  if (opened) {
    abrechnungsdatum.value = toIsoDate(new Date());
  }
});

function errorText(error: unknown): string {
  if (error instanceof ResponseError) {
    if (error.response.status === 400) {
      return "Das Abrechnungsdatum ist ungültig.";
    }
    if (error.response.status === 404) {
      return "Die Abrechnung wurde nicht gefunden.";
    }
  }
  return "Die Berechnung konnte nicht durchgeführt werden.";
}

async function createBerechnung(): Promise<void> {
  const validation = await form.value?.validate();
  if (!validation?.valid) {
    return;
  }

  calculating.value = true;
  try {
    const preview = await ApiFactory.getInstance(
      BerechnungControllerApi
    ).previewBerechnung(
      projektId,
      abrechnungId,
      new Date(abrechnungsdatum.value)
    );
    kontext.value = await fetchBerechnungsKontext(projektId, abrechnungId);
    ergebnis.value = preview;
    open.value = false;
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
