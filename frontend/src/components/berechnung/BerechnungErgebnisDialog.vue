<!--
  This dialog is temporary. It exists to compare the Berechnung against the prototype.

  Keeping all of it in one file is deliberate. The whole dialog goes away once a Bescheid replaces
  it, so splitting it into components would only create more files to delete.
-->
<template>
  <v-dialog
    v-model="open"
    scrollable
    width="1200"
  >
    <v-card>
      <v-card-title>Ergebnis der Berechnung (temporär zum Abgleich mit Prototyp)</v-card-title>
      <v-card-subtitle>{{ ueberschrift }}</v-card-subtitle>
      <v-card-text>
        <v-tabs v-model="tab">
          <v-tab
            id="ergebnis-tab-kontrolle"
            :value="TABS.KONTROLLE"
          >
            Kontrolle UI-Daten
          </v-tab>
          <v-tab
            id="ergebnis-tab-zusammenfassung"
            :value="TABS.ZUSAMMENFASSUNG"
          >
            Zusammenfassung
          </v-tab>
          <v-tab
            id="ergebnis-tab-berechnungslog"
            :value="TABS.BERECHNUNGSLOG"
          >
            Berechnungslog
          </v-tab>
          <v-tab
            id="ergebnis-tab-bescheiddaten"
            :value="TABS.BESCHEIDDATEN"
          >
            Bescheiddaten
          </v-tab>
        </v-tabs>

        <v-tabs-window
          v-model="tab"
          class="mt-4"
        >
          <v-tabs-window-item :value="TABS.KONTROLLE">
            <pre class="ergebnis-text">{{ kontrolldaten }}</pre>
          </v-tabs-window-item>

          <v-tabs-window-item :value="TABS.ZUSAMMENFASSUNG">
            <pre class="ergebnis-text">{{ ergebnis.zusammenfassung }}</pre>
          </v-tabs-window-item>

          <v-tabs-window-item :value="TABS.BERECHNUNGSLOG">
            <pre class="ergebnis-text">{{ ergebnis.berechnungslog }}</pre>
          </v-tabs-window-item>

          <v-tabs-window-item :value="TABS.BESCHEIDDATEN">
            <template
              v-for="(bescheiddaten, index) in ergebnis.bescheiddatenFlaechen"
              :key="`flaeche-${index}`"
            >
              <h3 class="text-title-medium mt-4">
                Flächen, Woche {{ bescheiddaten.wocheBeginn }} bis
                {{ bescheiddaten.wocheEnde }} ({{ bescheiddaten.wocheDauer }}
                Wochen)
              </h3>
              <div
                v-for="abschnitt in flaechenAbschnitte(bescheiddaten)"
                :key="abschnitt.label"
              >
                <div class="text-label-large mt-2">{{ abschnitt.label }}</div>
                <pre class="ergebnis-text">{{ abschnitt.text }}</pre>
              </div>
              <div class="text-label-large mt-2">
                Gebühr: {{ toEuroString(bescheiddaten.gebuehr) }}
              </div>
            </template>

            <template
              v-for="(
                adressabrechnung, index
              ) in ergebnis.abrechnungenUeberspannungen"
              :key="`ueberspannung-${index}`"
            >
              <h3 class="text-title-medium mt-4">
                Überspannungen, {{ adressabrechnung.adressbezeichnung }}
              </h3>
              <template
                v-for="(
                  bescheiddaten, monatIndex
                ) in adressabrechnung.abschnitte"
                :key="`ueberspannung-${index}-${monatIndex}`"
              >
                <div class="text-label-large mt-2">
                  Monat {{ bescheiddaten.monatBeginn }} bis
                  {{ bescheiddaten.monatEnde }} ({{ bescheiddaten.monatDauer }}
                  Monate)
                </div>
                <div
                  v-for="abschnitt in ueberspannungAbschnitte(bescheiddaten)"
                  :key="abschnitt.label"
                >
                  <div class="text-label-large mt-2">{{ abschnitt.label }}</div>
                  <pre class="ergebnis-text">{{ abschnitt.text }}</pre>
                </div>
                <div class="text-label-large mt-2">
                  Gebühr: {{ toEuroString(bescheiddaten.gebuehr) }}
                </div>
              </template>
            </template>

            <h3 class="text-title-medium mt-6">Gebühren</h3>
            <v-table density="compact">
              <tbody>
                <tr>
                  <th scope="row">Flächen</th>
                  <td class="text-right">
                    {{ toEuroString(ergebnis.gebuehrFlaechen) }}
                  </td>
                </tr>
                <tr>
                  <th scope="row">Überspannungen</th>
                  <td class="text-right">
                    {{ toEuroString(ergebnis.gebuehrUeberspannungen) }}
                  </td>
                </tr>
                <tr>
                  <th scope="row">Verwaltung</th>
                  <td class="text-right">
                    {{ toEuroString(ergebnis.gebuehrVerwaltung) }}
                  </td>
                </tr>
                <tr>
                  <th scope="row">Gesamt</th>
                  <td class="text-right">
                    {{ toEuroString(ergebnis.gebuehrGesamt) }}
                  </td>
                </tr>
                <tr>
                  <th scope="row">Zahlbetrag</th>
                  <td class="text-right">
                    {{ toEuroString(ergebnis.gebuehrZahlung) }}
                  </td>
                </tr>
              </tbody>
            </v-table>
          </v-tabs-window-item>
        </v-tabs-window>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn
          variant="text"
          @click="open = false"
        >
          Schließen
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import type {
  BerechnungResponseDTO,
  BescheiddatenFlaecheResponseDTO,
  BescheiddatenUeberspannungResponseDTO,
} from "@/api/generated/sonar-backend";
import type { BerechnungsKontext } from "@/util/berechnung/berechnungsKontext";

