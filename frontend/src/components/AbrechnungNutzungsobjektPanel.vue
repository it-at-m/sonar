<template>
  <v-card
    class="mb-4"
    variant="outlined"
  >
    <v-card-title class="d-flex align-center">
      <span class="text-title-medium">{{ label }}</span>
      <v-spacer />
      <v-btn
        v-if="!readonly"
        :aria-label="`${label} entfernen`"
        :disabled="!removable"
        :icon="mdiDelete"
        variant="text"
        @click="emit('remove')"
      />
    </v-card-title>
    <v-card-text>
      <div class="d-flex align-center flex-wrap ga-2 mb-2">
        <v-menu v-if="!readonly && !uebernommen && suggestions.length > 0">
          <template #activator="{ props: activatorProps }">
            <v-btn
              v-bind="activatorProps"
              :prepend-icon="mdiContentCopy"
              variant="text"
            >
              Aus dem Projekt übernehmen
            </v-btn>
          </template>
          <v-list :id="`${idPrefix}-projekt-adressen`">
            <!-- Keyed by position, because two Adressen of a Projekt can read the same. -->
            <v-list-item
              v-for="(suggestion, index) in suggestions"
              :key="index"
              :subtitle="nutzungTitle(suggestion)"
              :title="adresseTitle(suggestion)"
              @click="applyProjektAdresseSuggestion(nutzungsobjekt, suggestion)"
            />
          </v-list>
        </v-menu>

        <v-menu
          v-if="
            !readonly && !uebernommen && nutzungsobjektSuggestions.length > 0
          "
        >
          <template #activator="{ props: activatorProps }">
            <v-btn
              v-bind="activatorProps"
              :prepend-icon="mdiLinkVariant"
              variant="text"
            >
              Aus einer anderen Abrechnung übernehmen
            </v-btn>
          </template>
          <v-list :id="`${idPrefix}-nutzungsobjekte`">
            <v-list-item
              v-for="suggestion in nutzungsobjektSuggestions"
              :key="suggestion.id"
              :subtitle="nutzungTitle(suggestion)"
              :title="adresseTitle(suggestion)"
              @click="applyNutzungsobjektSuggestion(nutzungsobjekt, suggestion)"
            />
          </v-list>
        </v-menu>

        <v-btn
          v-if="!readonly && uebernommen"
          :id="`${idPrefix}-uebernahme-aufheben`"
          :prepend-icon="mdiLinkVariantOff"
          variant="text"
          @click="nutzungsobjekt.uebernommenesNutzungsobjektId = null"
        >
          Übernahme aufheben
        </v-btn>
      </div>

      <v-alert
        v-if="uebernommen && !readonly"
        class="mb-4"
        density="compact"
        text="Das Nutzungsobjekt gehört auch zu anderen Abrechnungen. Seine Daten lassen sich hier nicht ändern, nur die Positionen gehören zu dieser Abrechnung."
        type="info"
        variant="tonal"
      />

      <adresse-fields
        :id-prefix="idPrefix"
        :model-value="nutzungsobjekt"
        :readonly="uebernommen"
      >
        <template #after-nutzung>
          <v-checkbox
            :id="`${idPrefix}-aufschlag50prozent`"
            v-model="nutzungsobjekt.aufschlag50prozent"
            hide-details
            label="Aufschlag 50 %"
            :readonly="uebernommen"
          />
        </template>
      </adresse-fields>

      <h3 class="text-title-small mt-2 mb-2">Positionen</h3>

      <abrechnung-positionen-table
        v-model="nutzungsobjekt.positionen"
        :id-prefix="`${idPrefix}-position`"
        :readonly="readonly"
      />

      <unerlaubte-nutzung-fields
        :id-prefix="idPrefix"
        :model-value="nutzungsobjekt"
        :readonly="uebernommen"
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
            :readonly="uebernommen"
            rows="2"
          />
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import type { AbrechnungNutzungsobjektForm } from "@/types/abrechnung/AbrechnungNutzungsobjektForm";
import type { NutzungsobjektSuggestion } from "@/types/abrechnung/NutzungsobjektSuggestion";
import type { ProjektAdresseSuggestion } from "@/types/projekt/ProjektAdresseSuggestion";

import {
  mdiContentCopy,
  mdiDelete,
  mdiLinkVariant,
  mdiLinkVariantOff,
} from "@mdi/js";
import { computed } from "vue";

import AbrechnungPositionenTable from "@/components/AbrechnungPositionenTable.vue";
import AdresseFields from "@/components/common/AdresseFields.vue";
import UnerlaubteNutzungFields from "@/components/common/UnerlaubteNutzungFields.vue";
import { applyNutzungsobjektSuggestion } from "@/util/abrechnung/nutzungsobjektSuggestion";
import { adresseTitle, nutzungTitle } from "@/util/common/adresseLabel";
import { applyProjektAdresseSuggestion } from "@/util/projekt/projektAdresseSuggestion";

const nutzungsobjekt = defineModel<AbrechnungNutzungsobjektForm>({
  required: true,
});

const { nutzungsobjektSuggestions = [], readonly = false } = defineProps<{
  idPrefix: string;
  label: string;
  removable: boolean;
  suggestions: ProjektAdresseSuggestion[];
  nutzungsobjektSuggestions?: NutzungsobjektSuggestion[];
  readonly?: boolean;
}>();

const emit = defineEmits<{ remove: [] }>();

const uebernommen = computed(
  () => nutzungsobjekt.value.uebernommenesNutzungsobjektId !== null
);
</script>
