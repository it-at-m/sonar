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
            <th>Anteil Fläche (m²)</th>
            <th v-if="!readonly" />
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(position, index) in positionen"
            :key="position.id"
          >
            <td>
              <sonar-date-field
                :id="`${idPrefix}-beginn-${index}`"
                v-model="position.beginn"
                :allowed-dates="possibleBeginn(index)"
                :aria-label="`Beginn der Position ${index + 1}`"
                density="compact"
                :disabled="(!readonly && index === 0) || undefined"
                :readonly="readonly || undefined"
                :rules="beginnRules(index)"
              />
            </td>
            <td>
              <sonar-date-field
                :id="`${idPrefix}-ende-${index}`"
                v-model="position.ende"
                :aria-label="`Ende der Position ${index + 1}`"
                density="compact"
                :disabled="!readonly || undefined"
                :readonly="readonly || undefined"
              />
            </td>
            <td>
              <v-number-input
                :id="`${idPrefix}-laenge-${index}`"
                :aria-label="`Länge der Position ${index + 1}`"
                control-variant="hidden"
                density="compact"
                :model-value="position.laenge"
                :precision="2"
                :rules="[laengeRule(position), greaterThanZeroRule]"
                @update:model-value="setLaenge(position, $event)"
              />
            </td>
            <td>
              <v-number-input
                :id="`${idPrefix}-breite-${index}`"
                :aria-label="`Breite der Position ${index + 1}`"
                control-variant="hidden"
                density="compact"
                :model-value="position.breite"
                :precision="2"
                :rules="[breiteRule(position), greaterThanZeroRule]"
                @update:model-value="setBreite(position, $event)"
              />
            </td>
            <td>
              <v-number-input
                :id="`${idPrefix}-flaeche-${index}`"
                v-model="position.flaeche"
                :aria-label="`Fläche der Position ${index + 1}`"
                control-variant="hidden"
                density="compact"
                :disabled="hasLaengeUndBreite(position)"
                :precision="2"
                :rules="[requiredRule, greaterThanZeroRule]"
                :title="
                  hasLaengeUndBreite(position)
                    ? 'Aus Länge und Breite berechnet'
                    : 'Alternativ zu Länge und Breite eintragbar'
                "
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
            <td v-if="!readonly">
              <v-btn
                :aria-label="`Position ${index + 1} entfernen`"
                :disabled="!removable"
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
      v-if="!readonly"
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
import type { AbrechnungPositionForm } from "@/types/abrechnung/AbrechnungPositionForm";
import type { ValidationRule } from "@/util/validationRules";

import { mdiDelete, mdiPlus } from "@mdi/js";

import SonarDateField from "@/components/common/SonarDateField.vue";
import {
  createAbrechnungPosition,
  hasLaengeUndBreite,
  isPossibleBeginn,
  setBreite,
  setLaenge,
} from "@/util/abrechnung/abrechnungPositionForm";
import {
  beginnRule,
  breiteRule,
  laengeRule,
} from "@/util/abrechnung/abrechnungPositionRules";
import { toIsoDate } from "@/util/common/isoDate";
import { greaterThanZeroRule, requiredRule } from "@/util/validationRules";

const positionen = defineModel<AbrechnungPositionForm[]>({ required: true });

const { readonly = false, removable } = defineProps<{
  idPrefix: string;
  removable: boolean;
  readonly?: boolean;
}>();

function beginnRules(index: number): ValidationRule[] {
  const vorgaenger = positionen.value[index - 1];
  const position = positionen.value[index];
  return vorgaenger === undefined || position === undefined
    ? [requiredRule]
    : [requiredRule, beginnRule(vorgaenger, position)];
}

function possibleBeginn(
  index: number
): ((value: unknown) => boolean) | undefined {
  const vorgaenger = positionen.value[index - 1];
  const position = positionen.value[index];
  if (vorgaenger === undefined || position === undefined) {
    return undefined;
  }
  return (value) => isPossibleBeginn(vorgaenger, position, toIsoDate(value));
}

function addPosition(): void {
  positionen.value.push(createAbrechnungPosition());
}

function removePosition(index: number): void {
  positionen.value.splice(index, 1);
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
