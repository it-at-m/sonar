<template>
  <v-card
    class="mb-4"
    variant="outlined"
  >
    <v-card-title class="d-flex align-center">
      <span class="text-title-medium">{{ label }}</span>
      <v-spacer />
      <v-btn
        :aria-label="`${label} entfernen`"
        :disabled="!removable"
        :icon="mdiDelete"
        variant="text"
        @click="emit('remove')"
      />
    </v-card-title>
    <v-card-text>
      <v-menu v-if="suggestions.length > 0">
        <template #activator="{ props: activatorProps }">
          <v-btn
            v-bind="activatorProps"
            class="mb-2"
            :prepend-icon="mdiContentCopy"
            variant="text"
          >
            Aus dem Projekt übernehmen
          </v-btn>
        </template>
        <v-list>
          <v-list-item
            v-for="suggestion in suggestions"
            :key="projektAdresseSuggestionTitle(suggestion)"
            :subtitle="projektAdresseSuggestionSubtitle(suggestion)"
            :title="projektAdresseSuggestionTitle(suggestion)"
            @click="applyProjektAdresseSuggestion(nutzungsobjekt, suggestion)"
          />
        </v-list>
      </v-menu>

      <adresse-fields
        :id-prefix="idPrefix"
        :model-value="nutzungsobjekt"
      />

      <h3 class="text-title-small mt-2 mb-2">Positionen</h3>

      <abrechnung-positionen-table
        v-model="nutzungsobjekt.positionen"
        :id-prefix="`${idPrefix}-position`"
      />

      <unerlaubte-nutzung-fields
        :id-prefix="idPrefix"
        :model-value="nutzungsobjekt"
      />

      <v-row>
        <v-col cols="12">
          <v-textarea
            :id="`${idPrefix}-bemerkung`"
            v-model="nutzungsobjekt.bemerkung"
            auto-grow
            counter
            label="Bemerkung"
            maxlength="10000"
            rows="2"
          />
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import type { AbrechnungNutzungsobjektForm } from "@/types/AbrechnungNutzungsobjektForm";
import type { ProjektAdresseSuggestion } from "@/types/ProjektAdresseSuggestion";

import { mdiContentCopy, mdiDelete } from "@mdi/js";

import AbrechnungPositionenTable from "@/components/AbrechnungPositionenTable.vue";
import AdresseFields from "@/components/common/AdresseFields.vue";
import UnerlaubteNutzungFields from "@/components/common/UnerlaubteNutzungFields.vue";
import {
  applyProjektAdresseSuggestion,
  projektAdresseSuggestionSubtitle,
  projektAdresseSuggestionTitle,
} from "@/util/projektAdresseSuggestion";

const nutzungsobjekt = defineModel<AbrechnungNutzungsobjektForm>({
  required: true,
});

defineProps<{
  idPrefix: string;
  label: string;
  removable: boolean;
  suggestions: ProjektAdresseSuggestion[];
}>();

const emit = defineEmits<{ remove: [] }>();
</script>