import { computed, ref } from "vue";

import {
  kontrolldatenText,
  toDateString,
  toEuroString,
} from "@/util/berechnung/berechnungKontrolle";

interface Abschnitt {
  label: string;
  text: string;
}

const TABS = {
  KONTROLLE: "kontrolle",
  ZUSAMMENFASSUNG: "zusammenfassung",
  BERECHNUNGSLOG: "berechnungslog",
  BESCHEIDDATEN: "bescheiddaten",
} as const;

const open = defineModel<boolean>({ required: true });

const { ergebnis, kontext } = defineProps<{
  ergebnis: BerechnungResponseDTO;
  kontext: BerechnungsKontext;
}>();

const tab = ref<string>(TABS.KONTROLLE);

const ueberschrift = computed(
  () =>
    `Abrechnung ${ergebnis.lfdNr}, ` +
    `${toDateString(ergebnis.abrechnungszeitraumVon)} bis ` +
    `${toDateString(ergebnis.abrechnungszeitraumBis)}`
);

const kontrolldaten = computed(() =>
  kontrolldatenText({
    abrechnung: kontext.abrechnung,
    projekt: kontext.projekt,
    geschaeftspartner: kontext.geschaeftspartner,
    verwaltungsgebuehr: ergebnis.gebuehrVerwaltung,
  })
);

function flaechenAbschnitte(
  bescheiddaten: BescheiddatenFlaecheResponseDTO
): Abschnitt[] {
  return [
    { label: "Grund", text: bescheiddaten.grund ?? "" },
    {
      label: "Flächenverteilung",
      text: bescheiddaten.flaechenVerteilung ?? "",
    },
    { label: "Gebührenstufe", text: bescheiddaten.gebuehrenstufe ?? "" },
    { label: "Berechnung", text: bescheiddaten.berechnung ?? "" },
  ].filter((abschnitt) => abschnitt.text.trim());
}

function ueberspannungAbschnitte(
  bescheiddaten: BescheiddatenUeberspannungResponseDTO
): Abschnitt[] {
  return [
    { label: "Grund", text: bescheiddaten.grund ?? "" },
    { label: "Berechnung", text: bescheiddaten.berechnung ?? "" },
  ].filter((abschnitt) => abschnitt.text.trim());
}
</script>

<style scoped>
.ergebnis-text {
  font-family: monospace;
  font-size: 0.8rem;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
