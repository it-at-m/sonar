<template>
  <v-radio-group
    v-model="adresse.art"
    class="art-group"
    inline
    label="Erfassung als"
    @update:model-value="clearFieldsOfUnselectedArt(adresse)"
  >
    <v-radio
      label="Adresse"
      :value="ProjektAdresseRequestDTOArtEnum.ADRESSE"
    />
    <v-radio
      label="Flurstück"
      :value="ProjektAdresseRequestDTOArtEnum.FLURSTUECK"
    />
  </v-radio-group>

  <v-row>
    <template v-if="isAdresse">
      <v-col
        cols="12"
        md="3"
      >
        <v-text-field
          :id="`${idPrefix}-adresse`"
          v-model="adresse.adresse"
          label="Adresse"
          maxlength="255"
          :rules="[requiredRule]"
        />
      </v-col>
      <v-col
        cols="12"
        md="2"
      >
        <v-text-field
          :id="`${idPrefix}-hausnummer-von`"
          v-model="adresse.hausnummerVon"
          label="Hausnummer von"
          maxlength="20"
          :rules="[requiredRule]"
        />
      </v-col>
      <v-col
        cols="12"
        md="2"
      >
        <v-text-field
          :id="`${idPrefix}-hausnummer-bis`"
          v-model="adresse.hausnummerBis"
          hint="Nur bei einer Spanne von Hausnummern"
          label="Hausnummer bis"
          maxlength="20"
          persistent-hint
        />
      </v-col>
    </template>
    <template v-else>
      <v-col
        cols="12"
        md="3"
      >
        <v-text-field
          :id="`${idPrefix}-flurstueck`"
          v-model="adresse.flurstueck"
          label="Flurstück"
          maxlength="255"
          :rules="[requiredRule]"
        />
      </v-col>
      <v-col
        cols="12"
        md="4"
      >
        <v-text-field
          :id="`${idPrefix}-gemarkung`"
          v-model="adresse.gemarkung"
          label="Gemarkung"
          maxlength="255"
          :rules="[requiredRule]"
        />
      </v-col>
    </template>

    <v-col
      cols="12"
      :md="lastCols"
    >
      <v-select
        :id="`${idPrefix}-nutzung`"
        v-model="adresse.nutzungen"
        chips
        clearable
        closable-chips
        :items="NUTZUNG_OPTIONS"
        label="Nutzung"
        multiple
        @update:model-value="clearSonstigesOfUnselectedNutzung(adresse)"
      />
    </v-col>

    <v-col
      v-if="hasAfterNutzung"
      cols="12"
      md="2"
    >
      <slot name="after-nutzung" />
    </v-col>

    <v-col
      v-if="isSonstigeNutzung"
      class="ms-md-auto"
      cols="12"
      md="5"
    >
      <v-text-field
        :id="`${idPrefix}-nutzung-sonstiges`"
        v-model="adresse.nutzungSonstiges"
        label="Beschreibung der Nutzung"
        maxlength="255"
      />
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import type { Adresse } from "@/types/common/Adresse";

import { computed, useSlots } from "vue";

import {
  ProjektAdresseRequestDTOArtEnum,
  ProjektAdresseRequestDTONutzungenEnum,
} from "@/api/generated/sonar-backend";
import {
  clearFieldsOfUnselectedArt,
  clearSonstigesOfUnselectedNutzung,
} from "@/util/common/adresseForm";
import { NUTZUNG_OPTIONS } from "@/util/common/nutzungOptions";
import { requiredRule } from "@/util/validationRules";

const adresse = defineModel<Adresse>({ required: true });

defineProps<{
  idPrefix: string;
}>();

const isAdresse = computed(
  () => adresse.value.art === ProjektAdresseRequestDTOArtEnum.ADRESSE
);

const isSonstigeNutzung = computed(() =>
  adresse.value.nutzungen.includes(
    ProjektAdresseRequestDTONutzungenEnum.SONSTIGES
  )
);

const slots = useSlots();

const hasAfterNutzung = computed(() => slots["after-nutzung"] !== undefined);

const lastCols = computed(() => (hasAfterNutzung.value ? 3 : 5));
</script>

<style scoped>
/* Vuetify stacks the label of a radio group above the options; we want them on one line. */
.art-group :deep(.v-input__control) {
  flex-direction: row;
  flex-wrap: wrap;
  align-items: center;
  column-gap: 16px;
}

.art-group :deep(.v-input__control > .v-selection-control-group) {
  padding-inline-start: 0;
  margin-top: 0;
}
</style>
