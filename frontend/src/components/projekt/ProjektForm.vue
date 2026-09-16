<template>
  <v-form
    ref="form"
    @submit.prevent="submit"
  >
    <v-row>
      <v-col
        cols="12"
        md="4"
      >
        <v-text-field
          v-model="projekt.projektnummer"
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
          v-model="projekt.abrechnungBeginn"
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
          v-model="projekt.abrechnungEnde"
          label="Abrechnung Ende"
          type="date"
          :rules="[requiredRule, abrechnungEndeRule(projekt.abrechnungBeginn)]"
        />
      </v-col>
    </v-row>

    <h2 class="text-headline-small mt-4 mb-2">Adressen/Flurstücke</h2>

    <projekt-form-adresse-card
      v-for="(adresse, index) in projekt.adressen"
      :key="adresse.id"
      :id-prefix="`adresse-${index}`"
      :model-value="adresse"
      :position="index + 1"
      :removable="projekt.adressen.length > 1"
      @remove="removeAdresse(index)"
    />

    <v-btn
      :prepend-icon="mdiPlus"
      variant="text"
      @click="addAdresse"
    >
      Adresse/Flurstück hinzufügen
    </v-btn>

    <div class="d-flex justify-end mt-6">
      <v-btn
        class="mr-2"
        variant="text"
        @click="emit('cancel')"
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
</template>

<script setup lang="ts">
import type { ProjektRequestDTO } from "@/api/generated/sonar-backend";

import { mdiPlus } from "@mdi/js";
import { useTemplateRef } from "vue";

import ProjektFormAdresseCard from "@/components/projekt/ProjektFormAdresseCard.vue";
import { useProjektForm } from "@/composables/projektForm";
import { toProjektRequestDTO } from "@/util/projektMapper";
import { abrechnungEndeRule } from "@/util/projektRules";
import { requiredRule } from "@/util/validationRules";

defineProps<{ saving: boolean }>();

const emit = defineEmits<{
  save: [projekt: ProjektRequestDTO];
  cancel: [];
}>();

const form = useTemplateRef("form");

const { addAdresse, isDirty, projekt, removeAdresse } = useProjektForm();

async function submit(): Promise<void> {
  const validation = await form.value?.validate();
  if (!validation?.valid) {
    return;
  }
  emit("save", toProjektRequestDTO(projekt));
}

defineExpose({ isDirty });
</script>
