<template>
  <v-container class="fill-height d-flex align-center flex-wrap">
    <v-row class="text-center">
      <v-col>
        <h1 class="text-display-medium font-weight-bold mb-10">Get Started</h1>
        <h3>Dokumentation für die RefArch ist hier zu finden:</h3>
        <div>
          <a
            href="https://refarch.oss.muenchen.de/templates"
            target="_blank"
            rel="noopener noreferrer"
            @click="documentationClicked = true"
            >Dokumentation RefArch-Templates</a
          >
        </div>
        <div>
          <a
            href="https://refarch.oss.muenchen.de/"
            target="_blank"
            rel="noopener noreferrer"
            @click="documentationClicked = true"
            >Dokumentation RefArch</a
          >
        </div>
      </v-col>
    </v-row>
    <yes-no-dialog
      v-model="saveLeaveDialog"
      dialogtitle="Wirklich verlassen?"
      dialogtext="Wollen Sie wirklich nicht die Dokumentation anschauen?"
      @no="cancel"
      @yes="leave"
    />
  </v-container>
</template>

<script setup lang="ts">
import { ref } from "vue";

import YesNoDialog from "@/components/common/YesNoDialog.vue";
import { useSaveLeave } from "@/composables/saveLeave";
import { Role } from "@/types/Role";

definePage({
  meta: {
    hasAnyRole: [Role.READER, Role.WRITER],
  },
});

const documentationClicked = ref(false);
const { cancel, leave, saveLeaveDialog } = useSaveLeave(isDirty);

function isDirty(): boolean {
  return !documentationClicked.value;
}
</script>
