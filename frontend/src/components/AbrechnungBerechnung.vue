<template>
  <v-row>
    <v-col
      cols="12"
      md="4"
    >
      <v-text-field
        id="berechnung-zeitraum-von"
        v-model="abrechnung.zeitraumVon"
        label="Zeitraum von"
        :rules="[requiredRule]"
        type="date"
      />
    </v-col>
    <v-col
      cols="12"
      md="4"
    >
      <v-text-field
        id="berechnung-zeitraum-bis"
        v-model="abrechnung.zeitraumBis"
        label="Zeitraum bis"
        :rules="[
          requiredRule,
          (bis: string) => endeNotBeforeBeginn(abrechnung.zeitraumVon, bis),
        ]"
        type="date"
      />
    </v-col>
    <v-col
      cols="12"
      md="4"
    >
      <v-select
        id="berechnung-abrechnungs-art"
        v-model="abrechnung.abrechnungsArt"
        :items="ABRECHNUNGS_ART_OPTIONS"
        label="Art der Abrechnung"
        :rules="[requiredRule]"
      />
    </v-col>
  </v-row>

  <h2 class="text-headline-small mt-4 mb-2">Adressen/Flurstücke</h2>

  <div class="d-flex align-center mb-4">
    <v-tabs
      v-model="activeNutzungsobjekt"
      show-arrows
    >
      <v-tab
        v-for="tab in nutzungsobjektTabs"
        :key="tab.nutzungsobjekt.id"
        :aria-label="tab.invalid ? `${tab.label} enthält Fehler` : undefined"
        :value="tab.nutzungsobjekt.id"
      >
        {{ tab.label }}
        <v-icon
          v-if="tab.invalid"
          class="ml-1"
          color="error"
          :icon="mdiAlertCircle"
          size="small"
          :title="`${tab.label} enthält Fehler`"
        />
      </v-tab>
    </v-tabs>
    <v-btn
      class="ml-2 flex-shrink-0"
      :prepend-icon="mdiPlus"
      variant="text"
      @click="addNutzungsobjekt"
    >
      Adresse/Flurstück hinzufügen
    </v-btn>
  </div>

  <v-tabs-window v-model="activeNutzungsobjekt">
    <!-- eager, because the rules of an unmounted input never run -->
    <v-tabs-window-item
      v-for="tab in nutzungsobjektTabs"
      :key="tab.nutzungsobjekt.id"
      eager
      :value="tab.nutzungsobjekt.id"
    >
      <abrechnung-nutzungsobjekt-panel
        :model-value="tab.nutzungsobjekt"
        :id-prefix="tab.idPrefix"
        :label="tab.label"
        :removable="nutzungsobjektTabs.length > 1"
        :suggestions="suggestions"
        @remove="removeNutzungsobjekt(tab.index)"
      />
    </v-tabs-window-item>
  </v-tabs-window>
</template>

<script setup lang="ts">
import type { AbrechnungForm } from "@/composables/abrechnungForm";
import type { ProjektAdresseSuggestion } from "@/types/ProjektAdresseSuggestion";

import { mdiAlertCircle, mdiPlus } from "@mdi/js";
import { computed, ref } from "vue";

import { AbrechnungRequestDTOAbrechnungsArtEnum } from "@/api/generated/sonar-backend";
import AbrechnungNutzungsobjektPanel from "@/components/AbrechnungNutzungsobjektPanel.vue";
import { createAbrechnungNutzungsobjekt } from "@/util/abrechnungNutzungsobjektForm";
import {
  nutzungsobjektIdPrefix,
  nutzungsobjektOfError,
} from "@/util/abrechnungTabs";
import { adresseLabel } from "@/util/adresseLabel";
import { endeNotBeforeBeginn, requiredRule } from "@/util/validationRules";

const ABRECHNUNGS_ART_OPTIONS = [
  {
    title: "Endabrechnung",
    value: AbrechnungRequestDTOAbrechnungsArtEnum.ENDABRECHNUNG,
  },
  {
    title: "Zwischenabrechnung",
    value: AbrechnungRequestDTOAbrechnungsArtEnum.ZWISCHENABRECHNUNG,
  },
];

const abrechnung = defineModel<AbrechnungForm>({ required: true });

const props = defineProps<{
  suggestions: ProjektAdresseSuggestion[];
  invalidNutzungsobjekte: number[];
}>();

const activeNutzungsobjekt = ref(abrechnung.value.nutzungsobjekte[0]?.id ?? "");

const nutzungsobjektTabs = computed(() =>
  abrechnung.value.nutzungsobjekte.map((nutzungsobjekt, index) => ({
    nutzungsobjekt,
    index,
    label: adresseLabel(nutzungsobjekt, index + 1),
    idPrefix: nutzungsobjektIdPrefix(index),
    invalid: props.invalidNutzungsobjekte.includes(index),
  }))
);

function addNutzungsobjekt(): void {
  const nutzungsobjekt = createAbrechnungNutzungsobjekt();
  abrechnung.value.nutzungsobjekte.push(nutzungsobjekt);
  activeNutzungsobjekt.value = nutzungsobjekt.id;
}

function removeNutzungsobjekt(index: number): void {
  const removed = abrechnung.value.nutzungsobjekte[index];
  abrechnung.value.nutzungsobjekte.splice(index, 1);
  if (removed?.id !== activeNutzungsobjekt.value) {
    return;
  }
  const next =
    abrechnung.value.nutzungsobjekte[
      Math.min(index, abrechnung.value.nutzungsobjekte.length - 1)
    ];
  activeNutzungsobjekt.value = next?.id ?? "";
}

/** Selects the tab of the Nutzungsobjekt an offending input belongs to, so it can take the focus. */
function showError(errorId: string): void {
  const index = nutzungsobjektOfError(errorId);
  if (index === undefined) {
    return;
  }
  const nutzungsobjekt = abrechnung.value.nutzungsobjekte[index];
  if (nutzungsobjekt !== undefined) {
    activeNutzungsobjekt.value = nutzungsobjekt.id;
  }
}

defineExpose({ showError });
</script>
