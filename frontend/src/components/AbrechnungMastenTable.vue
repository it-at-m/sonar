<template>
  <div>
    <div>
      <v-table density="compact">
        <thead>
          <tr>
            <th>Mast</th>
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
            <td>Mast {{ index + 1 }}</td>
            <td>
              <v-text-field
                :id="`${idPrefix}-beginn-${index}`"
                v-model="mast.beginn"
                :aria-label="`Beginn des Masten ${index + 1}`"
                density="compact"
                :rules="[requiredRule]"
                type="date"
              />
            </td>
            <td>
              <v-text-field
                :id="`${idPrefix}-ende-${index}`"
                v-model="mast.ende"
                :aria-label="`Ende des Masten ${index + 1}`"
                density="compact"
                :rules="[
                  requiredRule,
                  (ende: string) => endeNotBeforeBeginn(mast.beginn, ende),
                ]"
                type="date"
              />
            </td>
            <td v-if="!readonly">
              <v-btn
                :aria-label="`Mast ${index + 1} entfernen`"
                :disabled="masten.length <= minLength"
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
import type { AbrechnungMastForm } from "@/types/AbrechnungMastForm";

import { mdiDelete, mdiPlus } from "@mdi/js";

import { createAbrechnungMast } from "@/util/abrechnungMastForm";
import { endeNotBeforeBeginn, requiredRule } from "@/util/validationRules";

const masten = defineModel<AbrechnungMastForm[]>({ required: true });

// minLength drops to 0 once the Nutzungsobjekt carries Positionen, because Positionen alone are a
// valid Nutzungsobjekt and the last Mast has to become removable for one to be entered.
const { readonly = false, minLength = 1 } = defineProps<{
  idPrefix: string;
  minLength?: number;
  readonly?: boolean;
}>();

function addMast(): void {
  masten.value.push(createAbrechnungMast());
}

function removeMast(index: number): void {
  masten.value.splice(index, 1);
}
</script>
