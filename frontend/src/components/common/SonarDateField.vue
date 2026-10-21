<template>
  <v-date-input
    :allowed-dates="zeitraumEnden"
    :min="fruehestesEnde"
    :model-value="date ?? ''"
    :picker-props="pickerProps"
    prepend-icon=""
    prepend-inner-icon="$calendar"
    :readonly="readonly"
    :rules="dateRules"
    @update:model-value="pick"
  />
</template>

<script setup lang="ts">
import type { ZeitraumEinheit } from "@/util/common/zeitraum";
import type { ValidationRule } from "@/util/validationRules";

import { computed, watch } from "vue";

import { toIsoDate } from "@/util/common/isoDate";
import {
  countWholePeriods,
  lastDayOfPeriods,
  snapToLastDayOfPeriod,
} from "@/util/common/zeitraum";
import { zeitraumEndeRule } from "@/util/common/zeitraumRules";

const date = defineModel<string | null | undefined>();

const {
  readonly,
  rules = [],
  zeitraumBeginn = "",
  zeitraumEinheit = "day",
} = defineProps<{
  readonly?: boolean;
  rules?: ValidationRule[];
  zeitraumBeginn?: string | null;
  zeitraumEinheit?: ZeitraumEinheit;
}>();

const beginn = computed(() => zeitraumBeginn ?? "");

const isZeitraumEnde = computed(() => beginn.value !== "");

const pickerProps = computed(() => (readonly ? { disabled: true } : undefined));

const dateRules = computed(() => {
  const given = isZeitraumEnde.value
    ? [...rules, zeitraumEndeRule(beginn.value, zeitraumEinheit)]
    : rules;
  return given.map((rule) => (value: unknown) => rule(toIsoDate(value)));
});

const zeitraumEnden = computed(() =>
  isZeitraumEnde.value
    ? (value: unknown) =>
        countWholePeriods(beginn.value, toIsoDate(value), zeitraumEinheit) !==
        undefined
    : undefined
);

const fruehestesEnde = computed(() =>
  isZeitraumEnde.value
    ? lastDayOfPeriods(beginn.value, 1, zeitraumEinheit)
    : undefined
);

function pick(value: unknown): void {
  const gewaehlt = toIsoDate(value);
  date.value =
    gewaehlt && isZeitraumEnde.value
      ? snapToLastDayOfPeriod(beginn.value, gewaehlt, zeitraumEinheit)
      : gewaehlt;
}

watch(beginn, (next, previous) => {
  if (!next || !date.value) {
    return;
  }
  const periods = countWholePeriods(previous, date.value, zeitraumEinheit);
  date.value =
    periods === undefined || zeitraumEinheit === "day"
      ? snapToLastDayOfPeriod(next, date.value, zeitraumEinheit)
      : lastDayOfPeriods(next, periods, zeitraumEinheit);
});
</script>
