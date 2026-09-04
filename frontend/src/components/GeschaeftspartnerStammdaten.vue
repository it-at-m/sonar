<template>
  <v-card
    v-if="loading || daten"
    variant="tonal"
    class="pa-4"
  >
    <v-skeleton-loader
      v-if="loading"
      type="text@4"
    />
    <template v-else>
      <div v-if="daten?.anrede">
        {{ daten.anrede }}
      </div>
      <div
        v-for="(zeile, index) in namenszeilen"
        :key="`name-${index}`"
        class="font-weight-medium"
      >
        {{ zeile }}
      </div>

      <div
        v-for="(zeile, index) in adresszeilen"
        :key="`adresse-${index}`"
        :class="{ 'mt-2': index === 0 }"
      >
        {{ zeile }}
      </div>

      <template v-if="kontaktzeilen.length">
        <v-divider class="my-3" />
        <div
          v-for="zeile in kontaktzeilen"
          :key="zeile.label"
          class="text-body-2"
        >
          <span class="text-medium-emphasis">{{ zeile.label }}:</span>
          {{ zeile.wert }}
        </div>
      </template>

      <div
        v-if="daten?.adressnotiz"
        class="text-body-2 text-medium-emphasis mt-3"
      >
        {{ daten.adressnotiz }}
      </div>
    </template>
  </v-card>
</template>

<script setup lang="ts">
import type { GeschaeftspartnerResponseDTO } from "@/api/generated/sonar-backend";

import { computed } from "vue";

const props = defineProps<{
  daten: GeschaeftspartnerResponseDTO | null;
  loading: boolean;
}>();

function isFilled(wert: string | undefined): wert is string {
  return Boolean(wert?.trim());
}

function line(...teile: (string | undefined)[]): string {
  return teile.filter(isFilled).join(" ");
}

const namenszeilen = computed<string[]>(() =>
  [
    props.daten?.name1,
    props.daten?.name2,
    props.daten?.name3,
    props.daten?.name4,
    line(props.daten?.vorname, props.daten?.nachname),
  ].filter(isFilled)
);

const adresszeilen = computed<string[]>(() =>
  [
    props.daten?.coName ? `c/o ${props.daten.coName}` : undefined,
    line(props.daten?.strasse, props.daten?.hausnummer),
    props.daten?.adresszusatz,
    line(props.daten?.postleitzahl, props.daten?.ort),
    props.daten?.land,
  ].filter(isFilled)
);

const kontaktzeilen = computed<{ label: string; wert: string }[]>(() =>
  [
    { label: "Telefon", wert: props.daten?.telefon },
    { label: "Mobil", wert: props.daten?.mobil },
    { label: "Fax", wert: props.daten?.fax },
    { label: "E-Mail", wert: props.daten?.email },
  ].filter((zeile): zeile is { label: string; wert: string } =>
    isFilled(zeile.wert)
  )
);
</script>
