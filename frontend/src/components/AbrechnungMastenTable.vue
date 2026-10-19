<template>
  <div>
    <div>
      <v-table density="compact">
        <thead>
          <tr>
            <th>Beginn</th>
            <th>Ende</th>
            <th v-if="!readonly" />
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(mast, index) in masten"
            :key="mast.id"
          >
            <td>
              <date-field
                :id="`${idPrefix}-beginn-${index}`"
                v-model="mast.beginn"
                :aria-label="`Beginn von Mast ${index + 1}`"
                density="compact"
                persistent-placeholder
                :readonly="readonly || undefined"
                :rules="[requiredRule]"
              />
            </td>
            <td>
              <date-field
                :id="`${idPrefix}-ende-${index}`"
                v-model="mast.ende"
                :aria-label="`Ende von Mast ${index + 1}`"
                density="compact"
                persistent-placeholder
                :readonly="readonly || undefined"
                :rules="[requiredRule]"
                :zeitraum-beginn="mast.beginn"
              />
            </td>
            <td v-if="!readonly">
              <v-btn
                :aria-label="`Mast ${index + 1} entfernen`"
                :disabled="!removable"
                :icon="mdiDelete"
                size="small"
                variant="text"
                @click="removeMast(index)"
              />
            </td>
          </tr>
        </tbody>
      </v-table>
    </div>

    <v-btn
      v-if="!readonly"
      class="mt-2"
      :prepend-icon="mdiPlus"
      variant="text"
      @click="addMast"
    >
      Mast hinzufügen
    </v-btn>
  </div>
</template>

<script setup lang="ts">
import type { AbrechnungMastForm } from "@/types/abrechnung/AbrechnungMastForm";

import { mdiDelete, mdiPlus } from "@mdi/js";

import DateField from "@/components/common/DateField.vue";
import { createAbrechnungMast } from "@/util/abrechnung/abrechnungMastForm";
import { requiredRule } from "@/util/validationRules";

const masten = defineModel<AbrechnungMastForm[]>({ required: true });

const { readonly = false, removable } = defineProps<{
  idPrefix: string;
  removable: boolean;
  readonly?: boolean;
}>();

function addMast(): void {
  if (masten.value.length === 0) {
    masten.value.push(createAbrechnungMast(), createAbrechnungMast());
  } else {
    masten.value.push(createAbrechnungMast());
  }
}

function removeMast(index: number): void {
  masten.value.splice(index, 1);
}
</script>

<style scoped>
:deep(.v-input__details) {
  width: 0;
  min-width: 100%;
}

:deep(.v-messages__message) {
  line-height: normal;
}

td {
  vertical-align: top;
}
</style>
