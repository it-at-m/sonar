<template>
  <v-dialog
    v-model="showAppearance"
    max-width="420"
    aria-labelledby="appearance-title"
  >
    <v-card theme="light">
      <v-card-title id="appearance-title">Darstellung</v-card-title>
      <v-card-text>
        <p class="text-subtitle-1 font-weight-medium mb-4">Anwendungsfarbe</p>
        <div class="d-flex flex-wrap ga-3 mb-4">
          <v-btn
            v-for="preset in presets"
            :key="preset.color"
            :color="preset.color"
            :aria-label="preset.label"
            :aria-pressed="themeColor === preset.color"
            :class="{ 'selected-swatch': themeColor === preset.color }"
            :title="preset.label"
            variant="flat"
            size="40"
            icon
            @click="updateThemeColor(preset.color)"
          >
            <v-icon
              v-if="themeColor === preset.color"
              :icon="mdiCheck"
            />
          </v-btn>
        </div>
        <v-btn
          variant="text"
          :prepend-icon="mdiPaletteOutline"
          :aria-expanded="showCustomColor"
          aria-controls="custom-color-picker"
          @click="showCustomColor = !showCustomColor"
        >
          Eigene Farbe
        </v-btn>
        <div
          v-if="showCustomColor"
          id="custom-color-picker"
          class="mt-3"
        >
          <v-color-picker
            :model-value="themeColor"
            mode="hex"
            :modes="['hex']"
            width="100%"
            hide-headers
            aria-label="Anwendungsfarbe"
            @update:model-value="updateThemeColor"
          />
        </div>
        <p class="text-body-2 text-medium-emphasis mt-4 mb-0">
          In diesem Browser gespeichert.
        </p>
      </v-card-text>
      <v-card-actions class="flex-wrap">
        <v-btn @click="updateThemeColor(DEFAULT_THEME_COLOR)">
          Standard wiederherstellen
        </v-btn>
        <v-spacer />
        <v-btn
          variant="tonal"
          @click="showAppearance = false"
        >
          Schließen
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { mdiCheck, mdiPaletteOutline } from "@mdi/js";
import { computed, ref, watch } from "vue";
import { useTheme } from "vuetify";

import { DEFAULT_THEME_COLOR, saveThemeColor } from "@/util/themeColor";

const showAppearance = defineModel<boolean>({ required: true });
const showCustomColor = ref(false);
const theme = useTheme();
const themeColor = computed(() =>
  String(theme.themes.value.light?.colors.primary ?? DEFAULT_THEME_COLOR)
);
const presets = [
  { label: "Anthrazit (Standard)", color: DEFAULT_THEME_COLOR },
  { label: "Blau", color: "#1565C0" },
  { label: "Petrol", color: "#00695C" },
  { label: "Violett", color: "#6A1B9A" },
  { label: "Bordeaux", color: "#9C2542" },
  { label: "Gold", color: "#FFCC00" },
];

watch(showAppearance, (isOpen) => {
  if (isOpen) showCustomColor.value = false;
});

function updateThemeColor(value: unknown): void {
  const color = saveThemeColor(value);
  const lightTheme = theme.themes.value.light;
  if (color !== null && lightTheme) {
    lightTheme.colors.primary = color;
  }
}
</script>

<style scoped>

</style>
