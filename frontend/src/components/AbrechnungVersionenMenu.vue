<template>
  <v-menu @update:model-value="onMenuToggled">
    <template #activator="{ props: activatorProps }">
      <v-btn
        v-bind="activatorProps"
        aria-label="Versionen der Abrechnung anzeigen"
        density="comfortable"
        :icon="mdiEye"
        variant="text"
      />
    </template>

    <v-list>
      <v-list-item
        v-if="versionen === undefined"
        title="Die Versionen werden geladen."
      >
        <template #prepend>
          <v-progress-circular
            class="mr-2"
            color="primary"
            indeterminate
            size="20"
          />
        </template>
      </v-list-item>

      <v-list-item
        v-else-if="versionen.length === 0"
        title="Die Versionen konnten nicht geladen werden."
      />

      <template v-else>
        <v-list-item
          v-for="version in versionen"
          :key="version.id"
          :title="abrechnungVersionLabel(version)"
          :to="`/projekte/${projektId}/abrechnungen/${version.id}/ansehen`"
        />
      </template>
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import type { AbrechnungVersion } from "@/types/abrechnung/AbrechnungVersion";

import { mdiEye } from "@mdi/js";
import { ref } from "vue";

import {
  abrechnungVersionLabel,
  fetchAbrechnungVersionen,
} from "@/util/abrechnung/abrechnungVersionen";

const { abrechnungId, projektId } = defineProps<{
  projektId: string;
  abrechnungId: string;
}>();

const versionen = ref<AbrechnungVersion[]>();

/**
 * The versions are loaded when the menu opens and not when it mounts. That way the requests do not
 * all fire at the start of the overview.
 */
async function onMenuToggled(opened: boolean): Promise<void> {
  if (!opened || versionen.value?.length) {
    return;
  }
  versionen.value = undefined;
  try {
    versionen.value = await fetchAbrechnungVersionen(projektId, abrechnungId);
  } catch {
    versionen.value = [];
  }
}
</script>
