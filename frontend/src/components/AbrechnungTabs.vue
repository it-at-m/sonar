<template>
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
      <abrechnung-berechnung
        ref="berechnung"
        v-model="abrechnung"
        :invalid-nutzungsobjekte="invalidNutzungsobjekte"
        :nutzungsobjekt-suggestions="nutzungsobjektSuggestions"
        :readonly="readonly"
        :suggestions="suggestions"
      />
    </v-tabs-window-item>
  </v-tabs-window>
</template>

<script setup lang="ts">
import type { AbrechnungForm } from "@/types/abrechnung/AbrechnungForm";
import type { NutzungsobjektSuggestion } from "@/types/abrechnung/NutzungsobjektSuggestion";
import type { ProjektAdresseSuggestion } from "@/types/projekt/ProjektAdresseSuggestion";

import { ref, useTemplateRef } from "vue";

import AbrechnungBasisinformationen from "@/components/AbrechnungBasisinformationen.vue";
import AbrechnungBerechnung from "@/components/AbrechnungBerechnung.vue";
import { tabOfError, TABS } from "@/util/abrechnung/abrechnungTabs";

const abrechnung = defineModel<AbrechnungForm>({ required: true });

const {
  invalidNutzungsobjekte = [],
  nutzungsobjektSuggestions = [],
  readonly = false,
  suggestions = [],
} = defineProps<{
  invalidNutzungsobjekte?: number[];
  nutzungsobjektSuggestions?: NutzungsobjektSuggestion[];
  readonly?: boolean;
  suggestions?: ProjektAdresseSuggestion[];
}>();

const berechnung = useTemplateRef("berechnung");
const tab = ref<string>(TABS.BASIS);

/** Selects the tab an offending input sits on, so that it can take the focus. */
function showError(errorId: string): void {
  const offendingTab = tabOfError(errorId);
  if (offendingTab !== undefined) {
    tab.value = offendingTab;
  }
  berechnung.value?.showError(errorId);
}

defineExpose({ showError });
</script>
