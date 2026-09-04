<template>
  <v-card
    class="mb-4"
    variant="outlined"
  >
    <v-card-title class="d-flex align-center">
      <span class="text-title-medium">{{ title }}</span>
      <v-spacer />
      <v-btn
        :aria-label="`${title} entfernen`"
        :disabled="!removable"
        :icon="mdiDelete"
        variant="text"
        @click="emit('remove')"
      />
    </v-card-title>
    <v-card-text>
      <adresse-fields
        :id-prefix="idPrefix"
        :model-value="adresse"
      />

      <unerlaubte-nutzung-fields
        :id-prefix="idPrefix"
        :model-value="adresse"
      />

      <v-row>
        <v-col
          cols="12"
          md="3"
        >
          <v-number-input
            :id="`${idPrefix}-anzahl-mahnungen`"
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
            :id="`${idPrefix}-sondernutzung-erlaubt`"
            v-model="adresse.sondernutzungErlaubt"
            label="Sondernutzung erlaubt"
            hide-details
          />
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import type { ProjektAdresseForm } from "@/types/ProjektAdresseForm";

import { mdiDelete } from "@mdi/js";
import { computed } from "vue";

import AdresseFields from "@/components/common/AdresseFields.vue";
import UnerlaubteNutzungFields from "@/components/common/UnerlaubteNutzungFields.vue";
import { adresseLabel } from "@/util/adresseLabel";
import { requiredRule } from "@/util/validationRules";

const adresse = defineModel<ProjektAdresseForm>({ required: true });

const props = defineProps<{
  idPrefix: string;
  position: number;
  removable: boolean;
}>();

const emit = defineEmits<{ remove: [] }>();

const title = computed(() => adresseLabel(adresse.value, props.position));
</script>
