<template>
  <span>
    <template
      v-for="(eintrag, index) in kette"
      :key="index"
    >
      <template v-if="index > 0"> → </template>
      <strong v-if="eintrag.current">{{ eintrag.projektnummer }}</strong>
      <template v-else>{{ eintrag.projektnummer }}</template>
    </template>
  </span>
</template>

<script setup lang="ts">
import type { ProjektketteEintrag } from "@/types/projekt/ProjektketteEintrag";

defineProps<{ kette: ProjektketteEintrag[] }>();
</script>
