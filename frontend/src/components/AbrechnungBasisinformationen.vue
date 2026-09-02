<template>
  <h2 class="text-headline-small mb-2">Geschäftspartner:in</h2>
  <v-row>
    <v-col
      cols="12"
      md="4"
    >
      <v-text-field
        id="basis-geschaeftspartner-id"
        v-model="abrechnung.geschaeftspartnerId"
        label="Geschäftspartner:in ID"
        :maxlength="GESCHAEFTSPARTNER_ID_MAX_LENGTH"
        :rules="[requiredRule]"
        :error-messages="geschaeftspartner.fehlermeldung.value"
      />
    </v-col>
  </v-row>
  <v-row>
    <v-col
      cols="12"
      md="8"
    >
      <geschaeftspartner-stammdaten
        :daten="geschaeftspartner.daten.value"
        :loading="geschaeftspartner.loading.value"
      />
    </v-col>
  </v-row>

  <v-switch
    v-model="abrechnung.zustellungsbevollmaechtigterGenutzt"
    class="mt-2"
    color="primary"
    label="Zustellungsbevollmächtigte:r vorhanden"
    hide-details
  />

  <template v-if="abrechnung.zustellungsbevollmaechtigterGenutzt">
    <h2 class="text-headline-small mt-4 mb-2">Zustellungsbevollmächtigte:r</h2>
    <v-row>
      <v-col
        cols="12"
        md="4"
      >
        <v-text-field
          id="basis-zustellungsbevollmaechtigter-id"
          v-model="abrechnung.zustellungsbevollmaechtigterId"
          label="Zustellungsbevollmächtigte:r ID"
          :maxlength="GESCHAEFTSPARTNER_ID_MAX_LENGTH"
          :rules="[requiredRule]"
          :error-messages="zustellungsbevollmaechtigter.fehlermeldung.value"
        />
      </v-col>
      <v-col
        cols="12"
        md="4"
      >
        <v-select
          id="basis-zustellungsbevollmaechtigter-typ"
          v-model="abrechnung.zustellungsbevollmaechtigterTyp"
          label="Typ"
          :items="ZUSTELLUNGSBEVOLLMAECHTIGTER_TYP_OPTIONS"
          :rules="[requiredRule]"
        />
      </v-col>
    </v-row>
    <v-row>
      <v-col
        cols="12"
        md="8"
      >
        <geschaeftspartner-stammdaten
          :daten="zustellungsbevollmaechtigter.daten.value"
          :loading="zustellungsbevollmaechtigter.loading.value"
        />
      </v-col>
    </v-row>
  </template>
</template>

<script setup lang="ts">
import type { AbrechnungRequestDTOZustellungsbevollmaechtigterTypEnum } from "@/api/generated/sonar-backend";
import type { AbrechnungForm } from "@/composables/abrechnungForm";

import { watchDebounced } from "@vueuse/core";

import GeschaeftspartnerStammdaten from "@/components/GeschaeftspartnerStammdaten.vue";
import { useGeschaeftspartnerLookup } from "@/composables/geschaeftspartnerLookup";
import { requiredRule } from "@/util/validationRules";

const GESCHAEFTSPARTNER_ID_MAX_LENGTH = 10;
const LOOKUP_DEBOUNCE_MS = 300;

const ZUSTELLUNGSBEVOLLMAECHTIGTER_TYP_OPTIONS: {
  title: string;
  value: AbrechnungRequestDTOZustellungsbevollmaechtigterTypEnum;
}[] = [
  { title: "01-Gesetzliche:r Vertreter:in", value: "GESETZLICHER_VERTRETER" },
  { title: "02-Vormund", value: "VORMUND" },
  { title: "03-Betreuer:in", value: "BETREUER" },
  { title: "04-Nachlassverwalter:in", value: "NACHLASSVERWALTER" },
  { title: "05-Nachlasspfleger:in", value: "NACHLASSPFLEGER" },
  { title: "06-Testamentsvollstrecker:in", value: "TESTAMENTSVOLLSTRECKER" },
  { title: "07-Zwangsverwalter:in", value: "ZWANGSVERWALTER" },
  { title: "08-Pfleger:in", value: "PFLEGER" },
  { title: "10-Vorstand", value: "VORSTAND" },
  {
    title: "11-Geschäftsf. Gesellschafter:in",
    value: "GESCHAEFTSFUEHRENDER_GESELLSCHAFTER",
  },
  { title: "13-Liquidator:in", value: "LIQUIDATOR" },
  { title: "14-Konkursverwalter:in", value: "KONKURSVERWALTER" },
  { title: "15-steuerliche:r Vertreter:in", value: "STEUERLICHER_VERTRETER" },
  { title: "16-Empfangsbevollmächtigte:r", value: "EMPFANGSBEVOLLMAECHTIGTER" },
  { title: "19-Vergleichsverwalter:in", value: "VERGLEICHSVERWALTER" },
  { title: "21-Gesamtrechtsnachfolger:in", value: "GESAMTRECHTSNACHFOLGER" },
  { title: "22-Verwalter:in nach dem WEG", value: "VERWALTER_NACH_DEM_WEG" },
  { title: "23-Geschäftsführer:in", value: "GESCHAEFTSFUEHRER" },
  { title: "24-Gesellschafter:in", value: "GESELLSCHAFTER" },
  { title: "25-Insolvenzverwalter:in", value: "INSOLVENZVERWALTER" },
  { title: "27-Nachtragsliquidator:in", value: "NACHTRAGSLIQUIDATOR" },
  { title: "28-Treuhänder:in", value: "TREUHAENDER" },
  {
    title: "29-vorläufige:r Insolvenzverwalter:in",
    value: "VORLAEUFIGER_INSOLVENZVERWALTER",
  },
];

const abrechnung = defineModel<AbrechnungForm>({ required: true });

const geschaeftspartner = useGeschaeftspartnerLookup();
const zustellungsbevollmaechtigter = useGeschaeftspartnerLookup();

watchDebounced(
  () => abrechnung.value.geschaeftspartnerId,
  (id) => void geschaeftspartner.lookup(id),
  { debounce: LOOKUP_DEBOUNCE_MS }
);
watchDebounced(
  () => abrechnung.value.zustellungsbevollmaechtigterId,
  (id) => void zustellungsbevollmaechtigter.lookup(id),
  { debounce: LOOKUP_DEBOUNCE_MS }
);
</script>
