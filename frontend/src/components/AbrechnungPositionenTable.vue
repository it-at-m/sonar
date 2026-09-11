<template>
  <div>
    <div>
      <v-table density="compact">
        <thead>
          <tr>
            <th>Beginn</th>
            <th>Ende</th>
            <th>Länge (m)</th>
            <th>Breite (m)</th>
            <th>Fläche (m²)</th>
            <th>50 %</th>
            <th>Anteil Fläche (m²)</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(position, index) in positionen"
            :key="position.id"
          >
            <td>
              <v-text-field
                :id="`${idPrefix}-beginn-${index}`"
                v-model="position.beginn"
                :aria-label="`Beginn der Position ${index + 1}`"
                density="compact"
                :rules="[requiredRule]"
                type="date"
              />
            </td>
            <td>
              <v-text-field
                :id="`${idPrefix}-ende-${index}`"
                v-model="position.ende"
                :aria-label="`Ende der Position ${index + 1}`"
                density="compact"
                :rules="[
                  requiredRule,
                  (ende: string) => endeNotBeforeBeginn(position.beginn, ende),
                ]"
                type="date"
              />
            </td>
            <td>
              <v-number-input
                :id="`${idPrefix}-laenge-${index}`"
                v-model="position.laenge"
                :aria-label="`Länge der Position ${index + 1}`"
                control-variant="hidden"
                density="compact"
                :precision="2"
                :rules="[requiredRule, greaterThanZeroRule]"
              />
            </td>
            <td>
              <v-number-input
                :id="`${idPrefix}-breite-${index}`"
                v-model="position.breite"
                :aria-label="`Breite der Position ${index + 1}`"
                control-variant="hidden"
                density="compact"
                :precision="2"
                :rules="[requiredRule, greaterThanZeroRule]"
              />
            </td>
            <td>
              <v-number-input
                :id="`${idPrefix}-flaeche-${index}`"
                v-model="position.flaeche"
                :aria-label="`Fläche der Position ${index + 1}`"
                control-variant="hidden"
                density="compact"
                :precision="2"
                :rules="[requiredRule, greaterThanZeroRule]"
              />
            </td>
            <td>
              <v-checkbox
                v-model="position.haelfte"
                :aria-label="`50 % für Position ${index + 1}`"
                density="compact"
                hide-details
              />
            </td>
            <td>
              <v-number-input
                :id="`${idPrefix}-anteil-${index}`"
                v-model="position.anteilAnFlaeche"
                :aria-label="`Anteil Fläche der Position ${index + 1}`"
                control-variant="hidden"
                density="compact"
                :precision="2"
                :rules="[requiredRule, greaterThanZeroRule]"
              />
            </td>
            <td>
              <v-btn
                :aria-label="`Position ${index + 1} entfernen`"
                :disabled="positionen.length === 1"
                :icon="mdiDelete"
                size="small"
                variant="text"
                @click="removePosition(index)"
              />
            </td>
          </tr>
        </tbody>
      </v-table>
    </div>

    <v-btn
      class="mt-2"
      :prepend-icon="mdiPlus"
      variant="text"
      @click="addPosition"
    >
      Position hinzufügen
    </v-btn>
  </div>
</template>

<script setup lang="ts">
import type { AbrechnungPositionForm } from "@/types/AbrechnungPositionForm";

import { mdiDelete, mdiPlus } from "@mdi/js";

import { createAbrechnungPosition } from "@/util/abrechnungPositionForm";
import {
  endeNotBeforeBeginn,
  greaterThanZeroRule,
  requiredRule,
} from "@/util/validationRules";

const positionen = defineModel<AbrechnungPositionForm[]>({ required: true });

defineProps<{
  idPrefix: string;
}>();

function addPosition(): void {
  positionen.value.push(createAbrechnungPosition());
}

function removePosition(index: number): void {
  positionen.value.splice(index, 1);
}
</script>
